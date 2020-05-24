package ph.com.team.gobiker.ui.dashboard;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import ph.com.team.gobiker.R;
import ph.com.team.gobiker.SettingsActivity;
import ph.com.team.gobiker.login;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    private FirebaseAuth mAuth;
    private Button signOutButton, updateProfileButton;
    private TextView userProfName,userGender, userBM, userEmail, userPhone;
    private CircleImageView userProfileImage;
    private DatabaseReference profileUserRef;
    private String currentUserId;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        final TextView textView = root.findViewById(R.id.text_dashboard);
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);

        userProfName = root.findViewById(R.id.my_profile_full_name);
        userGender = root.findViewById(R.id.my_gender);
        userBM = root.findViewById(R.id.my_bike_motor);
        userEmail = root.findViewById(R.id.my_email_address);
        userPhone = root.findViewById(R.id.my_phone);
        userProfileImage = root.findViewById(R.id.my_profile_pic);

        updateProfileButton = root.findViewById(R.id.myUpdateProfileButton);

        signOutButton = root.findViewById(R.id.signOutButton);

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                SendUserToLoginActivity();
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
                    userGender.setText("Gender: "+myGender);
                    userEmail.setText("Email Address: "+myEmail);
                    userPhone.setText("Phone: "+myPhone);

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
}
