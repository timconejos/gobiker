package ph.com.team.gobiker.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;
import ph.com.team.gobiker.ui.followers.FollowersActivity;
import ph.com.team.gobiker.ui.following.FollowingActivity;
import ph.com.team.gobiker.LogoutActivity;
import ph.com.team.gobiker.R;
import ph.com.team.gobiker.SettingsActivity;
import ph.com.team.gobiker.MainScreenActivity;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private Button signOutButton, updateProfileButton, seeallfollowerButton, seeallfollowingButton;
    private TextView userGender, userBM, userWeight, userHeight, userAge, level, overall_distance, address, weightlabel, heightlabel, agelabel, addrlabel;
    private CircleImageView userProfileImage;
    private DatabaseReference profileUserRef,UsersRef;
    private String profileID;

    public static ProfileFragment newInstance() {
        ProfileFragment f = new ProfileFragment();
        return f;
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_profile_details, container, false);

        profileID = getArguments().getString("profileId");
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(profileID);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();

        userGender = root.findViewById(R.id.my_gender);
        userBM = root.findViewById(R.id.my_bike_motor);
        userWeight = root.findViewById(R.id.my_bike_weight);
        userHeight = root.findViewById(R.id.my_bike_height);
        userAge = root.findViewById(R.id.my_bike_age);
        level = root.findViewById(R.id.my_level);
        overall_distance = root.findViewById(R.id.my_distance_traveled);
        address = root.findViewById(R.id.my_address);
        addrlabel = root.findViewById(R.id.address_lbl);

        weightlabel = root.findViewById(R.id.weight_lbl);
        heightlabel = root.findViewById(R.id.height_lbl);
        agelabel = root.findViewById(R.id.age_lbl);

        updateProfileButton = root.findViewById(R.id.myUpdateProfileButton);
        signOutButton = root.findViewById(R.id.signOutButton);

        if(!profileID.equals(mAuth.getCurrentUser().getUid())){
            updateProfileButton.setVisibility(View.GONE);
            signOutButton.setVisibility(View.GONE);
        }else{
            updateProfileButton.setVisibility(View.VISIBLE);
            signOutButton.setVisibility(View.VISIBLE);
        }


        seeallfollowerButton = root.findViewById(R.id.seeAllFollowersButton);

        seeallfollowerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToFollowersActivity();
            }
        });

        seeallfollowingButton = root.findViewById(R.id.seeAllFollowingButton);


        seeallfollowingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToFollowingActivity();
            }
        });

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToLogoutActivity();
            }
        });

        updateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToSettingsActivity();
            }
        });

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int f = 0;
                if (dataSnapshot.exists()) {
                    for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                        if (snapshot.hasChild("following")) {
                            if (snapshot.child("following").hasChild(profileID)) {
                                f++;
                            }
                        }
                    }
                }
                if (f==1)
                    seeallfollowerButton.setText(f+" follower");
                else
                    seeallfollowerButton.setText(f+" followers");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        profileUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                    long numFollowing = 0;

                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myBike = dataSnapshot.child("bike").getValue().toString();

                    if (dataSnapshot.hasChild("following")){
                        numFollowing = dataSnapshot.child("following").getChildrenCount();
                    }

                    seeallfollowingButton.setText(numFollowing+" following");

                    if (dataSnapshot.child("check_address").getValue().toString().equals("true")){
                        if (dataSnapshot.child("province").getValue().toString().equals("")) {
                            address.setText("");
                        }
                        else{
                            address.setText(dataSnapshot.child("city").getValue().toString()+" "+dataSnapshot.child("province").getValue().toString());
                        }
                        addrlabel.setVisibility(View.VISIBLE);
                        address.setVisibility(View.VISIBLE);
                    }
                    else{
                        addrlabel.setVisibility(View.GONE);
                        address.setVisibility(View.GONE);
                    }

                    userGender.setText(myGender);

                    /*if (myBike.equals("true")){
                        bm = "Bicycle";
                    }
                    if (myMotor.equals("true")){
                        if (bm.equals(""))
                            bm = "Motorcycle";
                        else
                            bm = "Bicycle and Motorcycle";
                    }
                    userBM.setText(bm);
                    */

                    String myWeight="", myHeight="",myAge="";
                    if (dataSnapshot.hasChild("savedweight")) {
                        if (dataSnapshot.child("savedweight").getValue().toString().equals("0")) {
                            userWeight.setText("");
                            userWeight.setVisibility(View.GONE);
                        }
                        else{
                            myWeight = dataSnapshot.child("savedweight").getValue().toString();
                            String wunit = dataSnapshot.child("savedwunit").getValue().toString();
                            userWeight.setText(myWeight + " " + wunit);
                            userWeight.setVisibility(View.VISIBLE);
                        }
                    }
                    else{
                        userWeight.setText("");
                        userWeight.setVisibility(View.GONE);
                    }

                    if (dataSnapshot.hasChild("savedheight")) {
                        if (dataSnapshot.child("savedheight").getValue().toString().equals("0")) {
                            userHeight.setText("");
                            userHeight.setVisibility(View.GONE);
                        }
                        else{
                            myHeight = dataSnapshot.child("savedheight").getValue().toString();
                            String hunit = dataSnapshot.child("savedhunit").getValue().toString();
                            userHeight.setText(myHeight + " " + hunit);
                            userHeight.setVisibility(View.VISIBLE);
                        }
                    }
                    else{
                        userHeight.setText("");
                        userHeight.setVisibility(View.GONE);
                    }

                    if (dataSnapshot.hasChild("age")) {
                        if (dataSnapshot.child("age").getValue().toString().equals("0")) {
                            userAge.setText("");
                            userAge.setVisibility(View.GONE);
                        }
                        else{
                            myAge = dataSnapshot.child("age").getValue().toString();
                            userAge.setText(myAge + " years old");
                            userAge.setVisibility(View.VISIBLE);
                        }


                    }
                    else{
                        userAge.setText("");
                        userAge.setVisibility(View.GONE);
                    }

                    if (dataSnapshot.hasChild("active_ride")){
                        userBM.setText(dataSnapshot.child("active_ride").getValue().toString());
                        if (myBike.equals("true") && dataSnapshot.child("active_ride").getValue().toString().equals("Bicycle")) {
                            /*userHeight.setVisibility(View.GONE);
                            userWeight.setVisibility(View.GONE);
                            userAge.setVisibility(View.GONE);
                            heightlabel.setVisibility(View.VISIBLE);
                            weightlabel.setVisibility(View.VISIBLE);
                            agelabel.setVisibility(View.VISIBLE);*/


                            if (dataSnapshot.hasChild("bike_level")) {
                                if (dataSnapshot.child("bike_level").getValue().toString().equals("")) {
                                    level.setText("1");
                                }
                                else{
                                    level.setText(dataSnapshot.child("bike_level").getValue().toString());
                                }
                            }
                            else{
                                level.setText("1");
                            }

                            if (dataSnapshot.hasChild("bike_overall_distance")) {
                                if (dataSnapshot.child("bike_overall_distance").getValue().toString().equals("")) {
                                    overall_distance.setText("0 m");
                                }
                                else{
                                    overall_distance.setText(dataSnapshot.child("bike_overall_distance").getValue().toString()+" m");
                                }
                            }
                            else{
                                overall_distance.setText("0 m");
                            }
                        }
                        else{
                            if (dataSnapshot.hasChild("motor_level")) {
                                if (dataSnapshot.child("motor_level").getValue().toString().equals("")) {
                                    level.setText("1");
                                }
                                else{
                                    level.setText(dataSnapshot.child("motor_level").getValue().toString());
                                }
                            }
                            else{
                                level.setText("1");
                            }

                            if (dataSnapshot.hasChild("motor_overall_distance")) {
                                if (dataSnapshot.child("motor_overall_distance").getValue().toString().equals("")) {
                                    overall_distance.setText("0 m");
                                }
                                else{
                                    overall_distance.setText(dataSnapshot.child("motor_overall_distance").getValue().toString()+" m");
                                }
                            }
                            else{
                                overall_distance.setText("0 m");
                            }
                        }
                    }
                    else{
                        userBM.setText("Bicycle");
                    }



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return root;
    };


    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(getActivity(), MainScreenActivity.class);
        startActivity(loginIntent);
    }

    private void SendUserToSettingsActivity() {
        Intent loginIntent = new Intent(getActivity(), SettingsActivity.class);
        startActivity(loginIntent);
    }

    private void SendUserToLogoutActivity() {
        Intent loginIntent = new Intent(getActivity(), LogoutActivity.class);
        startActivity(loginIntent);
    }

    private void SendUserToFollowersActivity() {
        Intent loginIntent = new Intent(getActivity(), FollowersActivity.class);
        loginIntent.putExtra("visit_user_id",profileID);
        startActivity(loginIntent);
    }

    private void SendUserToFollowingActivity() {
        Intent loginIntent = new Intent(getActivity(), FollowingActivity.class);
        loginIntent.putExtra("visit_user_id",profileID);
        startActivity(loginIntent);
    }

}
