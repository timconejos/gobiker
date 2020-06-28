package ph.com.team.gobiker.ui.map;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.TravelMode;
import com.google.maps.model.Unit;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ph.com.team.gobiker.NavActivity;
import ph.com.team.gobiker.R;
import ph.com.team.gobiker.maputils.MapStateManager;
import ph.com.team.gobiker.user.User;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener, LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static Place navTo;
    private static Place navFrom;
    private static Location currentLocation;
    private static float distanceTotal;
    private static DecimalFormat df = new DecimalFormat("0.00");
    private static boolean isDirectionalTravel = false;
    private static boolean isFreeTravel = false;
    private MapViewModel mViewModel;
    private GoogleMap mMap;
    private boolean mPermissionDenied = false;
    private LocationManager locationManager;
    private FloatingActionButton startNav;
    private FloatingActionButton startFreeRide;
    private TextView infoSpeed;
    private TextView infoCalories;
    private TextView infoTime;
    private TextView infoDistance;
    private User currentUserData;
    private double speed;
    private double calories;
    private static double prevDistance = 0;
    private int secondsTime = 0;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        startNav = v.findViewById(R.id.startNav);
        startNav.setOnClickListener(view -> startActivityForResult(new Intent(getContext(), NavigationStartActivity.class), 1));
        startFreeRide = v.findViewById(R.id.startFree);
        startFreeRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isFreeTravel = !isFreeTravel;
                if (isFreeTravel) {
                    secondsTime = 0;
                    calories = 0;
                    speed = 0;
                }
                Toast.makeText(getContext(), isFreeTravel ? "Starting free ride..." : "Stopping free ride...", Toast.LENGTH_SHORT).show();
            }
        });
        infoSpeed = v.findViewById(R.id.nav_info_speed);
        infoCalories = v.findViewById(R.id.nav_info_calories);
        infoDistance = v.findViewById(R.id.nav_info_distance);
        infoTime = v.findViewById(R.id.nav_info_time);

        distanceTotal = 0;
        setupUser();
        setupMapIfNeeded();
        setUpLocationManager();
        runTimer();
        return v;
    }

    private void setupUser() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        currentUserData = new User(userId);
        FirebaseDatabase.getInstance().getReference().child("Users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentUserData.setFullname(dataSnapshot.child("fullname").getValue().toString());
                currentUserData.setGender(dataSnapshot.child("gender").getValue().toString());
                currentUserData.setAge(21);//Integer.parseInt(dataSnapshot.child("age").getValue().toString())
                currentUserData.setHeight(155);//Float.parseFloat(dataSnapshot.child("height").getValue().toString())
                currentUserData.setWeight(155);//Float.parseFloat(dataSnapshot.child("weight").getValue().toString())
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MapViewModel.class);
        // TODO: Use the ViewModel
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapStateManager mgr = new MapStateManager(getContext());
        CameraPosition position = mgr.getSavedCameraPosition();
        mMap = googleMap;
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnMyLocationButtonClickListener(this);
        // Add a marker in Sydney and move the camera
//        LatLng mabuhay = new LatLng(14.320080, 120.985050);
//        mMap.addMarker(new MarkerOptions().position(mabuhay).title("Marker in Mabuhay"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(morayta, 18));
        if (position != null) {
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
            mMap.moveCamera(update);
            mMap.setMapType(mgr.getSavedMapType());
        } else {
            moveCamToLocation();
        }

        mMap.setOnPolylineClickListener(this);
        mMap.setOnPolygonClickListener(this);

        //fix
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

    }

    @Override
    public boolean onMyLocationButtonClick() {
        //showDirection();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }
        if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            moveCamToLocation();
        } else {
            // Permission was denied. Display an error message
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;

        }
    }


    @Override
    public void onPause() {
        super.onPause();
        MapStateManager mgr = new MapStateManager(getContext());
        mgr.saveMapState(mMap);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPermissionDenied) {
            showMissingPermissionError();
            mPermissionDenied = false;
        } else {
            setupMapIfNeeded();
            moveCamToLocation();
        }

    }


    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getChildFragmentManager(), "dialog");
    }

    private void showDirection() {
        DirectionsResult result = new DirectionsResult();
        try {
            result =
                    DirectionsApi.newRequest(NavActivity.context)
                            .mode(TravelMode.DRIVING)
                            .avoid(
                                    DirectionsApi.RouteRestriction.TOLLS,
                                    DirectionsApi.RouteRestriction.FERRIES)
                            .units(Unit.METRIC)
                            .origin(new com.google.maps.model.LatLng(14.320080, 120.985050))
                            .destination(new com.google.maps.model.LatLng(14.307661, 120.991594))
                            //.origin("Sydney")
                            //.destination("Melbourne")
                            .await();


        } catch (ApiException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        drawDirection(result);
    }

    private void drawDirection(DirectionsResult result) {
        List<LatLng> path = new ArrayList();
        if (result.routes != null && result.routes.length > 0) {
            DirectionsRoute route = result.routes[0];

            if (route.legs != null) {
                for (int i = 0; i < route.legs.length; i++) {
                    DirectionsLeg leg = route.legs[i];
                    if (leg.steps != null) {
                        for (int j = 0; j < leg.steps.length; j++) {
                            DirectionsStep step = leg.steps[j];
                            if (step.steps != null && step.steps.length > 0) {
                                for (int k = 0; k < step.steps.length; k++) {
                                    DirectionsStep step1 = step.steps[k];
                                    EncodedPolyline points1 = step1.polyline;
                                    if (points1 != null) {
                                        //Decode polyline and add points to list of route coordinates
                                        List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                        for (com.google.maps.model.LatLng coord1 : coords1) {
                                            path.add(new LatLng(coord1.lat, coord1.lng));
                                        }
                                    }
                                }
                            } else {
                                EncodedPolyline points = step.polyline;
                                if (points != null) {
                                    //Decode polyline and add points to list of route coordinates
                                    List<com.google.maps.model.LatLng> coords = points.decodePath();
                                    for (com.google.maps.model.LatLng coord : coords) {
                                        path.add(new LatLng(coord.lat, coord.lng));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (path.size() > 0) {
            PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.GREEN).width(5);
            mMap.addPolyline(opts);
        }
    }

    @Override
    public void onPolygonClick(Polygon polygon) {

    }

    @Override
    public void onPolylineClick(Polyline polyline) {

    }

    @Override
    public void onLocationChanged(Location location) {
        speed = 0;
//        if (currentLocation != null)
//            speed = Math.sqrt(
//                    Math.pow(location.getLongitude() - currentLocation.getLongitude(), 2)
//                            + Math.pow(location.getLatitude() - currentLocation.getLatitude(), 2)
//            ) / (location.getTime() - currentLocation.getTime());
//        if (location.hasSpeed())
//            speed = location.getSpeed();

/*        if (currentLocation != null) {
            double elapsedTime = (location.getTime() - currentLocation.getTime()) / 1_000; // Convert milliseconds to seconds
            speed = currentLocation.distanceTo(location) / elapsedTime;
        }*/
        if (isFreeTravel) {
            float distanceTraveled = currentLocation == null ? 0 : currentLocation.distanceTo(location);
            moveCamToLocation();

            distanceTotal += currentLocation != null ? distanceTraveled : 0;
            currentLocation = location;
            speed = distanceTraveled == 0 ? 0 : location.hasSpeed() ? location.getSpeed() : 0;
            infoSpeed.setText(df.format(speed) + " m/s ");

            if (distanceTotal / 1000 >= 1) {
                infoDistance.setText(df.format(distanceTotal / 1000) + " km");
            } else {
                infoDistance.setText(df.format(distanceTotal) + " m");
            }


            Log.d("MapLocationManager", "location updated ");
        } else {
            //location
        }
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

    public int getZoomLevel(Circle circle) {
        int zoomLevel = 0;
        if (circle != null) {
            double radius = circle.getRadius();
            double scale = radius / 500;
            zoomLevel = (int) (16 - Math.log(scale) / Math.log(2));
        }
        return zoomLevel;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == -1) {
            if (requestCode == 1) {
                navFrom = (Place) data.getExtras().get("navFrom");
                navTo = (Place) data.getExtras().get("navTo");
            }
        }
    }

    private void setupMapIfNeeded() {
        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    private void setUpLocationManager() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
            Log.d("MapLocationManager", "location Manager started");
        }
    }

    private void moveCamToLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                currentLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), false));
                if (currentLocation != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 17));
                }
            }
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission((AppCompatActivity) getContext(), LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
    }

    private void runTimer() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours = secondsTime / 3600;
                int minutes = (secondsTime % 3600) / 60;
                int secs = secondsTime % 60;
                String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);
                infoTime.setText(time);
                if (isFreeTravel || isDirectionalTravel) {
                    secondsTime++;
                    Log.d("MapLocationManager", "ddiff: " + prevDistance + " - dtotal: " + distanceTotal);
                    if(prevDistance != distanceTotal) {
                        handleCaloriesComputation();
                        if (calories / 1000 >= 1) {
                            infoCalories.setText(df.format(calories / 1000) + " kCal");
                        } else {
                            infoCalories.setText(df.format(calories) + " cal");
                        }
                    }
                    prevDistance = distanceTotal;
                }
                handler.postDelayed(this, 1000);

            }
        });
    }

    private void handleCaloriesComputation() {
        double msToMph = 2.23694;
        double met = 0;
        if(speed <= 5.5){
            met = 3.5;
        }
        else if(5.5 < speed && speed <= 9.4){
            met = 4;
        }
        else if(9.4 < speed && speed < 10){
            met = 5.8;
        }
        else if(10 <= speed && speed <= 11.9){
            met = 6.8;
        }
        else if(12 <= speed && speed <= 13.9){
            met = 8;
        }
        else if(14 <= speed && speed <= 15.9){
            met = 10;
        }
        else if(16 <= speed && speed <= 19){
            met = 12;
        }
        else if(speed > 19){
            met = 15.8;
        }
        calories += (((met * currentUserData.getWeight() * 3.5)/200)/60);
    }

}
