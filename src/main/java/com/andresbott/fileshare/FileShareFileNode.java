package com.andresbott.fileshare;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.InputStream;


/**
 *
 */
public class FileShareFileNode {

    protected String Filename;
    protected String FileSize;
    final protected String BASE_PATH="/content/fileshare";
    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Node node = null;
    protected Node fileNode = null;
    protected Node contentNode = null;
    protected Node metadataNode = null;

    /**
     *
     * @param session
     */
    public FileShareFileNode(Session session) {
        try {
            this.node = session.getNode(BASE_PATH);
        } catch (RepositoryException e) {
            log.error("Unable to get Node: "+ BASE_PATH + " exception: " + e.getMessage(),e);
        }
    }

    /**
     * Create a fsh:file node
     * @param filename
     * @param data InputStream with the data to save
     */
    public void createFile(String filename,InputStream data){
        try {
            this.fileNode = this.node.addNode(filename, "fsh:file");
            this.contentNode = this.fileNode.addNode("jcr:content", "nt:resource");
            this.contentNode.setProperty("jcr:data",data);
            this.metadataNode = this.fileNode.addNode("metadata", "fsh:metadata");
    //            this.metadataNode = this.fileNode.addNode("metadata","fsh:metadata3+");
            this.metadataNode.setProperty("fsh:filename",filename);
            this.metadataNode.setProperty("fsh:mimeType","mime");
            this.metadataNode.setProperty("fsh:hash","some long HASH");


//        out.println(" size: "+ param.getSize());
//        out.println(" type: " + param.getContentType());
//        c.setProperty("jcr:mimeType","image/jpeg");
        } catch (RepositoryException e) {
            log.error("Unable to create File Node: "+ filename + " exception: " + e.getMessage(),e);
        } catch (NullPointerException e){
            log.error("session not initialized: " + e.getMessage(),e);
        }
    }

    /**
     * Add a parameter to metadata node
     */
    public void addParameter(){

    }



    /**
     * will persist the current settings to repository
     * should be the last method called
     */
    public void save(){
        try {
            this.node.save();
        } catch (RepositoryException e) {
            log.error("Unable to save Node " + e.getMessage(),e);
        }


    }


}