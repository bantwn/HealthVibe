package com.example.vicky.healthvibe;

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
 * Created by vicky on 13/7/2017.
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
     * Update Users_medical_historic node for current user
     * @param diseases
     * @param allergies
     * @param bloodtype
     */
    public  void  updateUserMedicalHistoric(String diseases,String allergies, String bloodtype){
        Log.d(TAG, "updateUserMedicalHistoric: updating user medical historic.");

        if(diseases !=null){

            myRef.child(mContext.getString(R.string.dbname_user_medical_historic))
                    .child(userID)
                    .child(mContext.getString(R.string.field_diseases))
                    .setValue(diseases);

        }

        if(allergies !=null){
            myRef.child(mContext.getString(R.string.dbname_user_medical_historic))
                    .child(userID)
                    .child(mContext.getString(R.string.field_allergies))
                    .setValue(allergies);
        }

        if(bloodtype !=null){
            myRef.child(mContext.getString(R.string.dbname_user_medical_historic))
                    .child(userID)
                    .child(mContext.getString(R.string.field_bloodtype))
                    .setValue(bloodtype);
        }


    }

    /**
     * update the username in the user's' node and in the user_medical_historic node
     * @param username
     */
    public void updateUsername(String username){
        Log.d(TAG, "updateUsername: updating username to: " + username);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);

        myRef.child(mContext.getString(R.string.dbname_user_medical_historic))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);
    }

    /**
     * update the email in the user's' node
     * @param email
     */
    public void updateEmail(String email){
        Log.d(TAG, "updateEmail: updating email to: " + email);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_email))
                .setValue(email);


    }

   // public boolean checkIfUsernameExists(String username, DataSnapshot datasnapshot) {
  //      Log.d(TAG, "checkIfUsernameExists : checking if" + username + "already exists.");

  //      User user = new User();

  //      for (DataSnapshot ds : datasnapshot.child(userID).getChildren()) {
   //         Log.d(TAG, "checkIfUsernameExists : datasnapshot: " + ds);
//
 //           user.setUsername(ds.getValue(User.class).getUsername());
//            Log.d(TAG, "checkIfUsernameExists : username: " + user.getUsername());
//
//            if (user.getUsername().equals(username)) {
//                Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH: " + user.getUsername());
//               return true;
//            }
//        }
//        return false;

//    }

    /*
    Register a new email and password to firebase Authentication
     */
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
     * Add information to the users nodes
     * Add information to the user_medical_historic
     *
     * @param email
     * @param username
     * @param diseases
     * @param allergies
     * @param bloodtype
     */

    public void addNewUser(String email, String username, String diseases, String allergies, String bloodtype) {

        User user = new User(userID, email, username);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .setValue(user);

        UserMedicalHistoric historic = new UserMedicalHistoric(diseases, allergies, bloodtype);

        myRef.child(mContext.getString(R.string.dbname_user_medical_historic))
                .child(userID)
                .setValue(historic);
    }

    /**
     * Retrieves the medical historic for the user currently logged in
     * Database: user_medical_historic node
     *
     * @param dataSnapshot
     * @return
     */
    public UserSettings getUserSettings(DataSnapshot dataSnapshot) {
        Log.d(TAG, "getUserMedicalHistoric : retrieving user medical historic from firebase");

        UserMedicalHistoric historic = new UserMedicalHistoric();
        User user = new User();

        for (DataSnapshot ds : dataSnapshot.getChildren()) {

            // user_medical_historic node
            if (ds.getKey().equals(mContext.getString(R.string.dbname_user_medical_historic))) {
                Log.d(TAG, "getUserMedicalHistoric : datasnapshot: " + ds);

                try {


                    historic.setAllergies(
                            ds.child(userID)
                                    .getValue(UserMedicalHistoric.class)
                                    .getAllergies()
                    );
                    historic.setBloodtype(
                            ds.child(userID)
                                    .getValue(UserMedicalHistoric.class)
                                    .getBloodtype()
                    );
                    historic.setDiseases(
                            ds.child(userID)
                                    .getValue(UserMedicalHistoric.class)
                                    .getDiseases()
                    );

                    Log.d(TAG, "getUserMedicalHistoric : retrieved user_medical_historic information:" + historic.toString());
                } catch (NullPointerException e) {
                    Log.e(TAG, "getMedicalHistoric : NullPointerException" + e.getMessage());
                }


            }
            // users node
            if (ds.getKey().equals(mContext.getString(R.string.dbname_users))) {
                Log.d(TAG, "getUserMedicalHistoric : datasnapshot: " + ds);

                user.setEmail(
                        ds.child(userID)
                                .getValue(User.class)
                                .getEmail()
                );
                user.setUser_id(
                        ds.child(userID)
                                .getValue(User.class)
                                .getUser_id()
                );
                user.setUsername(
                        ds.child(userID)
                                .getValue(User.class)
                                .getUsername()
                );
                Log.d(TAG, "getUser : retrieved user information:" + user.toString());

            }

        }
        return new UserSettings(user, historic);

    }
}
