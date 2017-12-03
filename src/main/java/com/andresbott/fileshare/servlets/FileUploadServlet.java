package com.andresbott.fileshare.servlets;

import com.andresbott.fileshare.FileShareConstants;
import com.andresbott.fileshare.FileShareFileNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import javax.jcr.*;
import javax.servlet.Servlet;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
        immediate = true,
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/fileShare/upload",
                "sling.servlet.methods={get,post}"
        },
        configurationPid = "com.andresbott.fileshare.uploadService"
)
@Designate(ocd=FileUploadServlet.Configuration.class)
public class FileUploadServlet extends SlingAllMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private boolean allowAnon;

    //Inject a Sling ResourceResolverFactory
    @Reference
    private ResourceResolverFactory resolverFactory;

    @Activate
    @Modified
    protected void activate(Configuration config) {
        log.debug("Class: "+getClass().getName()+"Method: Actiavate()");
        this.allowAnon = config.allowAnon();
    }

    @ObjectClassDefinition(name = "FileShare Upload service")
    public @interface Configuration {
        @AttributeDefinition(
                name = "Allow anonymous",
                description = "Allow anonymous / unregistered users to upload / download  files"
        )
        boolean allowAnon() default true;
    }


    /**
     * Process GET request, returning the selected file base on the hashed id (node name)
     * @param request
     * @param response
     * @param action
     * @param sufix
     * @throws IOException
     */
    protected void  processFile(    final SlingHttpServletRequest request,
                                    final SlingHttpServletResponse response,
                                    String action,
                                    String sufix) throws  IOException {
        log.debug("Class: "+getClass().getName()+"Method: processFile()");
        try {
            Map<String,Object> param = new HashMap<String,Object>();
            param.put(ResourceResolverFactory.SUBSERVICE, "filesAccess");
            ResourceResolver resourceResolver = resolverFactory.getServiceResourceResolver(param);

            FileShareFileNode file = new FileShareFileNode(resourceResolver);
            file.selectFile(sufix);

            if( file.isNode() ){
                response.setContentType(file.getMimeType());
                response.setContentLength((int) file.getFileSize());
                response.addHeader("Cache-Control", "must-revalidate");
                response.addHeader("Pragma", "public");
                response.setHeader("Content-Length", Long.toString(file.getFileSize()));

                if(action.equals("dw")){
                    response.setHeader("Content-Disposition", "attachment;filename=" + file.getFileName());
                }

                IOUtils.copy(file.getFileData(), response.getOutputStream());
                file.getFileData().close();
                response.getOutputStream().close();

            }else{
                // TODO redirect to a not found page
               // response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            resourceResolver.close();
        } catch (LoginException e) {
            log.error("Unable to create service user session "+e);
        }
    }

    /**
     * check if user is allowed to perform get and post actions
     * based on anonymous and osgi configuration
     * logged in users will always be allowed
     * @param userSession
     * @return true if allowed false if not
     */
    protected boolean userIsAllowed(final Session userSession){

        String uid = userSession.getUserID();

        log.debug("checking permission for uid: " + uid + " and osgi setting: allowAnon: " + this.allowAnon);
        if ( ( uid.equals("anonymous") && this.allowAnon ) || !uid.equals("anonymous") ){
            return true;
        }else{
            return false;
        }
    }

    /**
     * check for user is allowed if a ResourceResolver is passed instead
     * of a session
     * @param resolver
     * @return
     */
    protected boolean userIsAllowed(final ResourceResolver resolver){
        Session userSession = resolver.adaptTo(Session.class);
        return  this.userIsAllowed(userSession);
    }


    /**
     * Process the Get Request based on permissions and selectors
     * @param request
     * @param response
     * @throws IOException
     */
    @Override
    protected void doGet(final SlingHttpServletRequest request,
                         final SlingHttpServletResponse response) throws  IOException {

        ResourceResolver resolver = request.getResourceResolver();
        Session userSession = resolver.adaptTo(Session.class);

        if (!this.userIsAllowed(userSession)){
            response.sendRedirect(FileShareConstants.REDIRECT_ANON_NOT_ALLOWED);
            resolver.close();
            userSession.logout();
        }else{
            RequestPathInfo info = request.getRequestPathInfo();
            String extension = info.getExtension();
            String sufix = info.getSuffix();
            if (sufix != null){
                if (sufix.length() > 0 ){
                    sufix = sufix.substring(1);
                }
            }

            log.info("FileShare GET with Extension: " + extension + "and suffix: " + sufix);
            if(extension != null){
                if (extension.equals("dw")){
                    this.processFile(request, response, "dw", sufix);
                }else if(extension.equals("get")){
                    this.processFile(request, response, "get", sufix);
                }
            }else{
                response.sendRedirect("/apps/fileshare/content/fileLink.html");
            }
//            userSession.logout();
//            resolver.close();

        }
    }

    /**
     * Process the POST Request based on permissions and provided Post Data
     * @param request
     * @param response
     * @throws IOException
     */
    @Override
    protected void doPost(final SlingHttpServletRequest request,
                         final SlingHttpServletResponse response) throws IOException {

        ResourceResolver resolver = request.getResourceResolver();

        if( ! this.userIsAllowed(resolver)){
            response.sendRedirect(FileShareConstants.REDIRECT_ANON_NOT_ALLOWED);
            resolver.close();
        }else{
            final boolean isMultipart = ServletFileUpload.isMultipartContent(request);
            if (isMultipart) {
                log.info("FileShare - Processing Post Request");
                try {

                    Map<String,Object> reqParam = new HashMap<String,Object>();
                    reqParam.put(ResourceResolverFactory.SUBSERVICE, "filesAccess");
                    ResourceResolver resourceResolver = resolverFactory.getServiceResourceResolver(reqParam);

                    FileShareFileNode file = new FileShareFileNode(resourceResolver);

                    String fileName = null;
                    InputStream data = null;
                    long size = 0;
                    String mimeType = null;

                    final Map<String, RequestParameter[]> params = request.getRequestParameterMap();
                    for (final Map.Entry<String, RequestParameter[]> pairs : params.entrySet()) {
                        final String k = pairs.getKey();
                        final RequestParameter[] pArr = pairs.getValue();
                        final RequestParameter param = pArr[0];
                        if (!param.isFormField()) {
                            fileName = param.getFileName();
                            mimeType = param.getContentType();
                            size = param.getSize();
                            data = param.getInputStream();
                        }
                    }

                    file.createFile(fileName, data, size, mimeType);
                    boolean fileCreatedSuccesfully = file.isNode();

                    file.save();

                    if(fileCreatedSuccesfully){
                        log.info("FileShare: File created succesfully with hash: " + file.getHash());
                        response.sendRedirect("/apps/fileshare/content/fileLink.share.html/" + file.getHash());
                    }else{
                        log.warn("FileShare - unable to Create filenode for file: "+fileName);
                        response.sendRedirect("/apps/fileshare/content/fileLink.html");
                    }
                } catch (LoginException e) {
                    log.error("Unable to create service user session "+e);
                }
            }
            resolver.close();
        }
    }
}
