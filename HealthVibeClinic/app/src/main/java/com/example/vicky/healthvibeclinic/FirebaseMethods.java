package com.example.vicky.healthvibeclinic;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by vicky on 6/9/2017.
 */

public class FirebaseMethods {

    private static final String TAG = "FirebaseMethods";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private String userID;
    private Context mContext;

    public FirebaseMethods(Context context) {
        mContext = context;
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        if (mAuth.getCurrentUser() != null) {
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    /**
     *update the username in the clinic's' node
     * @param username
     */

    public void updateUsername(String username){
        Log.d(TAG, "updateUsername: updating username to: " + username);

        myRef.child(mContext.getString(R.string.dbname_clinic))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);


    }

    /**
     *update the email in the clinic's' node
     * @param email
     */

    public void updateEmail(String email){
        Log.d(TAG, "updateEmail: updating email to: " + email);

        myRef.child(mContext.getString(R.string.dbname_clinic))
                .child(userID)
                .child(mContext.getString(R.string.field_email))
                .setValue(email);


    }

    public  void updateAddress(String address){
        Log.d(TAG, "updateAddress: updating address to: " + address);

        if (address != null) {
            myRef.child(mContext.getString(R.string.dbname_clinic))
                    .child(userID)
                    .child(mContext.getString(R.string.field_address))
                    .setValue(address);
        }



    }


    public void registerNewEmail(final String email, String password, final String username) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(mContext, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        } else if (task.isSuccessful()) {
                            //send verification email
                            sendVerificationEmail();
                            userID = mAuth.getCurrentUser().getUid();
                            Log.d(TAG, "onComplete : Authstate changed:" + userID);
                        }

                    }
                });
    }

    public void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                            } else {
                                Toast.makeText(mContext, "couldn't send verification email", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

        }
    }

    /**
     * Add information to the clinic node
     * @param email
     * @param username
     * @param address
     */

    public void addNewClinic(String email,String username,String address){

        Clinic clinic = new Clinic(userID,email,username,address);

        myRef.child(mContext.getString(R.string.dbname_clinic))
                .child(userID)
                .setValue(clinic);

    }

    /**
     * Retrieves the Clinic user currently logged in database
     * @param dataSnapshot
     * @return
     */

    public ClinicSettings getClinicSettings(DataSnapshot dataSnapshot) {
        Log.d(TAG, "getUserMedicalHistoric : retrieving user medical historic from firebase");


        Clinic clinic = new Clinic();

        for (DataSnapshot ds : dataSnapshot.getChildren()) {


            // clinic node
            if (ds.getKey().equals(mContext.getString(R.string.dbname_clinic))) {
                Log.d(TAG, "getClinicSettings : datasnapshot: " + ds);

                clinic.setEmail(
                        ds.child(userID)
                                .getValue(Clinic.class)
                                .getEmail()
                );
                clinic.setClinic_id(
                        ds.child(userID)
                                .getValue(Clinic.class)
                                .getClinic_id()
                );
                clinic.setUsername(
                        ds.child(userID)
                                .getValue(Clinic.class)
                                .getUsername()
                );
                clinic.setAddress(
                        ds.child(userID)
                                .getValue(Clinic.class)
                                .getAddress()
                );
                Log.d(TAG, "getUser : retrieved user information:" + clinic.toString());

            }

        }
        return new ClinicSettings(clinic);

    }
}
