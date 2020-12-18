package ph.com.team.gobiker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import ph.com.team.gobiker.ui.chat.ChatActivity;

public class PersonProfileActivity extends AppCompatActivity {
    private TextView userBM, userProfName, userGender,address, userPhone, level, overall_distance, numRides;
    private CircleImageView userProfileImage;
    private Button SendFriendReqButton, SendMsgButton,seeallfollowerButton, seeallfollowingButton;
    private LinearLayout addressLayout;
    private DatabaseReference UsersRef;
    private FirebaseAuth mAuth;
    private String senderUserId, receiverUserId, CURRENT_STATE, myProfileName;
    private String saveCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth = FirebaseAuth.getInstance();
        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        senderUserId = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        userProfName = findViewById(R.id.person_full_name);
        userGender = findViewById(R.id.person_gender);
        userBM = findViewById(R.id.person_bm);
        userProfileImage = findViewById(R.id.person_profile_pic);
        SendFriendReqButton = findViewById(R.id.person_send_friend_request_btn);
        SendMsgButton = findViewById(R.id.person_send_msg_btn);
        address = findViewById(R.id.person_address);
        addressLayout = findViewById(R.id.address_layout);
        userPhone = findViewById(R.id.person_phone);
        level = findViewById(R.id.person_level);
        numRides = findViewById(R.id.person_nr);
        overall_distance = findViewById(R.id.person_dt);

        seeallfollowerButton = findViewById(R.id.followers_btn);

        seeallfollowerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToFollowersActivity();
            }
        });

        seeallfollowingButton = findViewById(R.id.following_btn);

        seeallfollowingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToFollowingActivity();
            }
        });

        SendMsgButton.setOnClickListener(view -> {
            SendUserToChatActivity();
        });

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int f = 0;
                if (dataSnapshot.exists()) {
                    for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                        if (snapshot.hasChild("following")) {
                            if (snapshot.child("following").hasChild(receiverUserId)) {
                                f++;
                            }
                        }
                    }
                }

                String followerTxt = "Follower";
                if (f==1)
                    followerTxt = "Follower";
                else
                    followerTxt = "Followers";

                seeallfollowerButton.setText((Html.fromHtml("<big>"
                        + f + "</big><br/>"
                        + "<small>" + followerTxt
                        + "</small>")));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        UsersRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    long numFollowing = 0;
                    if (dataSnapshot.hasChild("profileimage")){
                        String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(PersonProfileActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);
                    }

                    if (dataSnapshot.hasChild("following")){
                        numFollowing = dataSnapshot.child("following").getChildrenCount();
                    }



//                    seeallfollowingButton.setText(numFollowing+" following");
                    seeallfollowingButton.setText((Html.fromHtml("<big>"
                            + numFollowing + "</big><br/>"
                            + "<small>" + "Following"
                            + "</small>")));

                    myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myPhone = dataSnapshot.child("phone").getValue().toString();
                    String myBike = dataSnapshot.child("bike").getValue().toString();
                    String myMotor = dataSnapshot.child("motor").getValue().toString();

                    userProfName.setText(myProfileName);
                    if (!myGender.equals("Rather Not Say"))
                        userGender.setText(myGender);
                    else
                        userGender.setVisibility(View.GONE);
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
                                    level.setText("Lvl. 1");
                                }
                                else{
                                    level.setText("Lvl. " + dataSnapshot.child("bike_level").getValue().toString());
                                }
                            }
                            else{
                                level.setText("Lvl. 1");
                            }

                            if (dataSnapshot.hasChild("bike_number_of_rides")){
                                if (dataSnapshot.child("bike_number_of_rides").getValue().toString().equals(""))
                                    numRides.setText("0");
                                else
                                    numRides.setText(dataSnapshot.child("bike_number_of_rides").getValue().toString());
                            }
                            else{
                                numRides.setText("0");
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
                                    level.setText("Lvl. 1");
                                }
                                else{
                                    level.setText("Lvl. "+ dataSnapshot.child("motor_level").getValue().toString());
                                }
                            }
                            else{
                                level.setText("Lvl. 1");
                            }

                            if (dataSnapshot.hasChild("motor_number_of_rides")){
                                if (dataSnapshot.child("motor_number_of_rides").getValue().toString().equals(""))
                                    numRides.setText("0");
                                else
                                    numRides.setText(dataSnapshot.child("motor_number_of_rides").getValue().toString());
                            }
                            else{
                                numRides.setText("0");
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

                    if (dataSnapshot.child("check_address").getValue().toString().equals("true")){
                        address.setVisibility(View.VISIBLE);
                        addressLayout.setVisibility(View.VISIBLE);
                        if (dataSnapshot.child("province").getValue().toString().equals("")) {
                            address.setText("-");
                        }
                        else{
                            address.setText(dataSnapshot.child("city").getValue().toString()+" "+dataSnapshot.child("province").getValue().toString());
                        }
                    }
                    else{
                        address.setVisibility(View.GONE);
                        addressLayout.setVisibility(View.GONE);
                    }

                    if (dataSnapshot.child("check_phone").getValue().toString().equals("true")){
                        userPhone.setText(myPhone);
                        userPhone.setVisibility(View.VISIBLE);
                    }
                    else{
                        userPhone.setText("");
                        userPhone.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        UsersRef.child(senderUserId).child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.hasChild(receiverUserId)){
                        SendFriendReqButton.setText("Unfollow");
                    }
                    else{
                        SendFriendReqButton.setText("Follow");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (!senderUserId.equals(receiverUserId)){
            SendFriendReqButton.setVisibility(View.VISIBLE);
            SendFriendReqButton.setEnabled(true);
            SendMsgButton.setVisibility(View.VISIBLE);
            SendMsgButton.setEnabled(true);
            SendFriendReqButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (SendFriendReqButton.getText().equals("Unfollow")){
                        UsersRef.child(senderUserId).child("following").child(receiverUserId).removeValue();
                        SendFriendReqButton.setText("Follow");
                    }
                    else{
                        Calendar calForDate = Calendar.getInstance();
                        SimpleDateFormat currentDate = new SimpleDateFormat("MM-dd-yyyy");
                        String saveCurrentDate = currentDate.format(calForDate.getTime());

                        SimpleDateFormat currentDates = new SimpleDateFormat("MMMM dd, yyyy");
                        String saveCurrentDates = currentDates.format(calForDate.getTime());

                        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
                        String saveCurrentTime = currentTime.format(calForDate.getTime());

                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                        SimpleDateFormat sdf2 = new SimpleDateFormat("hh:mm aa");
                        Date date3 = null;
                        try{
                            date3 = sdf.parse(saveCurrentTime); }catch(ParseException e){
                            e.printStackTrace();
                        }

                        HashMap friendsMap = new HashMap();
                        friendsMap.put("uid",receiverUserId);
                        friendsMap.put("Timestamp", saveCurrentDate+" "+saveCurrentTime);
                        friendsMap.put("isSeen", false);
                        UsersRef.child(senderUserId).child("following").child(receiverUserId).updateChildren(friendsMap);
                        SendFriendReqButton.setText("Unfollow");
                    }

                }
            });
        }
        else{
            SendFriendReqButton.setEnabled(false);
            SendFriendReqButton.setVisibility(View.INVISIBLE);
            SendMsgButton.setEnabled(false);
            SendMsgButton.setVisibility(View.INVISIBLE);
        }
    }

    private void SendUserToChatActivity() {
        Intent chatIntent = new Intent(PersonProfileActivity.this, ChatActivity.class);
        chatIntent.putExtra("visit_user_id",receiverUserId);
        //chatIntent.putExtra("userName",myProfileName);
        startActivity(chatIntent);
    }

    private void SendUserToFollowersActivity() {
        Intent loginIntent = new Intent(PersonProfileActivity.this, FollowersActivity.class);
        loginIntent.putExtra("visit_user_id",receiverUserId);
        startActivity(loginIntent);
    }

    private void SendUserToFollowingActivity() {
        Intent loginIntent = new Intent(PersonProfileActivity.this, FollowingActivity.class);
        loginIntent.putExtra("visit_user_id",receiverUserId);
        startActivity(loginIntent);
    }
}
