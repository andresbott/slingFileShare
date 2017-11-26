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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Stream;

import javax.jcr.*;
import javax.jcr.observation.ObservationManager;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import com.andresbott.fileshare.FileShareFileNode;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.*;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.servlets.post.impl.SlingPostServlet;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.commons.codec.digest.DigestUtils;

//import org.apache.sling.jcr.resource.JcrResourceUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SlingServlet(
        metatype = false,
        paths = {"/bin/fileShare/upload"},
        methods = {"GET","POST"},
        label = "fileShare File upload",
        description = "FileShare - Handle the file upload."
)
public class FileUploadServelet extends SlingAllMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(getClass());



    //Inject a Sling ResourceResolverFactory
    @Reference
    private ResourceResolverFactory resolverFactory;



    //Inject a Sling ResourceResolverFactory to create a Session requited by the EventHandler
    @Reference
    private SlingRepository repository;



    protected void getIfno(final SlingHttpServletRequest request,
                          final SlingHttpServletResponse response) throws  IOException {


        ResourceResolver resolver = request.getResourceResolver();
        Session session = resolver.adaptTo(Session.class);
        FileShareFileNode myFile = new FileShareFileNode(session);

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
            Session session = resourceResolver.adaptTo(Session.class);

            FileShareFileNode file = new FileShareFileNode(session);
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

        } catch (LoginException e) {
            log.error("Unable to create service user session "+e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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

        // Check that we have a file upload request
        final boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart) {
            try {

                Map<String, Object> sparam = new HashMap<String, Object>();
                sparam.put(ResourceResolverFactory.SUBSERVICE, "filesAccess");
                ResourceResolver resourceResolver = resolverFactory.getServiceResourceResolver(sparam);
                Session session = resourceResolver.adaptTo(Session.class);

                FileShareFileNode file = new FileShareFileNode(session);

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
                file.save();
                response.sendRedirect("/apps/fileshare/content/fileLink.html/"+file.getHash());

            } catch (IOException e) {

                log.error("Error in post" + e.getMessage(), e);
            } catch (org.apache.sling.api.resource.LoginException e) {
                log.error("unable to register session", e);
            }
        }
    }


//    @Override
//    protected void doPost(final SlingHttpServletRequest request,
//                          final SlingHttpServletResponse response) throws IOException {
//        final VersioningConfiguration localVersioningConfig = createRequestVersioningConfiguration(request);
//
//        request.setAttribute(VersioningConfiguration.class.getName(), localVersioningConfig);
//
//        // prepare the response
//        final PostResponse htmlResponse = createPostResponse(request);
//        htmlResponse.setReferer(request.getHeader("referer"));
//
//        final PostOperation operation = getSlingPostOperation(request);
//        if (operation == null) {
//
//            htmlResponse.setStatus(
//                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
//                    "Invalid operation specified for POST request");
//
//        } else {
//            request.getRequestProgressTracker().log(
//                    "Calling PostOperation: {0}", operation.getClass().getName());
//            final SlingPostProcessor[] processors = this.cachedPostProcessors;
//            try {
//                operation.run(request, htmlResponse, processors);
//            } catch (ResourceNotFoundException rnfe) {
//                htmlResponse.setStatus(HttpServletResponse.SC_NOT_FOUND,
//                        rnfe.getMessage());
//            } catch (final Exception exception) {
//                log.warn("Exception while handling POST "
//                        + request.getResource().getPath() + " with "
//                        + operation.getClass().getName(), exception);
//                htmlResponse.setError(exception);
//            }
//
//        }
//
//        // check for redirect URL if processing succeeded
//        if (htmlResponse.isSuccessful()) {
//            if (redirectIfNeeded(request, htmlResponse, response)) {
//                return;
//            }
//        }
//
//        // create a html response and send if unsuccessful or no redirect
//        htmlResponse.send(response, isSetStatus(request));
//    }



    @Activate
    @Modified
    protected final void activate(final Map<String, Object> config) {
        Map<String, Object> properties = Collections.emptyMap();
        //TODO programaticly change permision of desired folders, becasue not posible with node definition
        if (config != null) {
            properties = config;
        }


    }
}
