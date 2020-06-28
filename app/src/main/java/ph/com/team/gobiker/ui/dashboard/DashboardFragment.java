package ph.com.team.gobiker.ui.dashboard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import ph.com.team.gobiker.LogoutActivity;
import ph.com.team.gobiker.R;
import ph.com.team.gobiker.SettingsActivity;
import ph.com.team.gobiker.login;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    private FirebaseAuth mAuth;
    private Button signOutButton, updateProfileButton;
    private TextView userProfName,userGender, userBM, userEmail, userPhone, userWeight, userHeight, userAge;
    private CircleImageView userProfileImage;
    private DatabaseReference profileUserRef,UsersRef;
    private String currentUserId;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
//        final TextView textView = root.findViewById(R.id.text_dashboard);
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
//                textView.setText(s);
            }
        });

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        userProfName = root.findViewById(R.id.my_profile_full_name);
        userGender = root.findViewById(R.id.my_gender);
        userBM = root.findViewById(R.id.my_bike_motor);
        userEmail = root.findViewById(R.id.my_email_address);
        userPhone = root.findViewById(R.id.my_phone);
        userProfileImage = root.findViewById(R.id.my_profile_pic);
        userWeight = root.findViewById(R.id.my_bike_weight);
        userHeight = root.findViewById(R.id.my_bike_height);
        userAge = root.findViewById(R.id.my_bike_age);

        updateProfileButton = root.findViewById(R.id.myUpdateProfileButton);

        signOutButton = root.findViewById(R.id.signOutButton);

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

        profileUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                    String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myEmail = dataSnapshot.child("email").getValue().toString();
                    String myPhone = dataSnapshot.child("phone").getValue().toString();
                    String myBike = dataSnapshot.child("bike").getValue().toString();
                    String myMotor = dataSnapshot.child("motor").getValue().toString();
                    String bm = "";

                    if (dataSnapshot.hasChild("profileimage")){
                        String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(getActivity()).load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);
                    }
                    else{
                        Picasso.with(getActivity()).load(R.drawable.profile).into(userProfileImage);
                    }

                    userProfName.setText(myProfileName);
                    userGender.setText(myGender);
                    userEmail.setText(myEmail);
                    userPhone.setText(myPhone);

                    if (myBike.equals("true")){
                        bm = "Bicycle";
                    }
                    if (myMotor.equals("true")){
                        if (bm.equals(""))
                            bm = "Motorcycle";
                        else
                            bm = "Bicycle and Motorcycle";
                    }

                    userBM.setText(bm);

                    if (myBike.equals("true")) {
                        userHeight.setVisibility(View.GONE);
                        userWeight.setVisibility(View.GONE);
                        userAge.setVisibility(View.GONE);

                        String myWeight="", myHeight="",myAge="";
                        if (dataSnapshot.hasChild("savedweight")) {
                            if (dataSnapshot.child("savedweight").getValue().toString().equals("")) {
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
                            if (dataSnapshot.child("savedheight").getValue().toString().equals("")) {
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
                    }
                    else{
                        userHeight.setVisibility(View.GONE);
                        userWeight.setVisibility(View.GONE);
                        userAge.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return root;
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(getActivity(), login.class);
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
}
