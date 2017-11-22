package com.example.vicky.healthvibe;

/**
 * Created by vicky on 27/8/2017.
 */

public class UserSettings {

    private  User user;
    private UserMedicalHistoric historic;

    public UserSettings(User user, UserMedicalHistoric historic) {
        this.user = user;
        this.historic = historic;
    }

    public UserSettings() {

    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UserMedicalHistoric getHistoric() {
        return historic;
    }

    public void setHistoric(UserMedicalHistoric historic) {
        this.historic = historic;
    }

    @Override
    public String toString() {
        return "UserSettings{" +
                "user=" + user +
                ", historic=" + historic +
                '}';
    }
}
