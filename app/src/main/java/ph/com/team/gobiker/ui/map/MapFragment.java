package ph.com.team.gobiker.ui.map;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApi;
import com.google.maps.errors.ApiException;
import com.google.maps.errors.NotFoundException;
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
import java.util.Objects;

import ph.com.team.gobiker.NavActivity;
import ph.com.team.gobiker.R;
import ph.com.team.gobiker.maputils.MapService;
import ph.com.team.gobiker.maputils.MapStateManager;
import ph.com.team.gobiker.settings.SettingsService;
import ph.com.team.gobiker.user.User;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener, LocationListener {

    final static Handler handler = new Handler();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static LatLng navTo;
    private static LatLng navFrom;
    private static Location currentLocation;
    private static String currentUserUID;
    private static double distanceTotal;
    private static double distanceRemain;
    private static DecimalFormat df = new DecimalFormat("0.00");
    private static boolean isDirectionalTravel = false;
    private static boolean isFreeTravel = false;
    private static boolean isBehindStartPoint = false;
    private static boolean isPaused = false;
    private static double prevDistance = 0;
    private static Double lat1 = null;
    private static Double lon1 = null;
    private static Double lat2 = null;
    private static Double lon2 = null;
    private static double pinDirectionDistance;
    private MapViewModel mViewModel;
    private GoogleMap mMap;
    private boolean mPermissionDenied = false;
    private LocationManager locationManager;
    private ExtendedFloatingActionButton startNav;
    private ExtendedFloatingActionButton startFreeRide;
    private ExtendedFloatingActionButton pauseFreeRide;
    private ExtendedFloatingActionButton tipDistanceRemain;
    private TextView infoSpeed;
    private TextView infoCalories;
    private TextView infoTime;
    private TextView infoDistance;
    private User currentUserData;
    private double speed;
    private double calories;
    private int secondsTime = 0;
    private int toggleChecker = 0;
    private List<CurrentLocation> allAvailableLocations;
    private static int rideChoice = 0;
    private static Runnable startTimerRunnable;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        tipDistanceRemain = v.findViewById(R.id.info_distanceRemain);
        startNav = v.findViewById(R.id.startNav);
        startNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isDirectionalTravel) {
                    final WaitForLocationRunner executor = new WaitForLocationRunner();
                    executor.execute();
                } else {
                    startNav.setText("Add Direction");
                    isDirectionalTravel = false;
                    startFreeRide.setVisibility(View.VISIBLE);
                    tipDistanceRemain.setVisibility(View.GONE);
                    mMap.clear();
                }
            }
        });
        pauseFreeRide = v.findViewById(R.id.pauseFree);
        startFreeRide = v.findViewById(R.id.startFree);
        startFreeRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFreeTravel) {
                    chooseSingleOrGroup();
                } else if (isFreeTravel) {
                    toggleSingleFreeRide();
                }
            }
        });
        infoSpeed = v.findViewById(R.id.nav_info_speed);
        infoCalories = v.findViewById(R.id.nav_info_calories);
        infoDistance = v.findViewById(R.id.nav_info_distance);
        infoTime = v.findViewById(R.id.nav_info_time);

        distanceTotal = 0;
        distanceRemain = 0;

        allAvailableLocations = new ArrayList<CurrentLocation>();
        setupMapIfNeeded();
        setUpLocationManager();
        runTimer();
        setupUser();
        pauseFreeRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isPaused) {
                    handler.removeCallbacks(startTimerRunnable);
                    pauseFreeRide.setText("Resume");
                    startFreeRide.setVisibility(View.GONE);
                    isPaused = true;
                } else {
                    runTimer();
                    pauseFreeRide.setText("Pause");
                    startFreeRide.setVisibility(View.VISIBLE);
                    isPaused = false;
                }
            }
        });
        return v;
    }

    private void toggleSingleFreeRide() {
        isFreeTravel = !isFreeTravel;
        Toast.makeText(getContext(), isFreeTravel ? "Starting free ride..." : "Stopping free ride...", Toast.LENGTH_SHORT).show();
        if (isFreeTravel) {
            secondsTime = 0;
            calories = 0;
            speed = 0;
            startNav.setVisibility(View.GONE);
            startFreeRide.setText("Stop");
            pauseFreeRide.setVisibility(View.VISIBLE);
        } else {
            startNav.setVisibility(View.VISIBLE);
            startFreeRide.setText("Start");
            pauseFreeRide.setVisibility(View.GONE);
        }
    }


    private void chooseSingleOrGroup() {
        rideChoice = 0;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Go Biker").setMessage("Pick a ride choice.").setPositiveButton("Single", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                toggleSingleFreeRide();
            }
        }).setNegativeButton("Group", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                chooseCreateOrJoin();
            }
        }).setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();
        Button positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positive.setTextColor(Color.BLACK);
        Button negative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negative.setTextColor(Color.BLACK);
    }

    private void chooseCreateOrJoin() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Join a room or create a new room?").setPositiveButton("Create Room", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivityForResult(new Intent(getContext(), CreateRoomForGroupRide.class), 2);
            }
        }).setNegativeButton("Join Room", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
        Button positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positive.setTextColor(Color.BLACK);
        Button negative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negative.setTextColor(Color.BLACK);
    }

    private void getAllLocationAvailable() {
        DatabaseReference locRef = FirebaseDatabase.getInstance().getReference().child("Location");
        locRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d("firebase", "data added on user: " + snapshot.getValue());
                if (!snapshot.getKey().equals(currentUserUID)) {
                    CurrentLocation newlyAddedUserLocation = snapshot.getValue(CurrentLocation.class);
                    allAvailableLocations.add(newlyAddedUserLocation);
                    mMap.addMarker(new MarkerOptions().position(new LatLng(newlyAddedUserLocation.getLocation().getLat(), newlyAddedUserLocation.getLocation().getLng())).icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_pedal_bike_black_18dp)));
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void setupUser() {
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            currentUserUID = userId;
            currentUserData = new User(userId);
            FirebaseDatabase.getInstance().getReference().child("Users").child(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    currentUserData.setFullname(dataSnapshot.child("fullname").getValue().toString());
                    currentUserData.setGender(dataSnapshot.child("gender").getValue().toString());
                    currentUserData.setAge(Integer.parseInt(dataSnapshot.child("age").getValue().toString()));
                    currentUserData.setHeight(Float.parseFloat(dataSnapshot.child("height").getValue().toString()));
                    currentUserData.setWeight(Float.parseFloat(dataSnapshot.child("weight").getValue().toString()));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
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

        //separate threads for shareLocation and showing allLocation
        showAllLocationAvailable();
        getAllLocationAvailable();
    }

    private void showAllLocationAvailable() {

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
        MapStateManager mgr = new MapStateManager(requireContext());
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
        pinDirectionDistance = 0;
        List<LatLng> path = new ArrayList();
        if (result.routes != null && result.routes.length > 0) {
            DirectionsRoute route = result.routes[0];
            if (route.legs != null) {
                for (int i = 0; i < route.legs.length; i++) {
                    pinDirectionDistance = route.legs[i].distance.inMeters;
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
        if (currentLocation == null) currentLocation = location;
        speed = 0;
        if (!isPaused) {
            if (isFreeTravel || isDirectionalTravel) {
                if (toggleChecker == 0) {
                    lat1 = location.getLatitude();
                    lon1 = location.getLongitude();
                } else if ((toggleChecker % 2) != 0) {
                    lat2 = location.getLatitude();
                    lon2 = location.getLongitude();
                    distanceTotal += MapService.distanceBetweenTwoPoint(lat1, lon1, lat2, lon2);
                } else if ((toggleChecker % 2) == 0) {
                    lat1 = location.getLatitude();
                    lon1 = location.getLongitude();
                    distanceTotal += MapService.distanceBetweenTwoPoint(lat2, lon2, lat1, lon1);
                }
                toggleChecker++;

                if (distanceTotal / 1000 >= 1) {
                    infoDistance.setText(df.format(distanceTotal / 1000) + " km");
                } else {
                    infoDistance.setText(df.format(distanceTotal) + " m");
                }
            }
            if (isFreeTravel) {
                moveCamToLocation();
            /*
            needs fixing
             */


                /**
                 * speed calculation
                 */
                float distanceTraveled = currentLocation == null ? 0 : currentLocation.distanceTo(location);
                distanceTotal += currentLocation != null ? distanceTraveled : 0;
                currentLocation = location;
                speed = distanceTraveled == 0 ? 0 : location.hasSpeed() ? location.getSpeed() : 0;
                if (SettingsService.isKphFlag()) {
                    speed = speed * 18 / 5;
                    infoSpeed.setText(df.format(speed) + " kph");
                } else {
                    infoSpeed.setText(df.format(speed) + " m/s ");
                }
                Log.d("MapLocationManager", "speed: ");
            } else if (isDirectionalTravel) {
                Log.d("map", "still on directional");
                moveCamToLocation();
                Log.d("startNav", "isbehind? :" + isBehindStartPoint);

                if (isBehindStartPoint) {
                    distanceRemain = MapService.getDistanceFromStartLocation(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), navFrom) * -1;
                    if (distanceRemain >= -60) {
                        isBehindStartPoint = false;
                        Toast.makeText(getContext(), "You made it to the start. You're on your way!", Toast.LENGTH_SHORT).show();
                    }
                    if (distanceRemain / 1000 <= -1) {
                        tipDistanceRemain.setText(df.format(distanceRemain / 1000) + " km");
                    } else {
                        tipDistanceRemain.setText(df.format(distanceRemain) + " m");
                    }
                } else {
                    distanceRemain = MapService.getDistanceFromStartLocation(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), navTo);
                    if (distanceRemain / 1000 >= 1) {
                        tipDistanceRemain.setText(df.format(distanceRemain / 1000) + " km");
                    } else {
                        tipDistanceRemain.setText(df.format(distanceRemain) + " m");
                    }
                    if (distanceRemain <= 20) {
                        finishRide();
                        //move function to reset
                        startNav.setText("Add Direction");
                        isDirectionalTravel = false;
                        startFreeRide.setVisibility(View.VISIBLE);
                        mMap.clear();
                    }
                }

            }
            new SendLocationAsync(new MyLocation(currentLocation.getLatitude(), currentLocation.getLongitude()), currentUserUID).execute();
        }


    }

    private void finishRide() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("You have reached your destination. Details: ").setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).setPositiveButton("OK", null);
        AlertDialog dialog = builder.create();
        dialog.setTitle("FINISH");
        dialog.show();
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
                navFrom = (LatLng) data.getExtras().get("navFrom");
                navTo = (LatLng) data.getExtras().get("navTo");
                Log.d("marker ", "navFrom: " + navFrom);
                Log.d("marker ", "navTo: " + navTo);
                tipDistanceRemain.setVisibility(View.VISIBLE);
                startNavRide();
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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);
            Log.d("MapLocationManager", "location Manager started");
        }
    }

    private void moveCamToLocation() {
        if (getActivity() != null) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
            } else {
                // Permission to access the location is missing. Show rationale and request permission
                PermissionUtils.requestPermission((AppCompatActivity) getContext(), LOCATION_PERMISSION_REQUEST_CODE,
                        Manifest.permission.ACCESS_FINE_LOCATION, true);
                return;
            }
            if (mMap != null) {
                currentLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), false));
                if (currentLocation != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 17));
                }
            }
        }
    }

    private void runTimer() {
        startTimerRunnable = new Runnable() {
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
                    if (prevDistance != distanceTotal) {
                        calories += MapService.handleCaloriesComputation(speed, currentUserData.getWeight());
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
        };
        handler.post(startTimerRunnable);
    }

    private void startNavRide() {
        //hide other button
        detectLocationToStartNav();
        DirectionsResult res;
        try {
            res = DirectionsApi.newRequest(NavActivity.context)
                    .origin(new com.google.maps.model.LatLng(navFrom.latitude, navFrom.longitude))
                    .destination(new com.google.maps.model.LatLng(navTo.latitude, navTo.longitude))
                    .avoid(DirectionsApi.RouteRestriction.TOLLS, DirectionsApi.RouteRestriction.FERRIES)
                    .await();
            drawDirection(res);
            mMap.addMarker(new MarkerOptions().position(navFrom).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(pinDirectionDistance + " m")).showInfoWindow();
            mMap.addMarker(new MarkerOptions().position(navTo).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title("END"));
            isDirectionalTravel = true;
        } catch (NotFoundException e) {
            Toast.makeText(getContext(), "Route not found", Toast.LENGTH_SHORT).show();
            Log.d("marker", e.getMessage());
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error on setting direction", Toast.LENGTH_SHORT).show();
            Log.d("marker", e.getMessage());
        }

        if (isDirectionalTravel) {
            startNav.setText("Stop Nav");
            startFreeRide.setVisibility(View.GONE);
        }


    }

    private void detectLocationToStartNav() {
        double d = MapService.distanceBetweenTwoPoint(navFrom.latitude, navFrom.longitude, currentLocation.getLatitude(), currentLocation.getLongitude());
        if (d > 10) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("You are currently " + d + " meters away from starting point. Go near to the start point to start the stats and navigation.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            Button positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            positive.setTextColor(Color.BLACK);
            isBehindStartPoint = true;
        } else {
            isBehindStartPoint = false;
        }
        Log.d("startNav", "isbehind init? :" + isBehindStartPoint);
        Log.d("marker", "distance: " + d);
    }

    private void shareLocation() {
    }

    private void showAllLocation() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    private class WaitForLocationRunner extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog;


        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            if (currentLocation != null) {
                startActivityForResult(new Intent(getContext(), NavigationStartActivity.class).putExtra("currentLocation", currentLocation), 1);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while (currentLocation == null) {

            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(getContext(),
                    "GoBiker",
                    "Getting your location");
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    WaitForLocationRunner.this.cancel(true);
                }
            });
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(true);
            moveCamToLocation();
        }

        @Override
        protected void onCancelled() {
            progressDialog.dismiss();
        }
    }
}