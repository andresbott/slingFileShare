package com.andresbott.fileshare.scheduler;

import com.andresbott.fileshare.FileShareFileNode;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

@Component(
        immediate = true,
        configurationPid = "com.andresbott.fileshare.filedeleteshedule"
        // If you wanted the properties to be private
        // property = {
        //     "scheduler.expression=* * * * * ?",
        //     "scheduler.concurrent:Boolean=false"
        // }
)
@Designate(ocd = FileDeleteScheduler.Configuration.class)
public class FileDeleteScheduler implements Runnable {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Override
    public void run() {
        log.info("run with val:" + keepFiles);
        try {
            Map<String, Object> param = new HashMap<String, Object>();
            param.put(ResourceResolverFactory.SUBSERVICE, "filesAccess");
            ResourceResolver resourceResolver = resolverFactory.getServiceResourceResolver(param);

            FileShareFileNode FileShare = new FileShareFileNode(resourceResolver);
            FileShare.clean(keepFiles);

        } catch (LoginException e) {
            log.error("Unable to create service user session "+e);
        }


    }

    private long keepFiles;

    @Activate
    protected void activate(Configuration config) {
        keepFiles = config.keepFiles();
    }

    @ObjectClassDefinition(name="FileShare File Delete Scheduler")

    public @interface Configuration {
        @AttributeDefinition(
                name="File keep:",
                description="How long is a file is kept before deleting, value in seconds, default 2 weeks")
        long keepFiles() default 1209600;

        @AttributeDefinition(
                name = "Concurrent",
                description = "Schedule task concurrently",
                type = AttributeType.BOOLEAN
        )
        boolean scheduler_concurrent() default false;

        @AttributeDefinition(
                name = "Expression",
                description = "Delete od files. cron-job expression. Default: run every 5 minutes.",
                type = AttributeType.STRING
        )
//        String scheduler_expression() default "*/5 * * * * ?";
        String scheduler_expression() default "0 * * * * ?";
    }
}