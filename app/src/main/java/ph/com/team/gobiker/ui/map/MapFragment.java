package ph.com.team.gobiker.ui.map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeocodingApiRequest;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.TravelMode;
import com.google.maps.model.Unit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ph.com.team.gobiker.MainActivity;
import ph.com.team.gobiker.NavActivity;
import ph.com.team.gobiker.R;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener,  ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener, LocationListener {

    private MapViewModel mViewModel;
    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private LocationManager locationManager;
    private FloatingActionButton startNav;

    private static Place navTo;
    private static Place navFrom;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_map, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        startNav = v.findViewById(R.id.startNav);
        startNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getContext(), NavigationStartActivity.class), 1);
            }
        });
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MapViewModel.class);
        // TODO: Use the ViewModel
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnMyLocationButtonClickListener(this);
        // Add a marker in Sydney and move the camera
        LatLng mabuhay = new LatLng(14.320080, 120.985050);
        mMap.addMarker(new MarkerOptions().position(mabuhay).title("Marker in Mabuhay"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(morayta, 18));
        enableMyLocation();
        mMap.setOnPolylineClickListener(this);
        mMap.setOnPolygonClickListener(this);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(getContext(), "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        showDirection();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
    }

    private void enableMyLocation() {
        // [START maps_check_location_permission]
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), false));
                if(location != null){
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));
                }
            }
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission((AppCompatActivity) getContext(), LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
        // [END maps_check_location_permission]
    }

    // [START maps_check_location_permission_result]
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Permission was denied. Display an error message
            // [START_EXCLUDE]
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
            // [END_EXCLUDE]
        }
    }
    // [END maps_check_location_permission_result]


    @Override
    public void onResume() {
        super.onResume();
        if(mPermissionDenied){
            showMissingPermissionError();
            mPermissionDenied = false;
        }

    }


    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getChildFragmentManager(), "dialog");
    }

    private void showDirection(){
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

            if (route.legs !=null) {
                for(int i=0; i<route.legs.length; i++) {
                    DirectionsLeg leg = route.legs[i];
                    if (leg.steps != null) {
                        for (int j=0; j<leg.steps.length;j++){
                            DirectionsStep step = leg.steps[j];
                            if (step.steps != null && step.steps.length >0) {
                                for (int k=0; k<step.steps.length;k++){
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
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),18));
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
        if (circle != null){
            double radius = circle.getRadius();
            double scale = radius / 500;
            zoomLevel =(int) (16 - Math.log(scale) / Math.log(2));
        }
        return zoomLevel;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == -1){
            if(requestCode == 1){
                navFrom = (Place) data.getExtras().get("navFrom");
                navTo = (Place) data.getExtras().get("navTo");
            }
        }
    }
}
