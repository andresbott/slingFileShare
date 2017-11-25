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
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import javax.jcr.*;
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
import org.apache.sling.api.resource.ResourceNotFoundException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
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




    protected void render(final SlingHttpServletRequest request,
    final SlingHttpServletResponse response) throws  IOException {
        log.info("Rendering image");


        ResourceResolver resolver = request.getResourceResolver();
        Session session = resolver.adaptTo(Session.class);
        FileShareFileNode myFile = new FileShareFileNode(session);

        //myFile.selectFile("1511563907975ffc0d79f27b925126f35e5527d3d4cc4887c5fba6161fc63c8ee716012e28844");


//        PrintWriter out = response.getWriter();
        response.setContentType("image/jpeg");
        final String BASE_PATH="/content/fileshare";
        Node node = null;
        try {
            node = session.getNode(BASE_PATH);

            Node file = node.getNode("009.jpg");

            Node content = file.getNode("jcr:content");
            javax.jcr.Property fileData = content.getProperty("jcr:data");
            Binary bin = fileData.getBinary();
            InputStream fileDataS = bin.getStream();


            InputStream inStram = fileDataS;
            OutputStream outStram = response.getOutputStream();
            IOUtils.copy( inStram, outStram);
            outStram.close();
            inStram.close();


        } catch (RepositoryException e) {
            log.error("Unable lo load Node: exception: " + e.getMessage(),e);
        }



        response.setContentType("image/jpeg");
//        response.setContentLength((int) myFile.getFileSize());
//        response.addHeader("Cache-Control", "must-revalidate");
//        response.addHeader("Pragma", "public");


    }

    @Override
    protected void doGet(final SlingHttpServletRequest request,
                         final SlingHttpServletResponse response) throws  IOException {

        boolean renderIMG = false;

        if(renderIMG){
            this.render(request,response);
        }else{

            log.info("Rendering NODE");


            ResourceResolver resolver = request.getResourceResolver();
            Session session = resolver.adaptTo(Session.class);
            FileShareFileNode myFile = new FileShareFileNode(session);

            //myFile.selectFile("1511563907975ffc0d79f27b925126f35e5527d3d4cc4887c5fba6161fc63c8ee716012e28844");


//        PrintWriter out = response.getWriter();
            response.setContentType("image/jpeg");
            final String BASE_PATH="/content/fileshare";
            Node node = null;
            try {
                node = session.getNode(BASE_PATH);

                Node file = node.getNode("151162183801537560");

                Node content = file.getNode("jcr:content");
                javax.jcr.Property fileData = content.getProperty("jcr:data");
                Binary bin = fileData.getBinary();
                InputStream fileDataS = bin.getStream();


                InputStream inStram = fileDataS;
                OutputStream outStram = response.getOutputStream();
                IOUtils.copy( inStram, outStram);
                outStram.close();
                inStram.close();


            } catch (RepositoryException e) {
                log.error("Unable lo load Node: exception: " + e.getMessage(),e);
            }



            response.setContentType("image/jpeg");
//        response.setContentLength((int) myFile.getFileSize());
//        response.addHeader("Cache-Control", "must-revalidate");
//        response.addHeader("Pragma", "public");

        }






//                   // cannot handle the request for missing resources
//                 if (ResourceUtil.isNonExistingResource(request.getResource())) {
//                           throw new ResourceNotFoundException(
//                                       request.getResource().getPath(), "No Resource found");
//                       }
////
//                    Servlet rendererServlet;
//                   String ext = request.getRequestPathInfo().getExtension();
//
//        log.info("=====> "+ext);
//
//                   if (ext == null) {
//                            rendererServlet = streamerServlet;
//                       }
// else {
//                           rendererServlet = rendererMap.get(ext);
//                      }
//
//                    // fail if we should not just stream or we cannot support the ext.
//                    if (rendererServlet == null) {
//                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
//                                     "No renderer for extension='" + ext + "'");
//                         return;
//                    }
//
//                   request.getRequestProgressTracker().log("Using "
//                                  + rendererServlet.getClass().getName()
//                                    + " to render for extension=" + ext);
//                   rendererServlet.service(request, response);
//
//














//
//
//        response.setContentType(myFile.getMimeType());
//        response.setContentLength((int) myFile.getFileSize());
//        response.addHeader("Cache-Control", "must-revalidate");
//        response.addHeader("Pragma", "public");




//        try {
//
////            InputStream inStram = myFile.getFileData();
////            InputStream inStram = fileDataS;
////            OutputStream outStram = response.getOutputStream();
////            IOUtils.copy( inStram, outStram);
////            outStram.close();
////            inStram.close();
//
////            // Copy the contents of the file to the output stream
////            byte[] buf = new byte[1024];
////            int count = 0;
////            while ((count = inStram.read(buf)) >= 0) {
////                outStram.write(buf, 0, count);
////            }
////            outStram.close();
////            inStram.close();
////
////
////            IOUtils.copy(myFile.getFileData(), response.getOutputStream());
////            myFile.getFileData().close();
////            response.getOutputStream().close();
//        }catch (IOException e){
//            log.error("Unable to read content from file:"+myFile.getFileName() + " exception: "+e );
//            response.sendError(response.SC_NOT_FOUND);
//        }catch (IllegalStateException e){
//            log.error( ""+e );
//            response.sendError(response.SC_NOT_FOUND);
////            SC_INTERNAL_SERVER_ERROR
//        }
//
//




//            File srcFile = new File("/src_directory_path/hoge.txt");
//            FileUtils.copyFile(srcFile, resp.getOutputStream());
//
//

//


//        out.write(request.getResource().getPath());
//        out.write("\n");
//        out.write("GET");



//
//        out.write("name:" + myFile.getFileName());
//        out.write("size:" + myFile.getFileSize());
//        out.write("mime:"+ myFile.getMimeType());




//        if (file_exists($fichero)) {
//            header('Content-Description: File Transfer');

//            header('Content-Disposition: attachment; filename="'.basename($fichero).'"');
//            header('Expires: 0');
//            header('Cache-Control: must-revalidate');
//            header('Pragma: public');
//            header('Content-Length: ' . filesize($fichero));
//            readfile($fichero);
//            exit;
//        }
//        ?>
//
//

        //


//        To download a file:
//        header("Content-Disposition: attachment; filename=\"myData.kml\"");





//        out.write(sampleFelixService.getSettings());
    }


    @Override
    protected void doPost(final SlingHttpServletRequest request,
                         final SlingHttpServletResponse response) throws IOException {

//        PrintWriter out = response.getWriter();
//
//        response.setContentType("text/plain");
//        out.write("POST request \n");
//        out.write(request.getHeader("referer"));
//        out.write("\n");
//

//        private SlingRepository repo = new SlingRepository() {
//        }
//
//        log.info("Logg info for Fileupload POST request");
//
//
//        String path = "/path/to/your/node";
//        try {
//            session = repository.loginService(null, null); // this method requires additional setting in Apache Sling Service User Mapper Service. (AEM6)
//            //session = repository.loginAdministrative(repository.getDefaultWorkspace()); //this method is deprecated (it was used in previous versions)
//            Node node = session.getNode(path);
//            node.setProperty("propertyName", "propertyValue");
//            session.save();
//        } catch (Exception e) {
//            log.error(ExceptionUtils.getStackTrace(e));
//            e.printStackTrace();
//        } finally {
//            if(session != null) session.logout();
//        }


        ResourceResolver resolver = request.getResourceResolver();




        // Check that we have a file upload request
        final boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        PrintWriter out = null;
        try {
            out = response.getWriter();
            if (isMultipart) {


                Session session = resolver.adaptTo(Session.class);

                out.println("seesion uid: "+session.getUserID());
                final Map<String, RequestParameter[]> params = request.getRequestParameterMap();
                for (final Map.Entry<String, RequestParameter[]> pairs : params.entrySet()) {
                    final String k = pairs.getKey();
                    final RequestParameter[] pArr = pairs.getValue();
                    final RequestParameter param = pArr[0];
                    final InputStream stream = param.getInputStream();
                    if (param.isFormField()) {
                        out.println("Form field " + k + " with value " + Streams.asString(stream) + " detected.");
                        out.println(" size: "+ param.getSize());
                        out.println(" type: " + param.getContentType());

                    } else {
                        out.println("File field " + k + " with file name " + param.getFileName() + " detected.");

                        FileShareFileNode myFile = new FileShareFileNode(session);
                        myFile.createFile(param.getFileName(), param.getInputStream(), param.getSize(), param.getContentType());
//                        myFile.createClasicFile(param.getFileName(), param.getInputStream());
//                        myFile.save();



                    }
                }
            }
        }
        catch (IOException e){

            log.error("Error in post" + e.getMessage(),e);
        }




        //final PostResponse htmlResponse = createPostResponse(request);

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

        if (config != null) {
            properties = config;
        }


    }
}
