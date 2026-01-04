package com.clearner.models;

public class User {
    private String id;
    private String displayName;
    private long createdAt;
    private long lastActiveAt;
    private String syncStatus;

    public User() {}

    public User(String id, String displayName, long createdAt, long lastActiveAt, String syncStatus) {
        this.id = id;
        this.displayName = displayName;
        this.createdAt = createdAt;
        this.lastActiveAt = lastActiveAt;
        this.syncStatus = syncStatus;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(long lastActiveAt) { this.lastActiveAt = lastActiveAt; }

    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
}
