package com.example.vicky.healthvibe;

/**
 * Created by vicky on 13/7/2017.
 */

public class UserMedicalHistoric {

    private String diseases;
    private String allergies;
    private String bloodtype;

    public UserMedicalHistoric(String diseases, String allergies, String bloodtype) {
        this.diseases = diseases;
        this.allergies = allergies;
        this.bloodtype = bloodtype;
    }

    public UserMedicalHistoric() {
    }

    public String getDiseases() {
        return diseases;
    }

    public void setDiseases(String diseases) {
        this.diseases = diseases;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getBloodtype() {
        return bloodtype;
    }

    public void setBloodtype(String bloodtype) {
        this.bloodtype = bloodtype;
    }

    @Override
    public String toString() {
        return "UserMedicalHistoric{" +
                "diseases='" + diseases + '\'' +
                ", allergies='" + allergies + '\'' +
                ", bloodtype='" + bloodtype + '\'' +
                '}';
    }
}
