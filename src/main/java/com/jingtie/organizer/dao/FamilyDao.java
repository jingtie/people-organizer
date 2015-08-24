package com.jingtie.organizer.dao;

import java.util.LinkedList;

/**
 * Created by jingtie on 8/22/15.
 */
public class FamilyDao {

    private int id;
    private String name;
    private Long createdTime;
    private Long modifiedTime;
    private Long deletedTime;
    private LinkedList<Integer> members = null;

    public FamilyDao(int id, String name, Long createdTime, Long modifiedTime, Long deletedTime) {
        this.id = id;
        this.name = name;
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

    public Long getCreatedTime() {
        return createdTime;
    }

    public Long getModifiedTime() {
        return modifiedTime;
    }

    public Long getDeletedTime() {
        return deletedTime;
    }

    public LinkedList<Integer> getMembers() {
        return members;
    }
}
