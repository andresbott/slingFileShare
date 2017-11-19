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
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.servlets.post.impl.SlingPostServlet;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.jcr.api.SlingRepository;
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




    @Override
    protected void doGet(final SlingHttpServletRequest request,
                         final SlingHttpServletResponse response) throws  IOException {


        log.info("Logg info for Fileupload GEt request");

        PrintWriter out = response.getWriter();

        response.setContentType("text/plain");
        out.write(request.getResource().getPath());
        out.write("\n");
        out.write("GET");






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


        // Check that we have a file upload request
        final boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        PrintWriter out = null;
        try {
            out = response.getWriter();
            if (isMultipart) {
                final Map<String, RequestParameter[]> params = request.getRequestParameterMap();
                for (final Map.Entry<String, RequestParameter[]> pairs : params.entrySet()) {
                    final String k = pairs.getKey();
                    final RequestParameter[] pArr = pairs.getValue();
                    final RequestParameter param = pArr[0];
                    final InputStream stream = param.getInputStream();
                    if (param.isFormField()) {
                        out.println("Form field " + k + " with value " + Streams.asString(stream) + " detected.");
                    } else {
                        out.println("File field " + k + " with file name " + param.getFileName() + " detected.");
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
