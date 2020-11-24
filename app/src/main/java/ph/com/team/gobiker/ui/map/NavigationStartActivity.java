package ph.com.team.gobiker.ui.map;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;

import ph.com.team.gobiker.R;

public class NavigationStartActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    final int START_DIRECTION_REQUEST_CODE = 1;
    final int END_DIRECTION_REQUEST_CODE = 2;

    private EditText startPoint;
    private EditText endPoint;
    private Button letsGoBtn;

    static LatLng startLatLng;
    static LatLng endLatLng;

    private Marker start;
    private Marker end;

    private static Location currentLocation;
    private Place startPlace;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_nav);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.nav_map);
            mapFragment.getMapAsync(this);

        currentLocation = (Location)getIntent().getExtras().get("currentLocation");
        Log.d("marker ", "location: " + currentLocation);
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        PlacesClient placesClient = Places.createClient(this);
        List<Place.Field> fieldStart = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
        List<Place.Field> fieldEnd = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

        startPoint = findViewById(R.id.startPointNav);
        startPoint.setText(R.string.current_location);
        startPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN, fieldStart)
                        .build(getApplicationContext());
                startActivityForResult(intent, START_DIRECTION_REQUEST_CODE);
            }
        });

        endPoint = findViewById(R.id.endPointNav);
        endPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN, fieldEnd)
                        .build(getApplicationContext());
                startActivityForResult(intent, END_DIRECTION_REQUEST_CODE);
            }
        });

        letsGoBtn = findViewById(R.id.letsgo_btn);
        letsGoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(endLatLng != null) {
                    Intent sendBackIntent = new Intent();
                    sendBackIntent.putExtra("navFrom", startLatLng);
                    sendBackIntent.putExtra("navTo", endLatLng);
                    setResult(-1, sendBackIntent);
                    finish();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Drag the red marker or enter an end location on top", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            Place startPlace, endPlace;
            switch (requestCode) {
                case START_DIRECTION_REQUEST_CODE:
                    startPlace = Autocomplete.getPlaceFromIntent(data);
                    startPoint.setText(startPlace.getName());
                    startPlace.getId();
                    Log.d("getPlace", "splace: " + startPlace.getLatLng());
                    if (startPlace.getLatLng() != null) {
                        startLatLng = startPlace.getLatLng();
                        start.setPosition(startLatLng);
                        startPoint.setText(startPlace.getName());
                    }
                    break;
                case END_DIRECTION_REQUEST_CODE:
                    endPlace = Autocomplete.getPlaceFromIntent(data);
                    endPoint.setText(endPlace.getName());
                    Log.d("getPlace", "eplace: " + endPlace.getLatLng());
                    if (endPlace.getLatLng() != null) {
                        endLatLng = endPlace.getLatLng();
                        end.setPosition(endLatLng);
                        endPoint.setText(endPlace.getName());
                    }
                    break;
                default:
                    break;
            }
            updateCamera();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        start = mMap.addMarker(new MarkerOptions().position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())).draggable(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title("Start"));
        startLatLng = start.getPosition();
        end = mMap.addMarker(new MarkerOptions().position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude() + 0.0005)).draggable(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title("End"));
        updateCamera();
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                Log.d("marker drag", marker.getId() + " start: " + marker.getPosition());
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                Log.d("marker drag", marker.getId() + " drag: " + marker.getPosition());
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                Log.d("marker drag", marker.getId() + " end: " + marker.getPosition());
                if(marker.getId().equals("m0")) {
                    startLatLng = marker.getPosition();
                    startPoint.setText(marker.getPosition().toString().replace("lat/lng:", "").trim());
                }
                else{
                    endLatLng = marker.getPosition();
                    endPoint.setText(marker.getPosition().toString().replace("lat/lng:", "").trim());
                }
                updateCamera();
            }
        });
    }

    void updateCamera(){
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        b.include(start.getPosition());
        b.include(end.getPosition());
        LatLngBounds bounds = b.build();
        int width = getResources().getDisplayMetrics().widthPixels;
        int padding = (int) (width * 0.10);
        CameraUpdate cu  = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
    }
}
