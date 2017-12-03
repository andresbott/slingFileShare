package com.andresbott.fileshare;

import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;


import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;


public class ServiceExposeOsgiConfig {

    private final Logger log = LoggerFactory.getLogger(getClass());


    @Reference
    private ConfigurationAdmin configAdmin;


    private static final String LOGGER_FACTORY_PID = "org.apache.sling.commons.log.LogManager.factory.config";
    private static final String CUSTOM_LOGGER_PID = "org.apache.sling.commons.log.LogManager.factory.config.278b07c1-7eb9-43a8-8d84-8fed5ee6b0a4";
//    private static final String HTML_LIBRARY_MANAGER_PID = "com.day.cq.widget.impl.HtmlLibraryManagerImpl";
    private static final String MINIFY_PROPERTY = "htmllibmanager.minify";


    public void ServiceExposeOsgiConfig(){

    }

    public void doget( ) throws IOException {
        log.info("call to "+getClass().getName());
        /* Managed Service and Manage Service Factory configs */


        log.info("GET OSGI value of configAdmin: "+configAdmin);

        Configuration loggerFactoryConfig = configAdmin.getConfiguration(LOGGER_FACTORY_PID);
        Configuration loggerConfig = configAdmin.getConfiguration(CUSTOM_LOGGER_PID);

        /* returns true */
        boolean isFactoryPid = loggerConfig.getFactoryPid().equals(loggerFactoryConfig.getPid());

        /* Get all configs from the factory */
        try {
            /* Java filter syntax*/
            String filter = '(' + ConfigurationAdmin.SERVICE_FACTORYPID + '=' + LOGGER_FACTORY_PID + ')';
            Configuration[] allLoggerConfigs = configAdmin.listConfigurations(filter);
            for( int i = 0; i <= allLoggerConfigs.length - 1; i++){
                Configuration D = allLoggerConfigs[i];
                log.info("GET OSGI: "+D.toString());


            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
        }



        /*********************************************/


//        /* Get properties */
//        Configuration htmlLibraryManangerConfig = configAdmin.getConfiguration(HTML_LIBRARY_MANAGER_PID);
//        Dictionary<String, Object> properties = htmlLibraryManangerConfig.getProperties();
//
//        boolean isMinify = PropertiesUtil.toBoolean(properties.get(MINIFY_PROPERTY), false);
//        /*********************************************/


//        /* Set properties */
//        if (properties == null) {
//            properties = new Hashtable<String, Object>();
//        }
//
//        /* Remember HashTables don't accept null values. */
//        properties.put(MINIFY_PROPERTY, true);
//        htmlLibraryManangerConfig.update(properties);
    }
}
