package ph.com.team.gobiker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
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

public class LikesActivity extends AppCompatActivity {
    private String Post_Key;
    private DatabaseReference UsersRef, postRef;
    private FirebaseAuth mAuth;
    private RecyclerView LikesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_likes);

        Post_Key = getIntent().getExtras().get("PostKey").toString();

        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Likes").child(Post_Key).child("Likes");

        LikesList = findViewById(R.id.likes_list);
        LikesList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        LikesList.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onStart(){
        super.onStart();
        FirebaseRecyclerAdapter<Likes, LikesViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Likes, LikesViewHolder>(
                        Likes.class,
                        R.layout.all_likes_layout,
                        LikesViewHolder.class,
                        postRef
                ) {
                    @Override
                    protected void populateViewHolder(final LikesViewHolder likesViewHolder, Likes likes, int i) {
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

        LikesList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class LikesViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public LikesViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUsername(String username) {
            TextView myUserName = mView.findViewById(R.id.likes_username);
            myUserName.setText(username);
        }

        public void setProfileimage(Context ctx, String profileimage) {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.likes_profile_image);
            if (profileimage.equals(""))
                Picasso.with(ctx).load(R.drawable.profile).into(image);
            else
                Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(image);
        }
    }
}