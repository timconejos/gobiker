package ph.com.team.gobiker.ui.dashboard;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import ph.com.team.gobiker.R;

public class ViewOthersProfile extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView userProfName, userEmail, userPhone , numRides;
    private CircleImageView userProfileImage;
    private DatabaseReference profileUserRef;
    private String profileId;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_profile);
        profileId = getIntent().getExtras().get("profileId").toString();

        getProfileDetails();

        viewPager = findViewById(R.id.profilecontainer);
        tabLayout = findViewById(R.id.profilenavbar);

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
    }


    private void getProfileDetails(){

        mAuth = FirebaseAuth.getInstance();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(profileId);

        userProfName = findViewById(R.id.profile_name);
        userEmail = findViewById(R.id.profile_email);
        userPhone = findViewById(R.id.profile_phone);
        numRides = findViewById(R.id.profile_rides);
        userProfileImage = findViewById(R.id.profile_pic);

        profileUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                    String myBike = dataSnapshot.child("bike").getValue().toString();

                    String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myEmail = dataSnapshot.child("email").getValue().toString();
                    String myPhone = dataSnapshot.child("phone").getValue().toString();

                    if (dataSnapshot.hasChild("profileimage")){
                        String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(getApplicationContext()).load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);
                    }
                    else{
                        Picasso.with(getApplicationContext()).load(R.drawable.profile).into(userProfileImage);
                    }

                    userProfName.setText(myProfileName);
                    userEmail.setText(myEmail);

                    if (dataSnapshot.child("check_phone").getValue().toString().equals("true")){
                        userPhone.setText(myPhone);
                    }
                    else{
                        userPhone.setText("");
                    }

                    if (dataSnapshot.hasChild("active_ride")){
                        if (myBike.equals("true") && dataSnapshot.child("active_ride").getValue().toString().equals("Bicycle")) {

                            if (dataSnapshot.hasChild("bike_number_of_rides")){
                                if (dataSnapshot.child("bike_number_of_rides").getValue().toString().equals(""))
                                    numRides.setText("No Rides yet");
                                else
                                    numRides.setText(dataSnapshot.child("bike_number_of_rides").getValue().toString()+" Rides");
                            }
                            else{
                                numRides.setText("No Rides yet");
                            }
                        }
                        else{
                            if (dataSnapshot.hasChild("motor_number_of_rides")){
                                if (dataSnapshot.child("motor_number_of_rides").getValue().toString().equals(""))
                                    numRides.setText("No Rides yet");
                                else
                                    numRides.setText(dataSnapshot.child("motor_number_of_rides").getValue().toString()+" Rides");
                            }
                            else{
                                numRides.setText("No Rides yet");
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FeedFragment(), "Feed", profileId);
        adapter.addFragment(new ProfileFragment(), "Profile", profileId);
        viewPager.setAdapter(adapter);
    }
}
