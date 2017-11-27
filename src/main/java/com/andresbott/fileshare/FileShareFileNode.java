package com.andresbott.fileshare;


import org.apache.sling.api.resource.*;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.jcr.*;
import javax.jcr.query.Query;

import java.io.InputStream;

import java.sql.Timestamp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 *
 */
public class FileShareFileNode {



    protected ResourceResolver resourceResolver;
    protected Session session;

    protected String fileName;
    protected long fileSize = 0;
    protected String mimeType;
    protected String hash="";
    protected InputStream fileData;

    final protected String BASE_PATH="/content/fileshare";
    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Node node = null;
    protected Node fileNode = null;
    protected Node contentNode = null;
    protected long timeStampt;

    protected boolean nodeStatus = false;




    /**
     * Constructor
     */
    public FileShareFileNode(ResourceResolver resolverFactory) {
        try {
            this.resourceResolver = resolverFactory;
            this.session = this.resourceResolver.adaptTo(Session.class);
            this.node = this.session .getNode(BASE_PATH);
        } catch (RepositoryException e) {
            log.error("Unable to get Node: "+ BASE_PATH + " exception: " + e.getMessage(),e);
        }

//        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        this.timeStampt = timestamp.getTime();
    }

    public FileShareFileNode(Session sesion) {
        try {
            this.node = sesion.getNode(BASE_PATH);
        } catch (RepositoryException e) {
            log.error("Unable to get Node: "+ BASE_PATH + " exception: " + e.getMessage(),e);
        }
//        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        this.timeStampt = timestamp.getTime();
    }


    /**
     * Calculate a Hash to uniquly identify a file
     * we will use a timestamp +  sha256 hash of the imput stream
     * alternatively we could also implement a combination of microtime + size
     */
    protected void calculateHash(long size){

        String time = String.valueOf(this.timeStampt);
        String sizeString = String.valueOf(size);
        this.hash = time+sizeString;
    }


    /**
     * Create a fsh:file node
     * @param filename original filename
     * @param data InputStream with the data to save
     */
    public void createFile(String filename,InputStream data,long size,String type){

        try {

            this.calculateHash(size);
            String hash =this.hash;

            this.fileNode = this.node.addNode(hash, "nt:file");
            Node content = this.fileNode.addNode("jcr:content", "nt:resource");
            content.setProperty("jcr:data",data);
            this.fileNode.addMixin("fsh:mixmetadata1");

            Node metaNode = this.fileNode.addNode("metadata", "fsh:metadata");

            if(filename.length()>0){
                this.fileName = filename;
                metaNode.setProperty("fsh:filename",filename);
            }

            if(size >0){
                this.fileSize = size;
                metaNode.setProperty("fsh:size",size);
            }

            if(type.length()>0){
                this.mimeType = type;
                metaNode.setProperty("fsh:mimeType",type);
            }

            metaNode.setProperty("fsh:hash",hash);
            metaNode.setProperty("fsh:creationTimestamp", this.timeStampt);

            this.nodeStatus = true;

        } catch (RepositoryException e) {
            log.error("Unable to create File Node: "+ filename + " exception: " + e.getMessage(),e);
        } catch (NullPointerException e){
            log.error("session not initialized: " + e.getMessage(),e);
        }
    }


    /**
     * will persist the current settings to repository
     * should be the last method called
     */
    public void save(){
        try {
            if(this.isNode()){
                this.node.save();
            }else{
                log.error("Unable to save Node, Failed on save method after isNode() validation");
            }
            this.resourceResolver.close();
            this.session.logout();
        } catch (RepositoryException e) {
            log.error("Unable to save Node " + e.getMessage(),e);
        }
    }

    /**
     * Return the file based on the nodename
     * @param s node name
     */
    public void selectFile(String s) {
        try {
            this.fileNode = this.node.getNode(s);
//            NodeIterator nodes = this.node.getNodes();
//            while (nodes.hasNext()) {
//                Node node = nodes.nextNode();
//                log.error("path=" + node.getPath() + "\n");
//
//            }
            log.info("selectFile() got Node" + this.fileNode.getIdentifier());

            Node content = this.fileNode.getNode("jcr:content");
            Property fileData = content.getProperty("jcr:data");
            Binary bin = fileData.getBinary();
            this.fileData = bin.getStream();

            Node metadata = this.fileNode.getNode("metadata");

            Property fileNameProp = metadata.getProperty("fsh:filename");
            Value fileNameValue = fileNameProp.getValue();
            this.fileName = fileNameValue.toString();

            Property fileSizeProp = metadata.getProperty("fsh:size");

            Value fileSizeValue = fileSizeProp.getValue();
            this.fileSize = Long.parseLong(fileSizeValue.toString());

            Property mimeTypeProp = metadata.getProperty("fsh:mimeType");
            Value mimeTypeValue  = mimeTypeProp.getValue();
            this.mimeType = mimeTypeValue.toString();

            this.nodeStatus = true;


        } catch (RepositoryException e) {
            log.error("Unable lo load Node: "+ s + " exception: " + e.getMessage());
        }
    }

    public boolean isNode() {
        log.info("NodeStatus: " + this.nodeStatus);
        if (this.nodeStatus) {
            try {

                String V = this.fileNode.getIdentifier();
                log.info("nodeFound: " + V);
                if(
                    this.mimeType != null &&
                    this.fileName != null &&
                    this.fileSize != 0
                ){
                    return true;
                }else{
                    return false;
                }
            } catch (RepositoryException e) {
                log.info("Repository Exception in method isNOde: " + e.getMessage());
                return false;
            } catch (NullPointerException e){
                log.info("NullPointerException in method isNOde: " + e.getMessage());
                return false;
            }
        }else {
            return false;
        }

    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return this.fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public InputStream getFileData() {
        return fileData;
    }

    public String getHash() {
        return hash;
    }

    public void clean(long keepFiles) {
        log.info("FileSahe.schedule Clean: cleaning files older thant='{}' seconds", keepFiles);
        long newOld = keepFiles * 1000;
        long past = this.timeStampt - newOld;

        String query= "SELECT p.* FROM [nt:file] AS p " +
                "WHERE ISDESCENDANTNODE(p, [/content/fileshare])" +
                "AND p.[metadata/fsh:creationTimestamp] < '"+past+"'";

        Iterator<Resource> result = this.resourceResolver.findResources(query, Query.JCR_SQL2);

        while(result.hasNext()) {
            Resource element = result.next();

            try {
                Node n = session.getNode(element.getPath());
                log.info("FileSahe.schedule Clean: Deleting old node:"+element.getPath());
                n.remove();
                session.save();

            } catch (RepositoryException e) {

            }


        }
        this.session.logout();
        this.resourceResolver.close();

    }
}