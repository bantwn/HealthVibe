package com.example.vicky.healthvibeclinic;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    private String userId = "";
    private SupportMapFragment mapFragment;
    private LinearLayout mPatientInfo;
    private TextView mPatientName,mPatientDiseases,mPatientAllergies,mPatientBloodtype;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
         mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MapsActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
        }else{
            mapFragment.getMapAsync(this);
        }

        getAssignedUser();

        mPatientInfo = (LinearLayout)findViewById(R.id.patientInfo);
       // mPatientName =(TextView)findViewById(R.id.patientName);
        mPatientDiseases = (TextView)findViewById(R.id.patientDiseases);
        mPatientAllergies = (TextView)findViewById(R.id.patientAllergies);
        mPatientBloodtype = (TextView)findViewById(R.id.patientBloodtype);
    }

    private void getAssignedUser(){

        String clinicId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedUserRef = FirebaseDatabase.getInstance().getReference().child("clinics").child(clinicId).child("userRideId");

        assignedUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    userId = dataSnapshot.getValue().toString();
                    getAssignedUserPickupLocation();
                   // getAssignedUserPickupInfo();
                    getAssignedUserPickupHistoric();

                }else {
                    userId = "";
                    if(pickupMarker !=null){
                        pickupMarker.remove();
                    }

                    if(assignedUserPickupLocationRefListener !=null){

                        assignedUserPickupLocationRef.removeEventListener(assignedUserPickupLocationRefListener);
                    }
                    mPatientInfo.setVisibility(View.GONE);
                    // mPatientName.setText("");
                    mPatientDiseases.setText("");
                    mPatientAllergies.setText("");
                    mPatientBloodtype.setText("");



                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    Marker pickupMarker;
    private DatabaseReference assignedUserPickupLocationRef;
    private ValueEventListener assignedUserPickupLocationRefListener;

    private void getAssignedUserPickupLocation(){


        assignedUserPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("userRequest").child(userId).child("l");

        assignedUserPickupLocationRefListener = assignedUserPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()&& !userId.equals("")){
                    List<Object> map = (List<Object>)dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;



                    if(map.get(0) !=null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }

                    if(map.get(1) !=null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }

                    LatLng clinicLatLng = new LatLng(locationLat,locationLng);



                    pickupMarker = mMap.addMarker(new MarkerOptions().position(clinicLatLng).title("Pickup Location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getAssignedUserPickupHistoric(){
        mPatientInfo.setVisibility(View.VISIBLE);
       DatabaseReference mPatientHistoricDatabase = FirebaseDatabase.getInstance().getReference().child("user_medical_historic").child(userId);

        mPatientHistoricDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if(map.get("diseases")!=null){

                        mPatientDiseases.setText(("Diseases:"+map.get("diseases").toString()));
                    }
                    if(map.get("allergies")!=null){

                        mPatientAllergies.setText("Allergies:"+map.get("allergies").toString());
                    }
                    if(map.get("bloodtype")!=null){

                        mPatientBloodtype.setText("Bloodtype:"+map.get("bloodtype").toString());
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

  /*  private void getAssignedUserPickupInfo(){
        mPatientInfo.setVisibility(View.VISIBLE);
        DatabaseReference mPatientDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

        mPatientDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("username")!=null){

                        mPatientName.setText(map.get("username").toString());
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });




    } */




    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        buildGoogleApiClient();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    @Override
    public void onLocationChanged(Location location) {

        if(getApplicationContext() !=null) {

            mLastLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("ClinicsAvailable");
            DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("clinicsWorking");

            GeoFire geoFireAvailable = new GeoFire(refAvailable);
            GeoFire geoFireWorking = new GeoFire(refWorking);

            switch (userId){
                case "":
                    geoFireWorking.removeLocation(userID);
                    geoFireAvailable.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;

                default:
                    geoFireAvailable.removeLocation(userID);
                    geoFireWorking.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
            }




        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MapsActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    final int LOCATION_REQUEST_CODE = 1;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case LOCATION_REQUEST_CODE:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mapFragment.getMapAsync(this);
                } else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }


    @Override
    protected void onStop() {
        super.onStop();

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ClinicsAvailable");


        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userID);
    }
}
