package com.example.vicky.healthvibeclinic;

/**
 * Created by vicky on 5/9/2017.
 */

public class Clinic {

    private String clinic_id;
    private String email;
    private String username;
    private  String address;

    public Clinic(String clinic_id, String email, String username, String address) {
        this.clinic_id = clinic_id;
        this.email = email;
        this.username = username;
        this.address = address;
    }

    public Clinic(){

    }

    public String getClinic_id() {
        return clinic_id;
    }

    public void setClinic_id(String clinic_id) {
        this.clinic_id = clinic_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Clinic{" +
                "clinic_id='" + clinic_id + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
