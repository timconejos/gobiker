package ph.com.team.gobiker.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import ph.com.team.gobiker.ui.posts.ClickPostActivity;
import ph.com.team.gobiker.ui.posts.CommentsActivity;
import ph.com.team.gobiker.ui.posts.LikesActivity;
import ph.com.team.gobiker.ui.posts.PostActivity;
import ph.com.team.gobiker.R;
import ph.com.team.gobiker.ui.profile.ViewOthersProfile;
import uk.co.senab.photoview.PhotoViewAttacher;

public class HomeFeedFragment extends Fragment {
    private FloatingActionButton addNewPost;
    private RecyclerView postList;


    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef, LikesRef;
    private String currentUserID;
    private SwipeRefreshLayout swipe;

    Boolean LikeChecker = false;
    private PhotoViewAttacher pAttacher;

    private List<String> suggestions = new ArrayList<>();
    private ArrayAdapter<String> adapter ;

    public static HomeFeedFragment newInstance() {
        HomeFeedFragment f = new HomeFeedFragment();
        return f;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home_feed, container, false);

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

        swipe = root.findViewById(R.id.swiperefresh);
        swipe.setOnRefreshListener(() -> {

            new Handler().postDelayed(new Runnable() {
                @Override public void run() {
                    // Stop animation (This will be after 3 seconds)
                    DisplayAllUsersPosts();
                    swipe.setRefreshing(false);
                }
            }, 2500);
        });

        return root;
    };

    public void AddNewPost(){
        startActivity(new Intent(getActivity(), PostActivity.class));
    }


    private void DisplayAllUsersPosts() {
        Query SortPostsInDescendingOrder = PostsRef.orderByChild("counter");
        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(Posts.class,
                        R.layout.all_posts_layout,
                        PostsViewHolder.class,
                        SortPostsInDescendingOrder) {
                    @Override
                    protected void populateViewHolder(final PostsViewHolder viewHolder, final Posts posts, int position) {

                        final String PostKey = getRef(position).getKey();

                        UsersRef.child(currentUserID).child("following").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(posts.getUid()) || posts.getUid().equals(currentUserID)){
                                    viewHolder.mView.setVisibility(View.VISIBLE);
                                    viewHolder.lp.setVisibility(View.VISIBLE);

                                    //viewHolder.setFullname(UsersRef.child(posts.getUid()).child("fullname").val);

                                    UsersRef.child(posts.getUid()).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                viewHolder.setFullname(dataSnapshot.child("fullname").getValue().toString());
                                                if (dataSnapshot.hasChild("profileimage"))
                                                    viewHolder.setProfileimage(getActivity(), dataSnapshot.child("profileimage").getValue().toString());
                                                else
                                                    viewHolder.setProfileimage(getActivity(), "");

                                                //viewHolder.setProfileimage(getActivity().getApplicationContext(),Picasso.with(getActivity()).load(dataSnapshot.child("profileimage").getValue().toString()).placeholder(R.drawable.profile));

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                    viewHolder.setTime(posts.getTime(), posts.getDate());
//                        viewHolder.setDate();
                                    viewHolder.setDescription(posts.getDescription());
                                    //viewHolder.setProfileimage(getActivity().getApplicationContext(),posts.getProfileimage());

                                    if (posts.getPostimage()==""){

                                    }
                                    else {
                                        viewHolder.setPostimage(getActivity().getApplicationContext(), posts.getPostimage());
                                    }

                                    viewHolder.setLikeButtonStatus(PostKey);

                                    viewHolder.PostImage.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            pAttacher = new PhotoViewAttacher(viewHolder.PostImage);
                                            pAttacher.setZoomable(true);
                                            pAttacher.update();

                                        }
                                    });

                            /*viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent clickPostIntent = new Intent(getActivity(), ClickPostActivity.class);
                                    clickPostIntent.putExtra("PostKey", PostKey);
                                    startActivity(clickPostIntent);
                                }
                            });*/

                                    if(posts.getUid().equals(currentUserID)){
                                        viewHolder.optionMenuP.setVisibility(View.VISIBLE);
                                    }else{
                                        viewHolder.optionMenuP.setVisibility(View.GONE);
                                    }

                                    viewHolder.optionMenuP.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            //creating a popup menu
                                            PopupMenu popup = new PopupMenu(getActivity(), viewHolder.optionMenuP);
                                            //inflating menu from xml resource
                                            popup.inflate(R.menu.post_menu);
                                            //adding click listener
                                            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                                @Override
                                                public boolean onMenuItemClick(MenuItem item) {
                                                    switch (item.getItemId()) {
                                                        case R.id.post_edit_menu:
                                                            Intent clickPostIntent = new Intent(getActivity(), ClickPostActivity.class);
                                                            clickPostIntent.putExtra("PostKey", PostKey);
                                                            startActivity(clickPostIntent);
                                                            return true;
                                                        case R.id.post_delete_menu:
                                                            PostsRef.child(PostKey).removeValue();
                                                            DisplayAllUsersPosts();
                                                            Toast.makeText(getActivity(),"Post has been deleted.",Toast.LENGTH_SHORT).show();
                                                            return true;
                                                        default:
                                                            return false;
                                                    }
                                                }
                                            });
                                            //displaying the popup
                                            popup.show();

                                        }
                                    });

                                    viewHolder.CommentBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent commentsIntent = new Intent(getActivity(), CommentsActivity.class);
                                            commentsIntent.putExtra("PostKey", PostKey);
                                            commentsIntent.putExtra("FeedType", "HomeFeed");
                                            startActivity(commentsIntent);
                                        }
                                    });

                                    viewHolder.DisplayNoOfLikes.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent likesIntent = new Intent(getActivity(), LikesActivity.class);
                                            likesIntent.putExtra("PostKey", PostKey);
                                            likesIntent.putExtra("FeedType", "HomeFeed");
                                            startActivity(likesIntent);
                                        }
                                    });

                                    viewHolder.LikeBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            LikeChecker = true;
                                            LikesRef.child(PostKey).child("Likes").addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (LikeChecker.equals(true)) {
                                                        if (dataSnapshot.hasChild(currentUserID)) {
                                                            LikesRef.child(PostKey).child("Likes").child(currentUserID).child(currentUserID).removeValue();
                                                            LikesRef.child(PostKey).child("Likes").child(currentUserID).child("Timestamp").removeValue();
                                                            LikesRef.child(PostKey).child("Likes").child(currentUserID).child("isSeen").removeValue();
                                                            LikeChecker = false;
                                                        } else {
                                                            LikesRef.child(PostKey).child("Likes").child(currentUserID).child(currentUserID).setValue(true);
                                                            Calendar calForDate = Calendar.getInstance();
                                                            SimpleDateFormat currentDate = new SimpleDateFormat("MM-dd-yyyy");
                                                            String saveCurrentDate = currentDate.format(calForDate.getTime());

                                                            SimpleDateFormat currentDates = new SimpleDateFormat("MMMM dd, yyyy");
                                                            String saveCurrentDates = currentDates.format(calForDate.getTime());

                                                            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
                                                            String saveCurrentTime = currentTime.format(calForDate.getTime());

                                                            LikesRef.child(PostKey).child("Likes").child(currentUserID).child("Timestamp").setValue(saveCurrentDate+" "+saveCurrentTime);
                                                            LikesRef.child(PostKey).child("Likes").child(currentUserID).child("isSeen").setValue(false);
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
                                else{
                                    viewHolder.mView.setVisibility(View.GONE);
                                    viewHolder.lp.setVisibility(View.GONE);
                                }

                                viewHolder.profilell.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent profileIntent =  new Intent(getActivity(), ViewOthersProfile.class);
                                        profileIntent.putExtra("profileId",posts.getUid());
                                        getActivity().startActivity(profileIntent);
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                };
        postList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        ImageButton LikeBtn;
        Button CommentBtn;
        TextView DisplayNoOfLikes, optionMenuP;
        int countLikes;
        String currentUserId;
        LinearLayout lp, profilell;
        DatabaseReference LikesRef;
        ImageView PostImage;

        public PostsViewHolder(View itemView){
            super(itemView);
            mView = itemView;
            PostImage = (ImageView) mView.findViewById(R.id.post_image);
            LikeBtn = mView.findViewById(R.id.like_button);
            CommentBtn = mView.findViewById(R.id.comment_button);
            optionMenuP = mView.findViewById(R.id.post_options);
            lp = mView.findViewById(R.id.linear_posts);
            profilell = mView.findViewById(R.id.profile_ll);
//            LikepostButton = mView.findViewById(R.id.like_button);
//            CommentPostButton = mView.findViewById(R.id.comment_button);
            DisplayNoOfLikes = mView.findViewById(R.id.display_no_of_likes);

            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }



        public void setLikeButtonStatus(final String PostKey){
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(PostKey).child("Likes").hasChild(currentUserId)){
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

//        public void setLikeButtonStatus(final String PostKey){
//            LikesRef.addValueEventListener(new ValueEventListener() {
//                @RequiresApi(api = Build.VERSION_CODES.M)
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    Context context = null;
//                    if(dataSnapshot.child(PostKey).hasChild(currentUserId)){
//                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
//                        LikeBtn.setText(Integer.toString(countLikes));
////                        LikeBtn.setBackgroundResource(R.drawable.btn);
//                        LikeBtn.setTextAppearance(R.style.TransparentBtn_Like);
////                        LikepostButton.setImageResource(R.drawable.ic_favorite_black_24dp);
////                        DisplayNoOfLikes.setText(Integer.toString(countLikes));
//                    }
//                    else{
//                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
//                        LikeBtn.setText(Integer.toString(countLikes));
//                        LikeBtn.setTextAppearance(R.style.TransparentBtn_Unlike);
////                        LikepostButton.setImageResource(R.drawable.ic_favorite_border_black_24dp);
////                        DisplayNoOfLikes.setText(Integer.toString(countLikes));
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });
//        }

        public void setFullname(String fullname) {
            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }

        public void setProfileimage(Context ctx, String profileimage) {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);
            if (profileimage.equals(""))
                Picasso.with(ctx).load(R.drawable.profile).into(image);
            else
                Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(image);
        }
        public void setTime(String time, String date) {
            TextView PostTime = (TextView) mView.findViewById(R.id.post_time);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            try{
                Date date3 = sdf.parse(time);
                SimpleDateFormat sdf2 = new SimpleDateFormat("hh:mm aa");
                PostTime.setText(date+" "+sdf2.format(date3));
            }catch(ParseException e){
                e.printStackTrace();
            }
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
