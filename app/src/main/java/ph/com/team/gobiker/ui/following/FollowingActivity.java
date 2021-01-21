package ph.com.team.gobiker.ui.following;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ph.com.team.gobiker.R;
import ph.com.team.gobiker.ui.posts.Likes;
import ph.com.team.gobiker.ui.posts.LikesActivity;

public class FollowingActivity extends AppCompatActivity {
    private DatabaseReference UsersRef,FollowingsRef;
    private FirebaseAuth mAuth;
    private RecyclerView FollowingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);

        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FollowingsRef = FirebaseDatabase.getInstance().getReference().child("Users").child(getIntent().getExtras().get("visit_user_id").toString()).child("following");

        FollowingList = findViewById(R.id.following_list);
        FollowingList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        FollowingList.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onStart(){
        super.onStart();
        FirebaseRecyclerAdapter<Likes, LikesActivity.LikesViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Likes, LikesActivity.LikesViewHolder>(
                        Likes.class,
                        R.layout.all_likes_layout,
                        LikesActivity.LikesViewHolder.class,
                        FollowingsRef
                ) {
                    @Override
                    protected void populateViewHolder(final LikesActivity.LikesViewHolder likesViewHolder, Likes likes, int i) {
                        //likesViewHolder.setUsername(getRef(i).getKey());
                        UsersRef.child(getRef(i).getKey()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    likesViewHolder.setUsername(dataSnapshot.child("fullname").getValue().toString());
                                    if (dataSnapshot.hasChild("profileimage"))
                                        likesViewHolder.setProfileimage(getApplicationContext(), dataSnapshot.child("profileimage").getValue().toString());
                                    else
                                        likesViewHolder.setProfileimage(getApplicationContext(), "");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                };

        FollowingList.setAdapter(firebaseRecyclerAdapter);
    }
}