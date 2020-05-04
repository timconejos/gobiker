package ph.com.team.gobiker.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import ph.com.team.gobiker.ClickPostActivity;
import ph.com.team.gobiker.CommentsActivity;
import ph.com.team.gobiker.CreateAccount;
import ph.com.team.gobiker.PostActivity;
import ph.com.team.gobiker.R;
import ph.com.team.gobiker.ui.login.MainLoginActivity;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FloatingActionButton addNewPost;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Toolbar mToolbar;

    private CircleImageView NavProfileImage;
    private TextView NavProfileUsername;
    private ImageButton AddNewPostButton;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef, LikesRef;
    private String currentUserID;
    private View root;
    Boolean LikeChecker = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        addNewPost = root.findViewById(R.id.add_new_post);
        addNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddNewPost();
            }
        });

        mAuth = FirebaseAuth.getInstance();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        postList = root.findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);
        currentUserID = mAuth.getCurrentUser().getUid();
        DisplayAllUsersPosts();

        return root;
    }

    public void AddNewPost(){
        startActivity(new Intent(getActivity(), PostActivity.class));
    }

    private void DisplayAllUsersPosts() {
        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(Posts.class,
                        R.layout.all_posts_layout,
                        PostsViewHolder.class,
                        PostsRef) {
                    @Override
                    protected void populateViewHolder(PostsViewHolder viewHolder, Posts posts, int position) {
                        final String PostKey = getRef(position).getKey();

                        viewHolder.setFullname(posts.getFullname());
                        viewHolder.setTime(posts.getTime(), posts.getDate());
//                        viewHolder.setDate();
                        viewHolder.setDescription(posts.getDescription());
                        //viewHolder.setProfileimage(getApplicationContext(),posts.getProfileimage());
                        viewHolder.setPostimage(getActivity().getApplicationContext(),posts.getPostimage());

                        viewHolder.setLikeButtonStatus(PostKey);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent clickPostIntent = new Intent(getActivity(), ClickPostActivity.class);
                                clickPostIntent.putExtra("PostKey",PostKey);
                                startActivity(clickPostIntent);
                            }
                        });

                        viewHolder.CommentBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent commentsIntent = new Intent(getActivity(), CommentsActivity.class);
                                commentsIntent.putExtra("PostKey",PostKey);
                                startActivity(commentsIntent);
                            }
                        });

                        viewHolder.LikeBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                LikeChecker = true;
                                LikesRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (LikeChecker.equals(true)){
                                            if(dataSnapshot.child(PostKey).hasChild(currentUserID)){
                                                LikesRef.child(PostKey).child(currentUserID).removeValue();
                                                LikeChecker = false;
                                            }
                                            else{
                                                LikesRef.child(PostKey).child(currentUserID).setValue(true);
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
                };
        postList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        ImageButton LikepostButton, CommentPostButton;
        Button LikeBtn, CommentBtn;
        TextView DisplayNoOfLikes;
        int countLikes;
        String currentUserId;
        DatabaseReference LikesRef;

        public PostsViewHolder(View itemView){
            super(itemView);
            mView = itemView;
            LikeBtn = mView.findViewById(R.id.like_button);
            CommentBtn = mView.findViewById(R.id.comment_button);
//            LikepostButton = mView.findViewById(R.id.like_button);
//            CommentPostButton = mView.findViewById(R.id.comment_button);
//            DisplayNoOfLikes = mView.findViewById(R.id.like_button);

            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        public void setLikeButtonStatus(final String PostKey){
            LikesRef.addValueEventListener(new ValueEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Context context = null;
                    if(dataSnapshot.child(PostKey).hasChild(currentUserId)){
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        LikeBtn.setText(Integer.toString(countLikes)+" Likes");
//                        LikeBtn.setBackgroundResource(R.drawable.btn);
                        LikeBtn.setTextAppearance(R.style.TransparentBtn_Like);
//                        LikepostButton.setImageResource(R.drawable.ic_favorite_black_24dp);
//                        DisplayNoOfLikes.setText(Integer.toString(countLikes));
                    }
                    else{
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        LikeBtn.setText(Integer.toString(countLikes)+" Likes");
                        LikeBtn.setTextAppearance(R.style.TransparentBtn_Unlike);
//                        LikepostButton.setImageResource(R.drawable.ic_favorite_border_black_24dp);
//                        DisplayNoOfLikes.setText(Integer.toString(countLikes));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        public void setFullname(String fullname) {
            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }

        public void setProfileimage(Context ctx, String profileimage) {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);
            Picasso.with(ctx).load(profileimage).into(image);
        }
        public void setTime(String time, String date) {
            TextView PostTime = (TextView) mView.findViewById(R.id.post_time);
            PostTime.setText(date+" "+time);
        }
//        public void setDate(String date) {
//            TextView PostDate = (TextView) mView.findViewById(R.id.post_date);
//            PostDate.setText("   "+date);
//        }

        public void setDescription(String description) {
            TextView PostDescription = (TextView) mView.findViewById(R.id.post_description);
            PostDescription.setText(description);
        }

        public void setPostimage(Context ctx, String postimage) {
            ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.with(ctx).load(postimage).into(PostImage);
        }
    }
}
