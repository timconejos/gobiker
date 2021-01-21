package ph.com.team.gobiker.ui.followers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import ph.com.team.gobiker.R;

public class FollowersActivity extends AppCompatActivity {
    private DatabaseReference UsersRef,FollowingsRef;
    private FirebaseAuth mAuth;
    private RecyclerView FollowersList;
    private String receiverUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);

        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();

        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        //FollowingsRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child("following");

        FollowersList = findViewById(R.id.followers_list);
        FollowersList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        FollowersList.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onStart(){
        super.onStart();
        FirebaseRecyclerAdapter<Followers, FollowersActivity.FollowersViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Followers, FollowersActivity.FollowersViewHolder>(
                        Followers.class,
                        R.layout.all_likes_layout,
                        FollowersActivity.FollowersViewHolder.class,
                        UsersRef
                ) {
                    @Override
                    protected void populateViewHolder(final FollowersActivity.FollowersViewHolder likesViewHolder, Followers likes, int i) {
                        //likesViewHolder.setUsername(getRef(i).getKey());

                        UsersRef.child(getRef(i).getKey()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    if (dataSnapshot.hasChild("following")){
                                        if (dataSnapshot.child("following").hasChild(receiverUserId)){
                                            likesViewHolder.setUsername(dataSnapshot.child("fullname").getValue().toString());
                                            if (dataSnapshot.hasChild("profileimage"))
                                                likesViewHolder.setProfileimage(getApplicationContext(), dataSnapshot.child("profileimage").getValue().toString());
                                            else
                                                likesViewHolder.setProfileimage(getApplicationContext(), "");
                                        }
                                        else{
                                            likesViewHolder.image.setVisibility(View.GONE);
                                            likesViewHolder.myUserName.setVisibility(View.GONE);
                                            likesViewHolder.linearalllike.setVisibility(View.GONE);
                                        }
                                    }
                                    else{
                                        likesViewHolder.image.setVisibility(View.GONE);
                                        likesViewHolder.myUserName.setVisibility(View.GONE);
                                        likesViewHolder.linearalllike.setVisibility(View.GONE);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                };

        FollowersList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FollowersViewHolder extends RecyclerView.ViewHolder{
        View mView;
        TextView myUserName;
        CircleImageView image;
        LinearLayout linearalllike;
        public FollowersViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            myUserName = mView.findViewById(R.id.likes_username);
            image = mView.findViewById(R.id.likes_profile_image);
            linearalllike = mView.findViewById(R.id.linearalllikes);
        }

        public void setUsername(String username) {

            myUserName.setText(username);
        }

        public void setProfileimage(Context ctx, String profileimage) {

            if (profileimage.equals(""))
                Picasso.with(ctx).load(R.drawable.profile).into(image);
            else
                Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(image);
        }
    }
}