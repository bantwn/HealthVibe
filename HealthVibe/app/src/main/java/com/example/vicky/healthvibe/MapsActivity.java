package com.example.vicky.healthvibe;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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

import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    private Button mRequest;

    private LatLng pickupLocation;

    private boolean requestBol = false;

    private Marker pickupMarker;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MapsActivity.this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
        }else{
            mapFragment.getMapAsync(this);
        }

        mRequest =(Button)findViewById(R.id.request);

        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               if(requestBol){
                   requestBol = false;

                   geoQuery.removeAllListeners();
                   clinicLocationRef.removeEventListener(clinicLocationRefListener);

                   if(clinicFoundID !=null){

                       DatabaseReference clinicRef = FirebaseDatabase.getInstance().getReference().child("userRequest");
                       clinicRef.removeValue();

                       DatabaseReference clinicRef2 = FirebaseDatabase.getInstance().getReference().child("clinics").child(clinicFoundID).child("userRideId");
                       clinicRef2.removeValue();
                       clinicFoundID = null;
                   }
                   clinicFound = false;
                   radius = 1;

                   String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                   DatabaseReference ref = FirebaseDatabase.getInstance().getReference("userRequest");
                   GeoFire geoFire = new GeoFire(ref);
                   geoFire.removeLocation(userID);

                   if(pickupMarker !=null){
                       pickupMarker.remove();
                   }
                   mRequest.setText("CALL EMERGENCY");

               }else {

                   requestBol = true;

                   String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                   DatabaseReference ref = FirebaseDatabase.getInstance().getReference("userRequest");
                   GeoFire geoFire = new GeoFire(ref);
                   geoFire.setLocation(userID,new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()));

                   pickupLocation = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
                   pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup Here").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));

                   mRequest.setText("Getting your ambulance...");

                   getClosestClinic();

               }

            }
        });
    }
    private int radius = 1;
    private boolean clinicFound = false;
    private String clinicFoundID;

    GeoQuery geoQuery;
    private void getClosestClinic(){
        DatabaseReference clinicLocation = FirebaseDatabase.getInstance().getReference().child("ClinicsAvailable");

        GeoFire geoFire = new GeoFire(clinicLocation);

        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude,pickupLocation.longitude),radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!clinicFound && requestBol){
                    clinicFound = true;
                    clinicFoundID = key;

                    DatabaseReference clinicRef = FirebaseDatabase.getInstance().getReference().child("clinics").child(clinicFoundID);
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    HashMap map = new HashMap();
                    map.put("userRideId",userId);
                    clinicRef.updateChildren(map);



                    getClinicLocation();
                    mRequest.setText("Looking for Clinic's Location...");

                }


            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                if(!clinicFound){
                    radius++;
                    getClosestClinic();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
     private Marker mClinicMarker;
     private DatabaseReference clinicLocationRef;
    private ValueEventListener clinicLocationRefListener;
     private void getClinicLocation(){

         clinicLocationRef = FirebaseDatabase.getInstance().getReference().child("clinicsWorking").child(clinicFoundID).child("l");
         clinicLocationRefListener = clinicLocationRef.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(DataSnapshot dataSnapshot) {
                 if(dataSnapshot.exists() && requestBol){
                     List<Object> map = (List<Object>)dataSnapshot.getValue();
                     double locationLat = 0;
                     double locationLng = 0;

                     mRequest.setText("Ambulance Found!");

                     if(map.get(0) !=null){
                         locationLat = Double.parseDouble(map.get(0).toString());
                     }

                     if(map.get(1) !=null){
                         locationLng = Double.parseDouble(map.get(1).toString());
                     }

                     LatLng clinicLatLng = new LatLng(locationLat,locationLng);

                     if(mClinicMarker !=null){
                         mClinicMarker.remove();
                     }

                     Location loc1 = new Location("");
                     loc1.setLatitude(pickupLocation.latitude);
                     loc1.setLongitude(pickupLocation.longitude);

                     Location loc2 = new Location("");
                     loc2.setLatitude(clinicLatLng.latitude);
                     loc2.setLongitude(clinicLatLng.longitude);

                     float distance = loc1.distanceTo(loc2);

                     if(distance<100){
                         mRequest.setText("Ambulance is Here");
                     }else {
                         mRequest.setText("Ambulance Found:" +String.valueOf(distance));
                     }




                     mClinicMarker = mMap.addMarker(new MarkerOptions().position(clinicLatLng).title("Your Ambulance").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_ambulance)));

                 }
             }

             @Override
             public void onCancelled(DatabaseError databaseError) {

             }
         });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;



        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MapsActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
        }
        buildGoogleApiClient();
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
        mLastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        //mMap.addMarker(new MarkerOptions().position(latLng));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));


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


    }
}