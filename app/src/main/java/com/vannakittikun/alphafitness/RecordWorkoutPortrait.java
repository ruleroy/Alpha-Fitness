package com.vannakittikun.alphafitness;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RecordWorkoutPortrait.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RecordWorkoutPortrait#newInstance} factory method to
 * create an instance of this fragment.
 */

public class RecordWorkoutPortrait extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleMap.OnMyLocationButtonClickListener, ActivityCompat.OnRequestPermissionsResultCallback, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    FragmentManager manager;
    private GoogleMap mMap;
    final int MY_LOCATION_REQUEST_CODE = 1;
    Location mLastLocation;

    LocationRequest mLocationRequest;
    LocationRequest mLocationRequestUpdate;
    GoogleApiClient mGoogleApiClient;
    Marker beginMarker;
    Marker endMarker;
    FusedLocationProviderClient mFusedLocationClient;
    Polyline line;
    DecimalFormat df2;

    double currentLatitude;
    double currentLongitude;
    public static boolean workoutMode;
    int count;

    public static double weeklyDistance;
    public static long weeklyTime;
    public static int weeklyWorkouts;
    public static int weeklySteps;

    double beginLat = 0;
    double beginLng = 0;
    double endLat = 0;
    double endLng = 0;

    ConstraintLayout parentView;
    Button startWorkout;
    Chronometer chronometer;
    TextView distanceText;
    long stopTime = 0;
    double distance = 0;

    MyDBHandler dbHandler;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.hasExtra("update")) {
                distanceText.setText(df2.format(metersToMiles(distance)));
            }

            if(intent.hasExtra("latLng")) {
                Bundle bundle = intent.getParcelableExtra("latLng");
                LatLng latLng = bundle.getParcelable("bundleLatLng");
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            }
        }
    };


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public RecordWorkoutPortrait() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RecordWorkoutPortrait.
     */
    // TODO: Rename and change types and number of parameters
    public static RecordWorkoutPortrait newInstance(String param1, String param2) {
        RecordWorkoutPortrait fragment = new RecordWorkoutPortrait();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("chronometer", chronometer.getBase());
        outState.putDouble("distance", distance);
        outState.putDouble("beginLat", beginLat);
        outState.putDouble("beginLng", beginLng);
        outState.putDouble("endLat", endLat);
        outState.putDouble("endLng", endLng);
        //outState.putDouble("lastLat", currentLatitude);
        //outState.putDouble("lastLng", currentLongitude);
        outState.putBoolean("workoutMode", workoutMode);
        outState.putLong("stopTime", stopTime);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        dbHandler = new MyDBHandler(getActivity(), null, null, 1);
        weeklyWorkouts = dbHandler.getWeeklyWorkouts(1);
        weeklyDistance = dbHandler.getWeeklyDistance(1);
        weeklyTime = dbHandler.getWeeklyTime(1);

        parentView = getActivity().findViewById(R.id.parentView);
        chronometer = getActivity().findViewById(R.id.chronometer);
        distanceText = getActivity().findViewById(R.id.weeklyDistanceText);
        startWorkout = getActivity().findViewById(R.id.startWorkout);

        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener()
        {
            @Override
            public void onChronometerTick(Chronometer chronometer)
            {
                weeklyTime = weeklyTime + 1000;
                dbHandler.updateWeeklyTime(1, weeklyTime);
                Log.d("WeeklyTime", Long.toString(weeklyTime));
            }
        });


        df2 = new DecimalFormat("#.###");

        if (savedInstanceState != null) {
            workoutMode = savedInstanceState.getBoolean("workoutMode");

            if(workoutMode) {
                chronometer.setBase(savedInstanceState.getLong("chronometer"));
                chronometer.start();
                startWorkout.setText("Stop Workout");
                startWorkout.setBackgroundColor(getResources().getColor(R.color.googleRed));
            } else {
                chronometer.setBase(SystemClock.elapsedRealtime() + savedInstanceState.getLong("stopTime"));
            }

            distance = savedInstanceState.getDouble("distance");
            distanceText.setText(df2.format(metersToMiles(distance)));

            beginLat = savedInstanceState.getDouble("beginLat");
            beginLng = savedInstanceState.getDouble("beginLng");
            endLat = savedInstanceState.getDouble("endLat");
            endLng = savedInstanceState.getDouble("endLng");


            //mLastLocation = new Location("");
            //mLastLocation.setLatitude(savedInstanceState.getDouble("lastLat"));
            //mLastLocation.setLongitude(savedInstanceState.getDouble("lastLng"));

            //Bundle bundle = savedInstanceState.getBundle("bundleGoogle");
            //mGoogleApiClient = bundle.getParcelable("mGoogleApiClient");
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record_workout_portrait, container, false);

        MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        startWorkout = view.findViewById(R.id.startWorkout);
        startWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startWorkout(view);
            }
        });

        ImageButton imageButton = view.findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbHandler.updateUserDetails(1, weeklyDistance, weeklyTime, weeklyWorkouts, 100);
                Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                startActivity(intent);
            }
        });

        ImageView imageView2 = view.findViewById(R.id.imageView2);
        imageView2.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent dbmanager = new Intent(getActivity(), AndroidDatabaseManager.class);
                startActivity(dbmanager);
            }
        });

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_LOCATION_REQUEST_CODE);
            return;
        } else {
            mLocationRequest = new LocationRequest();
            //mLocationRequest.setInterval(5000); //5 seconds
            //mLocationRequest.setFastestInterval(3000); //3 seconds
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            //mLocationRequest.setSmallestDisplacement(0.1F); //1/10 meter

            mFusedLocationClient.requestLocationUpdates(mLocationRequest, firstLocationCallback, Looper.myLooper());
        }
    }

    LocationCallback firstLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                Log.i("MapsActivityFirst", "Location: " + location.getLatitude() + " " + location.getLongitude());
                mLastLocation = location;
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();

                //Place current location marker
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                //move map camera
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            }
        }

    };

    LocationCallback updateLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                Log.i("MapsActivityUpdate", "Location: " + location.getLatitude() + " " + location.getLongitude());
                mLastLocation = location;
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));

                beginLat = currentLatitude;
                beginLng = currentLongitude;
                beginMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(currentLatitude, currentLongitude))
                        .title("Begin")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                );
            }
        }

    };

    LocationCallback mLocationCallbackUpdate = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());

                if (mLastLocation.distanceTo(location) > 4.572 && count > 0) {
                    distance += mLastLocation.distanceTo(location);
                    weeklyDistance += distance;
                }

                mLastLocation = location;
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();

                distanceText.setText(df2.format(metersToMiles(distance)));


                dbHandler.addLocation(currentLatitude, currentLongitude, Math.abs(chronometer.getBase() - SystemClock.elapsedRealtime()));
                dbHandler.updateUserDetails(1, weeklyDistance, weeklyTime, weeklyWorkouts, 100);
                //Toast.makeText(getActivity(), "Time: " + Math.abs(chronometer.getBase() - SystemClock.elapsedRealtime()) + " Distance: " + distance, Toast.LENGTH_SHORT).show();
                //createPolyLine();
                addPolyLine();
                count++;
            }
        }

    };

    private void addPolyLine() {
        ArrayList<LatLng> path = dbHandler.getLastWorkoutPath();
        if (path.size() == 2) {
            Log.d("PATH", "Path 1: " + path.get(0) + " Path 2: " + path.get(1));
            if (path.get(0) != path.get(1)) {
                line = mMap.addPolyline(new PolylineOptions().add(path.get(0), path.get(1)).width(5).color(Color.RED));
            }
        }
    }

    private void createPolyLine() {
        ArrayList<LatLng> path = dbHandler.getTotalPath();
        Log.d("PATH", Integer.toString(path.size()));

        if (path.size() >= 2) {
            for(int i = 0; i<path.size()-1; i++){
                line = mMap.addPolyline(new PolylineOptions().add(path.get(i), path.get(i+1)).width(5).color(Color.RED));
                Log.d("ADD POLYLINE", "Attempted to add polyline " + "Path 1: " + path.get(i) + " Path 2: " + path.get(i+1));
            }
            //Log.d("PATH", "Path 1: " + path.get(0) + " Path 2: " + path.get(1));
        }
    }

    private void calculateDistance(){
        ArrayList<LatLng> path = dbHandler.getTotalPath();
        if (path.size() >= 2) {
            distance = 0;
            for(int i = 0; i<path.size()-1; i++){
                Location location1 = new Location("");
                Location location2 = new Location("");
                location1.setLatitude(path.get(i).latitude);
                location1.setLongitude(path.get(i).longitude);
                location2.setLatitude(path.get(i+1).latitude);
                location2.setLongitude(path.get(i+1).longitude);
                if (location1.distanceTo(location2) > 4.572) {
                    distance += location1.distanceTo(location2);
                }
            }
            //Log.d("PATH", "Path 1: " + path.get(0) + " Path 2: " + path.get(1));
            distanceText.setText(df2.format(metersToMiles(distance)));
        }
    }

    public double metersToMiles(double meters) {
        return meters * 0.000621371;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("record_workout");
        getActivity().registerReceiver(receiver,intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
        builder1.setMessage("Failed to connect");
        builder1.setCancelable(false);

        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getActivity().finish();
                        System.exit(0);
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //dbHandler.deleteAllLocation();
        setupMap();

    }

    private void startWorkoutService() {
        getActivity().startService(new Intent(getActivity(), WorkoutService.class));
    }

    public void setupMap() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_LOCATION_REQUEST_CODE);
            return;
        } else {
            if (mGoogleApiClient == null) {
                buildGoogleApiClient();
            }
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);

            mGoogleApiClient.connect();

            if (endLat != 0 && endLng != 0 && !workoutMode) {
                endMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(endLat, endLng))
                        .title("End"));
            }

            if (beginLat != 0 && beginLng != 0) {
                beginMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(beginLat, beginLng))
                        .title("Begin")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                );
                createPolyLine();
                calculateDistance();
            } else {
                dbHandler.deleteAllLocation();
            }

        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void startWorkout(View view) {
        Button startWorkout = getView().findViewById(R.id.startWorkout);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            if (!workoutMode) {
                Toast.makeText(getActivity(), "Workout started!", Toast.LENGTH_SHORT).show();

                dbHandler.deleteAllLocation();
                chronometer.setBase(SystemClock.elapsedRealtime());
                distance = 0;
                stopTime = 0;
                count = 0;
                weeklyWorkouts++;
                chronometer.start();
                mMap.clear();

                LocationRequest getCurrentLocation = new LocationRequest();
                getCurrentLocation.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                mFusedLocationClient.requestLocationUpdates(getCurrentLocation, updateLocationCallback, Looper.myLooper());

                mLocationRequestUpdate = new LocationRequest();
                mLocationRequestUpdate.setInterval(15000); //5 seconds
                mLocationRequestUpdate.setFastestInterval(10000); //3 seconds
                mLocationRequestUpdate.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                mLocationRequestUpdate.setSmallestDisplacement(4.572F); //1/10 meter
                mFusedLocationClient.requestLocationUpdates(mLocationRequestUpdate, mLocationCallbackUpdate, Looper.myLooper());

                startWorkout.setText("Stop Workout");
                startWorkout.setBackgroundColor(getResources().getColor(R.color.googleRed));
                distanceText.setText("0");
                workoutMode = true;
            } else {
                stopTime = chronometer.getBase() - SystemClock.elapsedRealtime();
                chronometer.stop();

                LocationRequest getLocation = new LocationRequest();
                getLocation.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                mFusedLocationClient.removeLocationUpdates(mLocationCallbackUpdate);
                mFusedLocationClient.requestLocationUpdates(getLocation, mLocationCallbackUpdate, Looper.myLooper());


                //Toast.makeText(getActivity(), "Time: " + Math.abs(chronometer.getBase() - SystemClock.elapsedRealtime()), Toast.LENGTH_SHORT).show();

                endLat = currentLatitude;
                endLng = currentLongitude;
                endMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(currentLatitude, currentLongitude))
                        .title("End"));
                mFusedLocationClient.removeLocationUpdates(mLocationCallbackUpdate);
                dbHandler.updateUserDetails(1, weeklyDistance, weeklyTime, weeklyWorkouts, 100);
                startWorkout.setText("Start Workout");
                startWorkout.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                workoutMode = false;
                Toast.makeText(getActivity(), "Workout done!", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_LOCATION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupMap();

                } else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                    builder1.setMessage("Please enable Location permission.");
                    builder1.setCancelable(false);

                    builder1.setPositiveButton(
                            "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    getActivity().finish();
                                    System.exit(0);
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
                return;
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
