package com.dt.projects.niraproxy.api;

import java.util.Date;

public class NiraCredentials {
    String username;
    String password;
    int noOfDaysLeft;
    Date lastModifiedTime;

    public NiraCredentials() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getNoOfDaysLeft() {
        return noOfDaysLeft;
    }

    public void setNoOfDaysLeft(int noOfDaysLeft) {
        this.noOfDaysLeft = noOfDaysLeft;
    }

    public Date getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    @Override
    public String toString() {
        return "NiraCredentials{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", noOfDaysLeft=" + noOfDaysLeft +
                ", lastModifiedTime=" + lastModifiedTime +
                '}';
    }
}
