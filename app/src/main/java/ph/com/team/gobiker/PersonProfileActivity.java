package ph.com.team.gobiker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {
    private TextView userBM, userProfName, userGender;
    private CircleImageView userProfileImage;
    private Button SendFriendReqButton, SendMsgButton;
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

        SendMsgButton.setOnClickListener(view -> {
            SendUserToChatActivity();
        });


        UsersRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.hasChild("profileimage")){
                        String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(PersonProfileActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);
                    }

                    myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myBike = dataSnapshot.child("bike").getValue().toString();
                    String myMotor = dataSnapshot.child("motor").getValue().toString();

                    userProfName.setText(myProfileName);
                    userGender.setText(myGender);
                    if (myBike.equals("true"))
                        userBM.setText("Bicycle");

                    if (myMotor.equals("true")){
                        if (userBM.getText().equals(""))
                            userBM.setText("Motorcycle");
                        else
                            userBM.setText("Bicycle and Motorcycle");
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
                        SendFriendReqButton.setText("UNFOLLOW");
                    }
                    else{
                        SendFriendReqButton.setText("FOLLOW");
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
                    if (SendFriendReqButton.getText().equals("UNFOLLOW")){
                        UsersRef.child(senderUserId).child("following").child(receiverUserId).removeValue();
                        SendFriendReqButton.setText("FOLLOW");
                    }
                    else{
                        HashMap friendsMap = new HashMap();
                        friendsMap.put("uid",receiverUserId);
                        UsersRef.child(senderUserId).child("following").child(receiverUserId).updateChildren(friendsMap);
                        SendFriendReqButton.setText("UNFOLLOW");
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
        Intent chatIntent = new Intent(PersonProfileActivity.this,ChatActivity.class);
        chatIntent.putExtra("visit_user_id",receiverUserId);
        chatIntent.putExtra("userName",myProfileName);
        startActivity(chatIntent);
    }


}
