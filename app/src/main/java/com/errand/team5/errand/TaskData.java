package com.errand.team5.errand;

import java.io.Serializable;

/**
 * Created by nicksmider on 4/1/18.
 */

public class TaskData implements Serializable {

    private String apiKey;
    private String userID;
    private String title;
    private String type;
    private String address;
    private String address2;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String description;
    private String city;
    private String state;
    private String zip;
    private int price;
    private String specialInstructions;

    public TaskData(){};

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
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

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public int getTimeToComplete() {
        return timeToComplete;
    }

    public void setTimeToComplete(int timeToComplete) {
        this.timeToComplete = timeToComplete;
    }

    public int getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(int expirationTime) {
        this.expirationTime = expirationTime;
    }

    private int timeToComplete;
    private int expirationTime;


}
