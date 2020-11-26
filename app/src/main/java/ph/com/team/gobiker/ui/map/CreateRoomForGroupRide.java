package ph.com.team.gobiker.ui.map;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import ph.com.team.gobiker.R;

public class CreateRoomForGroupRide extends AppCompatActivity {
    private Spinner createRoomPrivacy;
    private final ArrayList<String> PRIVACIES = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room_for_group_ride);
        PRIVACIES.add("Public");
        PRIVACIES.add("Private");
        PRIVACIES.add("Group");

        createRoomPrivacy = findViewById(R.id.create_room_privacy);
        createRoomPrivacy.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, PRIVACIES));
    }
}
