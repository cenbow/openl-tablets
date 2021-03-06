package org.openl.rules.repository.api;

import java.util.Date;

/**
 * @author Yury Molchan
 */
public class FileData {
    private String name;
    private long size;
    private String author;
    private String comment;
    private Date modifiedAt;
    private String version;
    private boolean deleted;

    /**
     * The full path of the file from the root folder. The path MUST not start
     * from the '/' symbol. The allowed folder separator is '/' symbol.
     * 
     * @return the full path name from the root folder.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The file size in bytes. Allowed value for the deleted file is -1, but can
     * be equals to the size of the deleted file.
     * 
     * @return the file size.
     */
    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    /**
     * The author of the last modification. Can be empty.
     * 
     * @return The author of the last modification.
     */
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * The comment for the file. Can be empty.
     * 
     * @return the file comment.
     */
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * The last modification time. This value is get from the repository system.
     *
     * Cannot be modified from the client side.
     * 
     * @return the time the file was last modified.
     */
    public Date getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    /**
     * The version of the file. This value is get from the repository system.
     * The latest file in the history MUST have the latest modification time.
     * 
     * Can be empty. Cannot be modified from the client side.
     * 
     * @return the version (revision) of the file.
     */
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * The mark that the file was deleted. This value is get from the repository
     * system.
     *
     * Cannot be modified from the client side.
     * 
     * @return true if the file was deleted.
     */
    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
