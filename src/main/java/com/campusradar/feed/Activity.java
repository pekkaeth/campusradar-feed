package com.campusradar.feed;

public class Activity {
    private int id;
    private String title;
    private String description;
    private String category;
    private String place;
    private int slotsNeeded;
    private int joinedCount;
    private int hostId;
    private String hostName;
    private long minutesLeft;
    private boolean joinedByMe;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getPlace() { return place; }
    public void setPlace(String place) { this.place = place; }
    public int getSlotsNeeded() { return slotsNeeded; }
    public void setSlotsNeeded(int slotsNeeded) { this.slotsNeeded = slotsNeeded; }
    public int getJoinedCount() { return joinedCount; }
    public void setJoinedCount(int joinedCount) { this.joinedCount = joinedCount; }
    public int getHostId() { return hostId; }
    public void setHostId(int hostId) { this.hostId = hostId; }
    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }
    public long getMinutesLeft() { return minutesLeft; }
    public void setMinutesLeft(long minutesLeft) { this.minutesLeft = minutesLeft; }
    public boolean isJoinedByMe() { return joinedByMe; }
    public void setJoinedByMe(boolean joinedByMe) { this.joinedByMe = joinedByMe; }

    public int getSpotsLeft() { return Math.max(0, slotsNeeded - joinedCount); }
}