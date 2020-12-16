package ph.com.team.gobiker.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
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

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    private View root;
    private FirebaseAuth mAuth;
    private TextView userProfName, userEmail, userPhone , numRides;
    private CircleImageView userProfileImage;
    private DatabaseReference profileUserRef,UsersRef;
    private String currentUserId;

    private TabLayout tabLayout;
    private ViewPager viewPager;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        dashboardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
//                textView.setText(s);
            }
        });

        getProfileDetails();

        viewPager = root.findViewById(R.id.profilecontainer);
        tabLayout = root.findViewById(R.id.profilenavbar);

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        return root;
    }

    private void getProfileDetails(){

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        userProfName = root.findViewById(R.id.profile_name);
        userEmail = root.findViewById(R.id.profile_email);
        userPhone = root.findViewById(R.id.profile_phone);
        numRides = root.findViewById(R.id.profile_rides);
        userProfileImage = root.findViewById(R.id.profile_pic);

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
                        Picasso.with(getActivity()).load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);
                    }
                    else{
                        Picasso.with(getActivity()).load(R.drawable.profile).into(userProfileImage);
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
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new FeedFragment(), "Feed");
        adapter.addFragment(new ProfileFragment(), "Profile");
        viewPager.setAdapter(adapter);
    }


}
