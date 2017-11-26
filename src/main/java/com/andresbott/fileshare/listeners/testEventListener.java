/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.andresbott.fileshare.listeners;


import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component(label = "ACL Event Listener", immediate = true, metatype=false)

@Service(EventListener.class)
public class testEventListener implements EventListener {
      
    private Logger log = LoggerFactory.getLogger(this.getClass());
      
    private BundleContext bundleContext;
      
    //Inject a Sling ResourceResolverFactory
    @Reference
    private ResourceResolverFactory resolverFactory;
      
    private static final String  COMPONENT_RESOURCE_PATH ="/apps/eag/eag-commons/components/content/componentcacheflush";
     
     
    private Session session;
      
    private ObservationManager observationManager;
      
  //Inject a Sling ResourceResolverFactory to create a Session requited by the EventHandler
    @Reference
    private SlingRepository repository;
    
    
    /**
    *
    */

    protected void activate(ComponentContext ctx) {
        log.error("Activating compontent");
    	this.bundleContext = ctx.getBundleContext();  
        try {



            Map<String,Object> param = new HashMap<String,Object>();
            
            param.put(ResourceResolverFactory.SUBSERVICE, "filesAccess");
            
            ResourceResolver resourceResolver = resolverFactory.getServiceResourceResolver(param);
            
            session = resourceResolver.adaptTo(Session.class);


//            session = repository.loginAdministrative(null); // - This code works

            log.info("Using user ID:"+  session.getUserID());

            
            // Setup the event handler to respond to a new claim under content/claim.... 
            observationManager = session.getWorkspace().getObservationManager();
            
            String[] types = {"nt:unstructured"};
            String path = "/content"; // define the path


            observationManager.addEventListener(this, Event.PROPERTY_CHANGED, path, true, null, types, false);
            
            log.info("Observing property changes to {} nodes under {}", Arrays.asList(types), path);
            resourceResolver.close();
        }catch(Exception e){
            log.error("unable to register session",e);
            
        }
    }

    public void onEvent(EventIterator it) {
 
        log.info("IN ONE EVENT!");

        try {
            while (it.hasNext()) {
                Event event = it.nextEvent();
                //business logic to perform on event execution
            }
        }catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}

