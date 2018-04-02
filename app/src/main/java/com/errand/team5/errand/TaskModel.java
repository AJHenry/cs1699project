package com.errand.team5.errand;


import java.io.Serializable;

/**
 * Created by Andrew on 3/7/2018.
 * This is the model for tasks for firebase
 */

//TODO Change Serializable to Parcelable
public class TaskModel implements Serializable {

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
    mTimestamp publishTime;

    //Time to complete errand, in minutes
    int timeToCompleteMins;

    //Cost of errand
    double baseCost;

    //Cost of money service
    //i.e paypal's cut
    double paymentCost;

    //Title of post
    String title;

    //Description of task
    String description;

    //Special instructions
    String specialInstructions;

    //mLocation of person requesting
    mLocation dropOffDestination;

    //mLocation of errand
    mLocation errandMLocation;

    //User of person
    User user;

    /* AUTO GENERATED */

    public TaskModel(){

    }

    public TaskModel(String taskId, String creatorId, int category, int status, mTimestamp publishTime, int timeToCompleteMins, double baseCost, double paymentCost, String title, String description, String specialInstructions, mLocation dropOffDestination, mLocation errandMLocation, User user) {
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
        this.errandMLocation = errandMLocation;
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTaskId() {
        return taskId;
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

    public mTimestamp getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(mTimestamp publishTime) {
        this.publishTime = publishTime;
    }

    public int getTimeToCompleteMins() {
        return timeToCompleteMins;
    }

    public void setTimeToCompleteMins(int timeToCompleteMins) {
        this.timeToCompleteMins = timeToCompleteMins;
    }

    public double getBaseCost() {
        return baseCost;
    }

    public void setBaseCost(double baseCost) {
        this.baseCost = baseCost;
    }

    public double getPaymentCost() {
        return paymentCost;
    }

    public void setPaymentCost(double paymentCost) {
        this.paymentCost = paymentCost;
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

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public mLocation getDropOffDestination() {
        return dropOffDestination;
    }

    public void setDropOffDestination(mLocation dropOffDestination) {
        this.dropOffDestination = dropOffDestination;
    }

    public mLocation getErrandMLocation() {
        return errandMLocation;
    }

    public void setErrandMLocation(mLocation errandMLocation) {
        this.errandMLocation = errandMLocation;
    }
}
