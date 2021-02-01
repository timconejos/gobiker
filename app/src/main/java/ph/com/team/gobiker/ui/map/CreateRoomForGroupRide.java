package ph.com.team.gobiker.ui.map;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import ph.com.team.gobiker.R;

public class CreateRoomForGroupRide extends AppCompatActivity {

    private AppCompatAutoCompleteTextView createRoomPrivacy;
    private AppCompatAutoCompleteTextView createRoomGroup;
    private TextInputLayout createRoomGroupLayout;
    private Button createRoomSubmit;
    private DatabaseReference groups = FirebaseDatabase.getInstance().getReference().child("Users").child("eS11aHziS3eGBMTihFPOh0lEfWS2"/*FirebaseAuth.getInstance().getUid()*/).child("groupsJoined");
    private DatabaseReference groupList = FirebaseDatabase.getInstance().getReference().child("Groups");
    private String TAG = "createroom";
    private ArrayList<String> groupsJoined;
    private ArrayList<String> groupKeys;
    private HashMap<String, String> groupsFromFirebase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room_for_group_ride);
        groupsJoined = new ArrayList<>();
        groupKeys = new ArrayList<>();
        initializeGroups();
        createRoomGroup = findViewById(R.id.create_room_groups);
        createRoomGroupLayout = findViewById(R.id.create_room_groups_layout);
        createRoomPrivacy = findViewById(R.id.create_room_privacy);
        createRoomPrivacy.setAdapter(ArrayAdapter.createFromResource(this, R.array.create_room_options, android.R.layout.simple_spinner_dropdown_item));
        createRoomPrivacy.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                groupKeys = new ArrayList<>();
                groupsJoined = new ArrayList<>();
                groups.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot snap : snapshot.getChildren()){
                            groupKeys.add(snap.getKey());
                            groupsJoined.add(groupsFromFirebase.get(snap.getKey()));
                            Log.d(TAG, groupsJoined.toString());
                        }
                        createRoomGroupLayout.setVisibility(View.VISIBLE);
                        createRoomGroup.setAdapter(new ArrayAdapter<String>(CreateRoomForGroupRide.this, android.R.layout.simple_spinner_dropdown_item, groupsJoined));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
        createRoomSubmit = findViewById(R.id.create_room_submit);
        createRoomSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }

    private void initializeGroups() {
        groupsFromFirebase = new HashMap<>();
        groupList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snap : dataSnapshot.getChildren()){
                    groupsFromFirebase.put(snap.getKey(), snap.child("group_name").getValue().toString());
                }
                Log.d(TAG, groupsFromFirebase.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
