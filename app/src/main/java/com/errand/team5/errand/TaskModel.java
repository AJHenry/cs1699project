package com.errand.team5.errand;


import android.location.Location;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * Created by Andrew on 3/7/2018.
 * This is the model for tasks for firebase
 */

public class TaskModel {

    //Task identifier, created by firebase
    String taskId;

    //Creator identifier, created by Google
    String creatorId;

    //Category of errand
    //0 - Default (none)
    int category;

    //Status of errand
    //0 - new, unrequested
    //1 - in progress
    //2 - completed
    //3 - cancelled
    int status;

    //Time it was published
    Timestamp publishTime;

    //Time to complete errand, in minutes
    int timeToCompleteMins;

    //Cost of errand
    float baseCost;

    //Cost of money service
    //i.e paypal's cut
    float paymentCost;

    //Title of post
    String title;

    //Description of task
    String description;

    //Special instructions
    String specialInstructions;

    //Location of person requesting
    Location dropOffDestination;

    //Location of errand
    Location errandLocation;

    /* AUTO GENERATED */

    public TaskModel(String taskId, String creatorId, int category, int status, Timestamp publishTime,
                     int timeToCompleteMins, float baseCost, float paymentCost, String title, String description,
                     String specialInstructions, Location dropOffDestination, Location errandLocation) {
        this.taskId = taskId;
        this.creatorId = creatorId;
        this.category = category;
        this.status = status;
        this.publishTime = publishTime;
        this.timeToCompleteMins = timeToCompleteMins;
        this.baseCost = baseCost;
        this.paymentCost = paymentCost;
        this.title = title;
        this.description = description;
        this.specialInstructions = specialInstructions;
        this.dropOffDestination = dropOffDestination;
        this.errandLocation = errandLocation;
    }

    public String getTaskId() {
        return taskId;

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

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Timestamp getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Timestamp publishTime) {
        this.publishTime = publishTime;
    }

    public int getTimeToCompleteMins() {
        return timeToCompleteMins;
    }

    public void setTimeToCompleteMins(int timeToCompleteMins) {
        this.timeToCompleteMins = timeToCompleteMins;
    }

    public float getBaseCost() {
        return baseCost;
    }

    public void setBaseCost(float baseCost) {
        this.baseCost = baseCost;
    }

    public float getPaymentCost() {
        return paymentCost;
    }

    public void setPaymentCost(float paymentCost) {
        this.paymentCost = paymentCost;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public Location getDropOffDestination() {
        return dropOffDestination;
    }

    public void setDropOffDestination(Location dropOffDestination) {
        this.dropOffDestination = dropOffDestination;
    }

    public Location getErrandLocation() {
        return errandLocation;
    }

    public void setErrandLocation(Location errandLocation) {
        this.errandLocation = errandLocation;
    }
}
