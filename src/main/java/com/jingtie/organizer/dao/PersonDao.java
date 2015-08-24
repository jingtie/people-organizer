package com.jingtie.organizer.dao;

/**
 * Created by jingtie on 8/22/15.
 */
public class PersonDao {

    private int id;
    private String name;
    private String email;
    private Long createdTime;
    private Long modifiedTime;
    private Long deletedTime;

    public PersonDao(int id, String name, String email, Long createdTime, Long modifiedTime, Long deletedTime) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.createdTime = createdTime;
        this.modifiedTime = modifiedTime;
        this.deletedTime = deletedTime;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public Long getModifiedTime() {
        return modifiedTime;
    }

    public Long getDeletedTime() {
        return deletedTime;
    }
}
