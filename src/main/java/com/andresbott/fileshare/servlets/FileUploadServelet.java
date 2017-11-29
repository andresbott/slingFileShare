/*
 *  Copyright 2017 Nate Yolles
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.andresbott.fileshare.servlets;

import com.andresbott.fileshare.FileShareFileNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.*;
import javax.jcr.*;
import javax.jcr.query.Query;
import javax.servlet.Servlet;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.IOUtils;
import org.apache.sling.api.resource.Resource;
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
import org.apache.sling.jcr.api.SlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component(
        immediate = true,
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/fileShare/upload",
                "sling.servlet.methods={get,post}"
        },
        configurationPid = "com.andresbott.fileshare.servelet"
)
@Designate(ocd=FileUploadServelet.Configuration.class)
public class FileUploadServelet extends SlingAllMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(getClass());

//    @Property(label="andresbott my label", name="attributeName",value="http://companyservices/myservice?wsdl",description = "some description")
//    static final String SERVICE_ENDPOINT_URL = "service.endpoint.url";

    private String serviceEndpointUrl;

    private boolean enabled;

    //Inject a Sling ResourceResolverFactory
    @Reference
    private ResourceResolverFactory resolverFactory;

    //Inject a Sling ResourceResolverFactory to create a Session requited by the EventHandler
    @Reference
    private SlingRepository repository;

    @Activate
    @Modified
    protected void Activate(Configuration config) {
        //TODO programaticly change permision of desired folders, becasue not posible with node definition
        boolean enabled = config.enabled();
    }

    @ObjectClassDefinition(name = "Annotation Demo Servlet - OSGi")
    public @interface Configuration {
        @AttributeDefinition(
                name = "Enable",
                description = "Sample boolean property"
        )
        boolean enabled() default false;
    }




    protected void getIfno(final SlingHttpServletRequest request,
                          final SlingHttpServletResponse response) throws  IOException {


        ResourceResolver resolver = request.getResourceResolver();
        Session session = resolver.adaptTo(Session.class);
        FileShareFileNode myFile = new FileShareFileNode(resolver);

        PrintWriter out = response.getWriter();

        out.println("bla");

        List<RequestParameter> parameters = request.getRequestParameterList();
        out.println("parameter List");
        out.println(parameters);

        out.println("pathInfo");
        RequestPathInfo info = request.getRequestPathInfo();
        String Extension = info.getExtension();
        String sufix = info.getSuffix();



        out.println("pathIfno");
        out.println( request.getRequestPathInfo());

        out.println("Extension");
        out.println( Extension);

        out.println("Sufix");
        out.println( sufix);


        out.println("LocalAddres");
        out.println(request.getLocalAddr());
        out.println("<hr>");


//        long old = 1209600;
        long old = 3600;
        long newOld = old * 1000;

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        long milis = timestamp.getTime();


        long past = milis - newOld;


        out.println("old: "+old);
        out.println("newOld: "+newOld);
        out.println("past: "+past);


        out.println("timeStamp: " + milis);


        String query= "SELECT p.* FROM [nt:file] AS p " +
                "WHERE ISDESCENDANTNODE(p, [/content/fileshare])" +
                "AND p.[metadata/fsh:creationTimestamp] < '"+past+"'";
        Iterator<Resource> result = resolver.findResources(query, Query.JCR_SQL2);

        out.println("query: "+ query);
        while(result.hasNext()) {
            Resource element = result.next();

            try {
                Node n = session.getNode(element.getPath());
//                log.info("FileSahe.schedule Clean: Dleing old node:"+element.getPath());
//                n.remove();
//                session.save();
//
            } catch (RepositoryException e) {

            }

            out.println("reuslt"+element.getPath());
        }

//        response.setContentLength((int) myFile.getFileSize());
//        response.addHeader("Cache-Control", "must-revalidate");
//        response.addHeader("Pragma", "public");


    }


    /**
     * Wraper to call processFile with download parameter
     * @param request
     * @param response
     * @param sufix
     * @throws IOException
     */
    protected void download(final SlingHttpServletRequest request,
                           final SlingHttpServletResponse response,String sufix) throws  IOException {

            this.processFile(request,response,"dw",sufix);
    }

    /**
     * Wraper to call processFile without download parameter (render)
     * @param request
     * @param response
     * @param sufix
     * @throws IOException
     */
    protected void get(final SlingHttpServletRequest request,
                            final SlingHttpServletResponse response,String sufix) throws  IOException {

        this.processFile(request,response,"get",sufix);
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
                response.setHeader("Content-Length",Long.toString(file.getFileSize()));

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


    @Override
    protected void doGet(final SlingHttpServletRequest request,
                         final SlingHttpServletResponse response) throws  IOException {

        RequestPathInfo info = request.getRequestPathInfo();
        String extension = info.getExtension();
        String sufix = info.getSuffix();
        if (sufix != null){
            if (sufix.length() > 0 ){
                sufix = sufix.substring(1);
            }
        }

        log.info("Doing GET with Extension: " + extension);

        if(extension != null){
            if (extension.equals("info")){
                this.getIfno(request, response);

            }else if (extension.equals("dw")){
                log.info("Downloading NODE:"+sufix);
                this.download(request, response, sufix);

            }else if(extension.equals("get")){
                log.info("Rendering NODE:" + sufix);
                this.get(request, response, sufix);
            }
        }else{
            response.sendRedirect("/apps/fileshare/content/fileLink.html");
        }

    }


    @Override
    protected void doPost(final SlingHttpServletRequest request,
                         final SlingHttpServletResponse response) throws IOException {

        final boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart) {

            try {
                Map<String,Object> reqParam = new HashMap<String,Object>();
                reqParam.put(ResourceResolverFactory.SUBSERVICE, "filesAccess");

                ResourceResolver resourceResolver = resolverFactory.getServiceResourceResolver(reqParam);


                FileShareFileNode file = new FileShareFileNode(resourceResolver);

                PrintWriter out = response.getWriter();

                String fileName = null;
                InputStream data = null;
                long size = 0;
                String mimeType = null;

                final Map<String, RequestParameter[]> params = request.getRequestParameterMap();
                for (final Map.Entry<String, RequestParameter[]> pairs : params.entrySet()) {
                    final String k = pairs.getKey();
                    final RequestParameter[] pArr = pairs.getValue();
                    final RequestParameter param = pArr[0];
                    final InputStream stream = param.getInputStream();
                    if (param.isFormField()) {
                        out.println("Form field " + k + " with value " + Streams.asString(stream) + " detected.");
                    } else {
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
                    response.sendRedirect("/apps/fileshare/content/fileLink.share.html/"+file.getHash());
                }else{
                    response.sendRedirect("/apps/fileshare/content/fileLink.html");
                }
            } catch (LoginException e) {
                log.error("Unable to create service user session "+e);
            }
        }
    }




}
