package com.example.vicky.healthvibeclinic;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by vicky on 7/9/2017.
 */

public class EditProfileFragment extends Fragment implements
        ConfirmPasswordDialog.OnConfirmPasswordListener {

    @Override
    public void onConfirmPassword(String Password) {
        Log.d(TAG, "onConfirmPassword : got the password" + Password);

        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider
                .getCredential(mAuth.getCurrentUser().getEmail(),Password );

        ////////// Prompt the user to re-provide their sign-in credentials
        mAuth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "User re-authenticated.");

                            ////// check to see if the email is not already present in the database
                            mAuth.fetchProvidersForEmail(mEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                                    if(task.isSuccessful()){

                                        try{

                                            if(task.getResult().getProviders().size()==1){
                                                Log.d(TAG, "onComplete: that email is already in use.");
                                                Toast.makeText(getActivity(),"That email is already in use",Toast.LENGTH_SHORT).show();

                                            }else  {
                                                Log.d(TAG, "onComplete: That email is available");

                                                /////// the email is available so update it
                                                mAuth.getCurrentUser().updateEmail(mEmail.getText().toString())
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d(TAG, "User email address updated.");
                                                                    Toast.makeText(getActivity(),"Email updated",Toast.LENGTH_SHORT).show();
                                                                    mFirebaseMethods.updateEmail(mEmail.getText().toString());

                                                                }
                                                            }
                                                        });

                                            }
                                        }catch (NullPointerException e){
                                            Log.e(TAG, "onComplete: NullPointerException:"  +e.getMessage());
                                        }

                                    }

                                }
                            });




                        }else {
                            Log.d(TAG, "onComplete: re_authentication failed.");
                        }

                    }
                });

    }


    private static final String TAG = "EditProfileFragment";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    private String userID;

    private ImageView backArrow;

    // EditProfil Fragment widgets
    private EditText mUsername,mEmail,mAddress;


    //vars
    private  ClinicSettings mClinicSettings;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_editprofile,container,false);


        mUsername =(EditText)view.findViewById(R.id.username);
        mEmail =(EditText)view.findViewById(R.id.email);
        mAddress =(EditText)view.findViewById(R.id.address);

        mFirebaseMethods = new FirebaseMethods(getActivity());

        setupFirebaseAuth();

        backArrow = (ImageView)view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick : navigating back to Main Activity" );
                getActivity().finish();
            }
        });

        ImageView checkmark = (ImageView)view.findViewById(R.id.saveChanges);
        checkmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick : attempting to save changes.");
                saveProfileSettings();
            }
        });


        return view;
    }

    private void saveProfileSettings(){
        final String username = mUsername.getText().toString();
        final String email = mEmail.getText().toString();
        final String address = mAddress.getText().toString();







        // case 1: if the user made a change to his username
        if(!mClinicSettings.getClinic().getUsername().equals(username)){

            checkIfUsernameExists(username);

        }
        // case 2: if the user made a change to his email
        if(!mClinicSettings.getClinic().getEmail().equals(email)) {

            // step1) Re-authenticate
            //       -Confirm the password and email
            ConfirmPasswordDialog dialog = new ConfirmPasswordDialog();
            dialog.show(getFragmentManager(),getString(R.string.confirm_password_dialog));
            dialog.setTargetFragment(EditProfileFragment.this,1);


            // step2) check if the email is already registered
            //      -"fetchProvidersForEmail(String email)"
            // step3) change email
            //     -submit the email to the database and authentication

        }

        /**
         * change the rest of  the settings that do not require uniquenes
         */

        if(!mClinicSettings.getClinic().getAddress().equals(address)){
            // update Address
            mFirebaseMethods.updateAddress(address);
        }




    }

    /**
     *
     Check if @param username already exists in the database
     */
    private void checkIfUsernameExists(final String username) {
        Log.d(TAG, "checkIfUsernameExists : checking if " +username +"already exists.");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_clinic))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.exists()){
                    //add the username
                    mFirebaseMethods.updateUsername(username);
                    Toast.makeText(getActivity(),"saved username. " , Toast.LENGTH_SHORT).show();

                }
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    if(singleSnapshot.exists()){
                        Log.d(TAG, "checkIfUsernameExists :FOUND A MATCH" + singleSnapshot.getValue(Clinic.class).getUsername());
                        Toast.makeText(getActivity(),"That username already exists " , Toast.LENGTH_SHORT).show();

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setProfileWidgets(ClinicSettings clinicSettings){
        //Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from firebase database: " + userSettings.toString());
        // Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from firebase database: " + userSettings.getUser().getUsername());

        mClinicSettings = clinicSettings;

        Clinic clinic = clinicSettings.getClinic();
       // UserMedicalHistoric historic = userSettings.getHistoric();




        mEmail.setText(clinicSettings.getClinic().getEmail());
        mUsername.setText(clinicSettings.getClinic().getUsername());
        mAddress.setText(clinicSettings.getClinic().getAddress());


    }

     /*
    ------------------------------------ Firebase ---------------------------------------------
     */

    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        userID = mAuth.getCurrentUser().getUid();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();


                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //retrieve user information from the database
                setProfileWidgets(mFirebaseMethods.getClinicSettings(dataSnapshot));

                //retrieve images for the user in question

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

}
