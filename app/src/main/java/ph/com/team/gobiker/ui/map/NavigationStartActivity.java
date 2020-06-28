package ph.com.team.gobiker.ui.map;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;

import ph.com.team.gobiker.R;

public class NavigationStartActivity extends AppCompatActivity {

    final int START_DIRECTION_REQUEST_CODE = 1;
    final int END_DIRECTION_REQUEST_CODE = 2;

    private EditText startPoint;
    private EditText endPoint;
    private Button letsGoBtn;
    static Place startPlace;
    static Place endPlace;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_nav);
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));

        List<Place.Field> fieldStart = Arrays.asList(Place.Field.ID, Place.Field.NAME);
        List<Place.Field> fieldEnd = Arrays.asList(Place.Field.ID, Place.Field.NAME);

        startPoint = findViewById(R.id.startPointNav);
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
                Intent sendBackIntent = new Intent();
                sendBackIntent.putExtra("navFrom", startPlace);
                sendBackIntent.putExtra("navTo", endPlace);
                setResult(-1, sendBackIntent);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == -1){
            switch (requestCode) {
                case START_DIRECTION_REQUEST_CODE:
                    startPlace = Autocomplete.getPlaceFromIntent(data);
                    startPoint.setText(startPlace.getName());
                    break;
                case END_DIRECTION_REQUEST_CODE:
                    endPlace = Autocomplete.getPlaceFromIntent(data);
                    endPoint.setText(endPlace.getName());
                    break;
                default:
                    break;
            }
        }
    }
}
