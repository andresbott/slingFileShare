package com.andresbott.fileshare;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.jcr.api.SlingRepositoryInitializer;
import org.apache.sling.jcr.repoinit.JcrRepoInitOpsProcessor;
import org.apache.sling.repoinit.parser.RepoInitParser;
import org.apache.sling.repoinit.parser.operations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
@Service(SlingRepositoryInitializer.class)
public class ServiceInit implements SlingRepositoryInitializer {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public static final String REPOINIT_FILE = "/provisioning/model.txt";

    @Reference
    private RepoInitParser parser;

    @Reference
    private JcrRepoInitOpsProcessor processor;

    @Override
    public void processRepository(SlingRepository repo) throws Exception {
        final Session s = repo.loginAdministrative(null);
        final InputStream is = getClass().getResourceAsStream(REPOINIT_FILE);
        try {
            if(is == null) {
                throw new IOException("Class Resource not found:" + REPOINIT_FILE);
            }

            final Reader r = new InputStreamReader(is, "UTF-8");

            List<Operation> ops = parser.parse(r);

            log.info("Executing {} repoinit Operations", ops.size());
            processor.apply(s, ops);
            s.save();
        } finally {
            s.logout();
            is.close();
//        }
    }
}
}
