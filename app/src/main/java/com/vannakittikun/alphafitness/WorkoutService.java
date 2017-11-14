package com.vannakittikun.alphafitness;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

/**
 * Created by Rule on 11/9/2017.
 */

public class WorkoutService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    Location mLastLocation;
    MyDBHandler dbHandler;
    final int MY_LOCATION_REQUEST_CODE = 1;

    LocationRequest mLocationRequest;
    LocationRequest mLocationRequestUpdate;
    GoogleApiClient mGoogleApiClient;
    Marker beginMarker;
    Marker endMarker;
    FusedLocationProviderClient mFusedLocationClient;
    Polyline line;
    double currentLatitude;
    double currentLongitude;
    double distance = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        buildGoogleApiClient();
        Log.d("HEEEEEEEY", "FOUND U BRUH");
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mGoogleApiClient.connect();
        dbHandler = new MyDBHandler(this.getApplicationContext(), null, null, 1);

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        //mLocationRequest.setInterval(5000); //5 seconds
        //mLocationRequest.setFastestInterval(3000); //3 seconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //mLocationRequest.setSmallestDisplacement(0.1F); //1/10 meter
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, firstLocationCallback, Looper.myLooper());
    }

    LocationCallback firstLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                mLastLocation = location;
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();

                //Place current location marker
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                Bundle args = new Bundle();
                args.putParcelable("bundleLatLng", latLng);

                Intent intent = new Intent();
                intent.putExtra("latLng", args);
                sendBroadcast(intent);
                //move map camera
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            }
        }

    };

    /*
    LocationCallback mLocationCallbackUpdate = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());

                if (mLastLocation.distanceTo(location) > 4.572 && count > 0) {
                    distance += mLastLocation.distanceTo(location);
                }

                mLastLocation = location;
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();

                Intent intent = new Intent("record_workout");
                intent.putExtra("update", stepCounter);
                sendBroadcast();
                distanceText.setText(df2.format(metersToMiles(distance)));

                dbHandler.addLocation(currentLatitude, currentLongitude, Math.abs(chronometer.getBase() - SystemClock.elapsedRealtime()));
                //Toast.makeText(getActivity(), "Time: " + Math.abs(chronometer.getBase() - SystemClock.elapsedRealtime()) + " Distance: " + distance, Toast.LENGTH_SHORT).show();
                createPolyLine();
                count++;
            }
        }

    };
    */

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this.getApplicationContext());
        builder1.setMessage("Failed to connect");
        builder1.setCancelable(false);

        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        System.exit(0);
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
}
