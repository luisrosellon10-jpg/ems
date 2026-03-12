package ems.model;

import java.sql.Timestamp;

public class Announcement {
    private long id;
    private String title;
    private String body;
    private Timestamp postedAt;
    private long authorId;
    private String authorName;
    private AnnouncementTarget target; // See enum below

    // Getters/setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public Timestamp getPostedAt() { return postedAt; }
    public void setPostedAt(Timestamp postedAt) { this.postedAt = postedAt; }
    public long getAuthorId() { return authorId; }
    public void setAuthorId(long authorId) { this.authorId = authorId; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public AnnouncementTarget getTarget() { return target; }
    public void setTarget(AnnouncementTarget target) { this.target = target; }
}