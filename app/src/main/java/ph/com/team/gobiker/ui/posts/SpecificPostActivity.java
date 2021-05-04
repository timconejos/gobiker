package ph.com.team.gobiker.ui.posts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import ph.com.team.gobiker.NavActivity;
import ph.com.team.gobiker.R;
import ph.com.team.gobiker.ui.home.Posts;
import ph.com.team.gobiker.user.User;
import uk.co.senab.photoview.PhotoViewAttacher;

public class SpecificPostActivity extends AppCompatActivity {
    private DatabaseReference usersRef, postRef, likesRef, commentsRef;
    private FirebaseAuth mAuth;
    private String current_user_id, current_post_id;

    private TextView userName;
    private CircleImageView profileImage;

    private ImageButton LikeBtn;
    private TextView DisplayNoOfLikes, optionMenuP;
    private int countLikes;
    private LinearLayout lp;
    private ImageView PostImage;
    private TextView postTime, postDescription;
    private PhotoViewAttacher pAttacher;
    Boolean LikeChecker = false;

    private ImageButton PostCommentButton;
    private EditText CommentInputText;
    private RecyclerView CommentsList;
    private String from_feed_type;

    FirebaseRecyclerAdapter<Comments, CommentsViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_post);

        current_post_id = getIntent().getExtras().get("post_id").toString();
        from_feed_type = getIntent().getExtras().get("feed_type").toString();

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        if(from_feed_type.equals("HomeFeed")){
            postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
            likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        }else if(from_feed_type.equals("GroupFeed")){
            postRef = FirebaseDatabase.getInstance().getReference().child("GroupPosts");
            likesRef = FirebaseDatabase.getInstance().getReference().child("GroupLikes");
        }

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        commentsRef = postRef.child(current_post_id).child("Comments");

        PostImage = (ImageView) findViewById(R.id.post_image);
        LikeBtn = findViewById(R.id.like_button);
        optionMenuP = findViewById(R.id.post_options);
        lp = findViewById(R.id.linear_posts);
        DisplayNoOfLikes = findViewById(R.id.display_no_of_likes);

        userName = (TextView) findViewById(R.id.post_user_name);
        profileImage = (CircleImageView) findViewById(R.id.post_profile_image);
        postTime = (TextView) findViewById(R.id.post_time);
        postDescription = (TextView) findViewById(R.id.post_description);

        CommentsList = findViewById(R.id.comments_list);
//        CommentsList.setHasFixedSize(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentsList.setLayoutManager(linearLayoutManager);

        CommentInputText = findViewById(R.id.comment_input);
        PostCommentButton = findViewById(R.id.post_comment_btn);

        PostCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usersRef.child(current_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String userName = dataSnapshot.child("fullname").getValue().toString();

                            ValidateComment(userName);

                            CommentInputText.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        retrievePostDetails();
    }

    @Override
    protected void onStart() {
        super.onStart();
        retrieveComments();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        firebaseRecyclerAdapter.cleanup();
    }

    private void retrievePostDetails(){
        postRef.child(current_post_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Posts post = snapshot.getValue(Posts.class);

                usersRef.child(post.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userName.setText(snapshot.child("fullname").getValue().toString());

                        if (!snapshot.hasChild("profileimage"))
                            Picasso.with(SpecificPostActivity.this).load(R.drawable.profile).into(profileImage);
                        else
                            Picasso.with(SpecificPostActivity.this).load(snapshot.child("profileimage").getValue().toString()).placeholder(R.drawable.profile).into(profileImage);

                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                        try{
                            Date date3 = sdf.parse(post.getTime());
                            SimpleDateFormat sdf2 = new SimpleDateFormat("hh:mm aa");
                            postTime.setText(post.getDate()+" "+sdf2.format(date3));
                        }catch(ParseException e){
                            e.printStackTrace();
                        }

                        postDescription.setText(post.getDescription());

                        if (post.getPostimage()!=""){
                            Picasso.with(SpecificPostActivity.this).load(post.getPostimage()).into(PostImage);
                        }

                        setLikeButtonStatus(current_post_id, current_user_id);

                        PostImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                pAttacher = new PhotoViewAttacher(PostImage);
                                pAttacher.setZoomable(true);
                                pAttacher.update();

                            }
                        });

                        if(!current_user_id.equals(post.getUid())){
                            optionMenuP.setVisibility(View.INVISIBLE);
                        }else{
                            optionMenuP.setVisibility(View.VISIBLE);
                        }

                        optionMenuP.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                PopupMenu popup = new PopupMenu(SpecificPostActivity.this, optionMenuP);
                                popup.inflate(R.menu.post_menu);

                                popup.getMenu().findItem(R.id.post_view_menu).setVisible(false);

                                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        switch (item.getItemId()) {
                                            case R.id.post_view_menu:
                                                Intent viewIntent = new Intent(SpecificPostActivity.this, SpecificPostActivity.class);
                                                viewIntent.putExtra("post_id", current_post_id);
                                                viewIntent.putExtra("feed_type", from_feed_type);
                                                startActivity(viewIntent);
                                                return true;
                                            case R.id.post_edit_menu:
                                                Intent clickPostIntent = new Intent(SpecificPostActivity.this, ClickPostActivity.class);
                                                clickPostIntent.putExtra("PostKey", current_post_id);
                                                clickPostIntent.putExtra("from_feed", from_feed_type);
                                                startActivity(clickPostIntent);
                                                return true;
                                            case R.id.post_delete_menu:
                                                postRef.child(current_post_id).removeValue();
                                                Toast.makeText(SpecificPostActivity.this,"Post has been deleted.",Toast.LENGTH_SHORT).show();
                                                Intent mainIntent = new Intent(SpecificPostActivity.this, NavActivity.class);
                                                startActivity(mainIntent);
                                                finish();
                                                return true;
                                            default:
                                                return false;
                                        }
                                    }
                                });

                                popup.show();

                            }
                        });

                        DisplayNoOfLikes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent likesIntent = new Intent(SpecificPostActivity.this, LikesActivity.class);
                                likesIntent.putExtra("PostKey", current_post_id);
                                likesIntent.putExtra("FeedType", "HomeFeed");
                                startActivity(likesIntent);
                            }
                        });

                        LikeBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                LikeChecker = true;
                                likesRef.child(current_post_id).child("Likes").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (LikeChecker.equals(true)) {
                                            if (dataSnapshot.hasChild(current_user_id)) {
                                                likesRef.child(current_post_id).child("Likes").child(current_user_id).child(current_user_id).removeValue();
                                                likesRef.child(current_post_id).child("Likes").child(current_user_id).child("Timestamp").removeValue();
                                                likesRef.child(current_post_id).child("Likes").child(current_user_id).child("isSeen").removeValue();
                                                LikeChecker = false;
                                            } else {
                                                likesRef.child(current_post_id).child("Likes").child(current_user_id).child(current_user_id).setValue(true);
                                                Calendar calForDate = Calendar.getInstance();
                                                SimpleDateFormat currentDate = new SimpleDateFormat("MM-dd-yyyy");
                                                String saveCurrentDate = currentDate.format(calForDate.getTime());

                                                SimpleDateFormat currentDates = new SimpleDateFormat("MMMM dd, yyyy");
                                                String saveCurrentDates = currentDates.format(calForDate.getTime());

                                                SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
                                                String saveCurrentTime = currentTime.format(calForDate.getTime());

                                                likesRef.child(current_post_id).child("Likes").child(current_user_id).child("Timestamp").setValue(saveCurrentDate+" "+saveCurrentTime);
                                                likesRef.child(current_post_id).child("Likes").child(current_user_id).child("isSeen").setValue(false);
                                                LikeChecker = false;
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void setLikeButtonStatus(final String PostKey, final String profileId){
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(PostKey).child("Likes").hasChild(profileId)){
                    countLikes = (int) dataSnapshot.child(PostKey).child("Likes").getChildrenCount();
                    LikeBtn.setImageResource(R.drawable.ic_favorite_border_red_24dp);
                    DisplayNoOfLikes.setText(Integer.toString(countLikes));
                }
                else{
                    countLikes = (int) dataSnapshot.child(PostKey).child("Likes").getChildrenCount();
                    LikeBtn.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                    DisplayNoOfLikes.setText(Integer.toString(countLikes));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void retrieveComments(){
        firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(
                        Comments.class,
                        R.layout.all_comments_layout,
                        CommentsViewHolder.class,
                        commentsRef
                ) {
                    @Override
                    protected void populateViewHolder(final CommentsViewHolder commentsViewHolder, Comments comments, int i) {
                        usersRef.child(comments.getUid()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    commentsViewHolder.setUsername(dataSnapshot.child("fullname").getValue().toString());
                                    if (dataSnapshot.hasChild("profileimage"))
                                        commentsViewHolder.setProfileimage(getApplicationContext(), dataSnapshot.child("profileimage").getValue().toString());
                                    else
                                        commentsViewHolder.setProfileimage(getApplicationContext(), "");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        commentsViewHolder.setComments(comments.getComments());
                        commentsViewHolder.setTime(comments.getDate()+" "+comments.getTime());
                        final String CommentsKey = getRef(i).getKey();

                        if (comments.getUid().equals(current_user_id)) {
                            commentsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //Toast.makeText(CommentsActivity.this,"AA",Toast.LENGTH_SHORT).show();
                                    Intent clickPostIntent = new Intent(SpecificPostActivity.this, ClickCommentActivity.class);
                                    clickPostIntent.putExtra("PostKey", current_post_id);
                                    clickPostIntent.putExtra("CommentsKey", CommentsKey);
                                    startActivity(clickPostIntent);
                                }
                            });
                        }
                    }
                };

        CommentsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUsername(String username) {
            TextView myUserName = mView.findViewById(R.id.comment_username);
            myUserName.setText(username);
        }

        public void setTime(String time) {
            TextView myTime = mView.findViewById(R.id.comment_time);
            myTime.setText(time);
        }

        public void setProfileimage(Context ctx, String profileimage) {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.comment_profile_image);
            if (profileimage.equals(""))
                Picasso.with(ctx).load(R.drawable.profile).into(image);
            else
                Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(image);
        }

        public void setComments(String comments) {
            TextView myComment = mView.findViewById(R.id.comment_text);
            myComment.setText(comments);
        }
    }

    private void ValidateComment(String userName) {
        String commentText = CommentInputText.getText().toString();

        if (TextUtils.isEmpty(commentText)){
            Toast.makeText(this, "Please write text to comment...", Toast.LENGTH_SHORT).show();
        }
        else{
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("MM-dd-yyyy");
            final String saveCurrentDate = currentDate.format(calForDate.getTime());
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
            final String saveCurrentTime = currentTime.format(calForDate.getTime());

            final String RandomKey = current_user_id + saveCurrentDate + saveCurrentTime;
            HashMap commentsMap = new HashMap();
            commentsMap.put("uid",current_user_id);
            commentsMap.put("comment",commentText);
            commentsMap.put("date",saveCurrentDate);
            commentsMap.put("time",saveCurrentTime);
            commentsMap.put("username",userName);
            commentsMap.put("isSeen", false);

            commentsRef.child(RandomKey).updateChildren(commentsMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){
                                Toast.makeText(SpecificPostActivity.this,"you have commented successfully.",Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(SpecificPostActivity.this,"Error occurred, try again.",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}

