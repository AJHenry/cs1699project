package com.errand.team5.errand;

import java.sql.Date;

/**
 * Created by Andrew on 3/7/2018.
 */

public class TaskModel {

    String taskID;
    String type;
    String creatorID;
    int status;


    String title;
    String description;
    String address;
    String city;
    String state;
    String zip;
    Date time;
    String dateToTime;
    String timeToComplete;
    int price;




    public TaskModel(String taskID, String type, String creatorID, int status, String title, String description, String address, String city, String state, String zip, Date time) {
        this.taskID = taskID;
        this.type = type;
        this.creatorID = creatorID;
        this.status = status;
        this.title = title;
        this.description = description;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.time = time;
    }

    public TaskModel(String taskID, String title, String timeToComplete, int price, String description) {
        this.taskID = taskID;
        this.title = title;
        this.timeToComplete = timeToComplete;
        this.price = price;
        this.description = description;
    }

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCreatorID() {
        return creatorID;
    }

    public void setCreatorID(String creatorID) {
        this.creatorID = creatorID;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getDateToTime() {
        return "100 mins ago";
    }

    public void setDateToTime(String dateToTime) {
        this.dateToTime = dateToTime;
    }

    public String getTimeToComplete() {
        return timeToComplete;
    }

    public void setTimeToComplete(String timeToComplete) {
        this.timeToComplete = timeToComplete;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
