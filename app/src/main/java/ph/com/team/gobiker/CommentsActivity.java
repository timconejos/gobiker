    package ph.com.team.gobiker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

    public class CommentsActivity extends AppCompatActivity {

        private ImageButton PostCommentButton;
        private EditText CommentInputText;
        private RecyclerView CommentsList;
        private String Post_Key, current_user_id;
        private DatabaseReference UsersRef, postRef;
        private FirebaseAuth mAuth;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_comments);

            Post_Key = getIntent().getExtras().get("PostKey").toString();

            mAuth = FirebaseAuth.getInstance();
            current_user_id = mAuth.getCurrentUser().getUid();
            UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
            postRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("Comments");

            CommentsList = findViewById(R.id.comments_list);
            CommentsList.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setReverseLayout(true);
            linearLayoutManager.setStackFromEnd(true);
            CommentsList.setLayoutManager(linearLayoutManager);

            CommentInputText = findViewById(R.id.comment_input);
            PostCommentButton = findViewById(R.id.post_comment_btn);

            PostCommentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
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
        }

        @Override
        protected void onStart(){
            super.onStart();
            FirebaseRecyclerAdapter<Comments, CommentsViewHolder> firebaseRecyclerAdapter =
                    new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(
                            Comments.class,
                            R.layout.all_comments_layout,
                            CommentsViewHolder.class,
                            postRef
                    ) {
                        @Override
                        protected void populateViewHolder(final CommentsViewHolder commentsViewHolder, Comments comments, int i) {
                            //commentsViewHolder.setUsername(comments.getUsername());
                            UsersRef.child(comments.getUid()).addValueEventListener(new ValueEventListener() {
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
                            //commentsViewHolder.setDate(comments.getDate()+" "+comments.getTime());
                            commentsViewHolder.setTime(comments.getDate()+" "+comments.getTime());
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

                postRef.child(RandomKey).updateChildren(commentsMap)
                        .addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(CommentsActivity.this,"you have commented successfully.",Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(CommentsActivity.this,"Error occurred, try again.",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
}
