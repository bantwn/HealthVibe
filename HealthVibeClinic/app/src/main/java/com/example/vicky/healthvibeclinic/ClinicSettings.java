package com.example.vicky.healthvibeclinic;

/**
 * Created by vicky on 6/9/2017.
 */

public class ClinicSettings {

    private  Clinic clinic;

    public ClinicSettings(Clinic clinic) {
        this.clinic = clinic;
    }

    public ClinicSettings() {

    }

    public Clinic getClinic() {
        return clinic;
    }

    public void setClinic(Clinic clinic) {
        this.clinic = clinic;
    }

    @Override
    public String toString() {
        return "ClinicSettings{" +
                "clinic=" + clinic +
                '}';
    }
}
