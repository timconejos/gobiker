package ph.com.team.gobiker.ui.home;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
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
import ph.com.team.gobiker.R;
import ph.com.team.gobiker.ui.search.SearchAutoComplete;
import ph.com.team.gobiker.ui.profile.ViewOthersProfile;
import uk.co.senab.photoview.PhotoViewAttacher;

public class GroupDetailsActivity extends AppCompatActivity {
    private LinearLayout adminLayout;
    private TextView groupName, groupMembersNr, groupDateCreated, groupType, groupStatus, groupLabel, groupTypeNStatus, groupMembersNrNStatus;
    private CircleImageView groupImage;
    private Button membersBtn, groupRideBtn, joinBtn, requestsBtn, editGroupBtn;
    private View btnDivider;

    private DatabaseReference GroupsRef, GrpPostsRef, UsersRef, LikesRef;
    private FirebaseAuth mAuth;
    private String currentGroupID, currentUserID, fromNotifications;

    private RecyclerView groupSpecificPostList;
    private long countPosts = 0;
    private String usernamehldr, rolehldr, grouptypehldr;

    Boolean LikeChecker = false;
    private SwipeRefreshLayout swipe;
    private PhotoViewAttacher pAttacher;

    //members list and adapter
    private List<SearchAutoComplete> profileList;
    public RecyclerView profileSelectedView;
    public GroupSearchAdapter profileadapter;
    public List<GroupMemberProfile> profileSelectedList;

    //join requests list and adapter
    public RecyclerView requestSelectedView;
    public GroupRequestAdapter requestadapter;
    public List<GroupMemberProfile> requestSelectedList;

    //edit group details
    EditText edit_group_name;
    CircleImageView edit_group_image;
    Spinner edit_group_type;
    final static int Gallery_Pick = 1;
    private Uri ImageUri;
    private String grouppicstring;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_profile);

        currentGroupID = getIntent().getExtras().get("GroupID").toString();
        fromNotifications = getIntent().getExtras().get("groupAction").toString();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        GroupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        GrpPostsRef = FirebaseDatabase.getInstance().getReference().child("GroupPosts");
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("GroupLikes");

        groupImage = findViewById(R.id.group_profile_pic);
        groupName = findViewById(R.id.person_full_name);
//        groupTypeNStatus = findViewById(R.id.group_type_and_status);
        groupMembersNrNStatus = findViewById(R.id.members_nr_and_status);
        groupDateCreated = findViewById(R.id.date_created);
//        groupMembersNr = findViewById(R.id.members_nr);
//        groupStatus = findViewById(R.id.group_status);
        groupType = findViewById(R.id.group_type);
        groupLabel = findViewById(R.id.current_groups_label);

        adminLayout = findViewById(R.id.adminlayout);
        btnDivider = findViewById(R.id.btndivider);
        requestsBtn = findViewById(R.id.requests_btn);
        editGroupBtn = findViewById(R.id.edit_btn);
        membersBtn = findViewById(R.id.btn_members);
        joinBtn = findViewById(R.id.btn_join);
        groupRideBtn = findViewById(R.id.btn_groupride);

        //setup group posts
        groupSpecificPostList = findViewById(R.id.all_users_post_list);
        groupSpecificPostList.setHasFixedSize(true);
        groupSpecificPostList.setNestedScrollingEnabled(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        groupSpecificPostList.setLayoutManager(linearLayoutManager);

        profileList = new ArrayList<>();
        profileSelectedList = new ArrayList<>();
        profileadapter = new GroupSearchAdapter(profileSelectedList, GroupDetailsActivity.this, GroupsRef);
        profileadapter.notifyDataSetChanged();

        requestSelectedList = new ArrayList<>();
        requestadapter = new GroupRequestAdapter(requestSelectedList, GroupDetailsActivity.this);
        requestadapter.notifyDataSetChanged();

        if(fromNotifications.equals("joinrequests")){
            ViewJoinRequestDialog alert = new ViewJoinRequestDialog();
            alert.showDialog(GroupDetailsActivity.this);
        }

        if(fromNotifications.equals("viewmembers")){
            ViewMembersDialog alert = new ViewMembersDialog();
            alert.showDialog(GroupDetailsActivity.this);
        }

        requestsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewJoinRequestDialog alert = new ViewJoinRequestDialog();
                alert.showDialog(GroupDetailsActivity.this);
            }
        });

        membersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewMembersDialog alert = new ViewMembersDialog();
                alert.showDialog(GroupDetailsActivity.this);
            }
        });

        editGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewEditGroupDialog alert = new ViewEditGroupDialog();
                alert.showDialog(GroupDetailsActivity.this);
            }
        });

        groupSpecificPostList.setVisibility(View.GONE);

        RetrieveGroupDetails();
        RetrieveUsers();

    }

    private void RetrieveGroupDetails(){
        GroupsRef.child(currentGroupID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Groups group = dataSnapshot.getValue(Groups.class);

                if (dataSnapshot.hasChild("group_picture")){
                    grouppicstring = group.getGroup_picture();
                    Picasso.with(GroupDetailsActivity.this).load(group.getGroup_picture()).placeholder(R.drawable.profile).into(groupImage);
                }
                else{
                    grouppicstring = "";
                    Picasso.with(GroupDetailsActivity.this).load(R.drawable.profile).into(groupImage);
                }

                groupName.setText(group.getGroup_name());
                groupMembersNrNStatus.setText((Html.fromHtml("<b>"
                        + String.valueOf(dataSnapshot.child("Members").getChildrenCount()) +" members"
                                + "</b> | <i>" + group.getStatus() + "</i>")));
                groupDateCreated.setText("Created at "+group.getCreate_date());
//                groupMembersNr.setText(String.valueOf(dataSnapshot.child("Members").getChildrenCount())+" members");

                groupType.setText((Html.fromHtml("<b>"
                        + group.getGroup_type() + " group</b> ")));
                grouptypehldr = group.getGroup_type();
                rolehldr = "";
//                groupStatus.setText(group.getStatus());
//                groupType.setText(group.getGroup_type());

                if(dataSnapshot.child("Members").child(currentUserID).child("role").exists()) {
                    rolehldr = dataSnapshot.child("Members").child(currentUserID).child("role").getValue().toString();
                }else
                    rolehldr = "Member";

                if(group.getGroup_type().equals("Public")){
                    groupLabel.setText("Posts from this group");
                    if(dataSnapshot.child("Members").hasChild(currentUserID)) {
                        groupSpecificPostList.setVisibility(View.VISIBLE);
                        joinBtn.setText("Leave Group");
                    }else{
                        joinBtn.setText("Join");
                    }
                    requestsBtn.setVisibility(View.GONE);
                    if(rolehldr.equals("Member")){
                        adminLayout.setVisibility(View.GONE);
                        // btnDivider.setVisibility(View.GONE);
                        editGroupBtn.setVisibility(View.GONE);
                        groupRideBtn.setVisibility(View.GONE);
                    }else if(rolehldr.equals("Admin")){
                        adminLayout.setVisibility(View.VISIBLE);
                        // btnDivider.setVisibility(View.GONE);
                        editGroupBtn.setVisibility(View.VISIBLE);
                        groupRideBtn.setVisibility(View.VISIBLE);
                    }else{
                        adminLayout.setVisibility(View.GONE);
                        btnDivider.setVisibility(View.GONE);
                        editGroupBtn.setVisibility(View.GONE);
                        groupRideBtn.setVisibility(View.GONE);
                    }
                }else if(group.getGroup_type().equals("Private")){
                    if(dataSnapshot.child("Members").hasChild(currentUserID)){
                        if(dataSnapshot.child("Members").child(currentUserID).child("status").exists()){
                            if(dataSnapshot.child("Members").child(currentUserID).child("status").getValue().equals("Pending")){
                                joinBtn.setText("Cancel Join Request");
                                groupLabel.setText("Group is private. You need to have your join request approved first to view group posts.");
                                adminLayout.setVisibility(View.GONE);
                                editGroupBtn.setVisibility(View.GONE);
                                requestsBtn.setVisibility(View.GONE);
                                // btnDivider.setVisibility(View.GONE);
                                groupRideBtn.setVisibility(View.GONE);
                            }else if(dataSnapshot.child("Members").child(currentUserID).child("status").getValue().equals("Accepted")){
                                RetrievePostsFromGroup();
                                groupLabel.setText("Posts from this group");
                                joinBtn.setText("Leave Group");
                                groupSpecificPostList.setVisibility(View.VISIBLE);

                                if(rolehldr.equals("Member")){
                                    adminLayout.setVisibility(View.GONE);
                                    editGroupBtn.setVisibility(View.GONE);
                                    requestsBtn.setVisibility(View.GONE);
//                                    btnDivider.setVisibility(View.GONE);
                                }else if(rolehldr.equals("Admin")){
                                    adminLayout.setVisibility(View.VISIBLE);
                                    // btnDivider.setVisibility(View.VISIBLE);
                                    requestsBtn.setVisibility(View.VISIBLE);
                                    editGroupBtn.setVisibility(View.VISIBLE);
                                    groupRideBtn.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }else{
                        adminLayout.setVisibility(View.GONE);
                        editGroupBtn.setVisibility(View.GONE);
                        requestsBtn.setVisibility(View.GONE);
                        // btnDivider.setVisibility(View.GONE);
                        groupRideBtn.setVisibility(View.GONE);

                        groupLabel.setText("Group is private. You need to join first to view group posts.");
                        joinBtn.setText("Join");
                    }
                }

                CheckGroupMembership(group.getGroup_type(), joinBtn.getText().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void CheckGroupMembership(String groupType, String btnText){
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MM-dd-yyyy");
        String saveCurrentDate = currentDate.format(calForDate.getTime());

        SimpleDateFormat currentDates = new SimpleDateFormat("MMMM dd, yyyy");
        String saveCurrentDates = currentDates.format(calForDate.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        String saveCurrentTime = currentTime.format(calForDate.getTime());

        if(btnText.equals("Leave Group")){
            joinBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ProgressDialog loadingBarLeave = new ProgressDialog(getApplicationContext());
                    loadingBarLeave.setTitle("Loading");
                    loadingBarLeave.setMessage("Leaving group, please wait...");
                    loadingBarLeave.setCanceledOnTouchOutside(true);
                    loadingBarLeave.show();
                    if(rolehldr.equals("Admin")){
                        GroupsRef.child(currentGroupID).child("Members").orderByChild("role").equalTo("Admin").addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        boolean isCurrAdmin = false;
                                        int adminCtr = 0;
                                        for (DataSnapshot memberSnapshot : snapshot.getChildren()){
                                            if(memberSnapshot.getKey().equals(currentUserID)){
                                                isCurrAdmin = true;
                                            }
                                            adminCtr++;
                                        }
                                        if(adminCtr == 1 && isCurrAdmin){
                                            Toast.makeText(GroupDetailsActivity.this, "You have to assign atleast one admin to replace you before you leave the group.", Toast.LENGTH_SHORT).show();
                                        }else{
                                            GroupsRef.child(currentGroupID).child("Members").child(currentUserID).removeValue();
                                            Toast.makeText(GroupDetailsActivity.this, "You have left the group.", Toast.LENGTH_SHORT).show();
                                            joinBtn.setText("Join");
                                            groupSpecificPostList.setVisibility(View.GONE);
                                            loadingBarLeave.dismiss();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                }
                        );
                    }
                }
            });
        }

        if(groupType.equals("Public")){
           if (btnText.equals("Join")){
               joinBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        GroupsRef.child(currentGroupID).child("Members").child(currentUserID).child("timestamp_joined").setValue(saveCurrentDate+" "+saveCurrentTime);
                        GroupsRef.child(currentGroupID).child("Members").child(currentUserID).child("role").setValue("Member");
                        GroupsRef.child(currentGroupID).child("Members").child(currentUserID).child("status").setValue("Accepted");
                        UsersRef.child(currentUserID).child("groupsJoined").child(currentGroupID).setValue("Accepted");
                        Toast.makeText(GroupDetailsActivity.this, "You have joined the group.", Toast.LENGTH_SHORT).show();
                        joinBtn.setText("Leave Group");
                    }
                });
            }
        }else if(groupType.equals("Private")){
            if(btnText.equals("Join")){
                joinBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        GroupsRef.child(currentGroupID).child("Members").child(currentUserID).child("timestamp_joined").setValue(saveCurrentDate+" "+saveCurrentTime);
                        GroupsRef.child(currentGroupID).child("Members").child(currentUserID).child("role").setValue("Member");
                        GroupsRef.child(currentGroupID).child("Members").child(currentUserID).child("status").setValue("Pending");
                        UsersRef.child(currentUserID).child("groupsJoined").child(currentGroupID).setValue("Pending");
                        Toast.makeText(GroupDetailsActivity.this, "You have sent a join request to the group.", Toast.LENGTH_SHORT).show();
                        joinBtn.setText("Cancel Join Request");
                    }
                });
            }else if(btnText.equals("Cancel Join Request")){
                joinBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        GroupsRef.child(currentGroupID).child("Members").child(currentUserID).removeValue();
                        UsersRef.child(currentUserID).child("groupsJoined").child(currentGroupID).removeValue();
                        Toast.makeText(GroupDetailsActivity.this, "You have cancelled your join request the group.", Toast.LENGTH_SHORT).show();
                        joinBtn.setText("Join");
                    }
                });

            }
        }
    }

    private void RetrievePostsFromGroup() {
        ProgressDialog loadingBar = new ProgressDialog(this);
        loadingBar.setTitle("Loading");
        loadingBar.setMessage("Please wait, while we are loading the posts...");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();

        Query SpecificPostFromGroup = GrpPostsRef.orderByChild("counter");
        FirebaseRecyclerAdapter<GroupsPost, GroupPostsViewHolder> specGrpFirebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<GroupsPost, GroupPostsViewHolder>(GroupsPost.class,
                        R.layout.all_posts_layout,
                        GroupPostsViewHolder.class,
                        SpecificPostFromGroup) {
                    @Override
                    protected void populateViewHolder(final GroupPostsViewHolder viewHolder, final GroupsPost posts, int position) {
                            final String PostKey = getRef(position).getKey();

                            UsersRef.child(posts.getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        usernamehldr = dataSnapshot.child("fullname").getValue().toString();
                                        viewHolder.setFullname(dataSnapshot.child("fullname").getValue().toString());
                                        if (dataSnapshot.hasChild("profileimage"))
                                            viewHolder.setProfileimage(GroupDetailsActivity.this, dataSnapshot.child("profileimage").getValue().toString());
                                        else
                                            viewHolder.setProfileimage(GroupDetailsActivity.this, "");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            viewHolder.setTime(posts.getTime(), posts.getDate());
                            viewHolder.setDescription(posts.getDescription());

                            if (posts.getPostimage()==""){

                            }
                            else {
                                viewHolder.setPostimage(GroupDetailsActivity.this, posts.getPostimage());
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

                            if(posts.getUid().equals(currentUserID)){
                                viewHolder.optionMenuP.setVisibility(View.VISIBLE);
                            }else{
                                viewHolder.optionMenuP.setVisibility(View.GONE);
                            }

                            viewHolder.optionMenuP.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    //creating a popup menu
                                    PopupMenu popup = new PopupMenu(GroupDetailsActivity.this, viewHolder.optionMenuP);
                                    //inflating menu from xml resource
                                    popup.inflate(R.menu.post_menu);
                                    //adding click listener
                                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                        @Override
                                        public boolean onMenuItemClick(MenuItem item) {
                                            switch (item.getItemId()) {
                                                case R.id.post_edit_menu:
                                                    Intent clickPostIntent = new Intent(GroupDetailsActivity.this, ClickPostActivity.class);
                                                    clickPostIntent.putExtra("PostKey", PostKey);
                                                    startActivity(clickPostIntent);
                                                    return true;
                                                case R.id.post_delete_menu:
                                                    GrpPostsRef.child(PostKey).removeValue();
                                                    Toast.makeText(GroupDetailsActivity.this,"Post has been deleted.",Toast.LENGTH_SHORT).show();
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
                                    Intent commentsIntent = new Intent(GroupDetailsActivity.this, CommentsActivity.class);
                                    commentsIntent.putExtra("PostKey", PostKey);
                                    commentsIntent.putExtra("FeedType", "GroupFeed");
                                    startActivity(commentsIntent);
                                }
                            });

                            viewHolder.DisplayNoOfLikes.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent likesIntent = new Intent(GroupDetailsActivity.this, LikesActivity.class);
                                    likesIntent.putExtra("PostKey", PostKey);
                                    likesIntent.putExtra("FeedType", "GroupFeed");
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

                            GroupsRef.child(posts.getGroupid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        viewHolder.setFullname(usernamehldr+" > "+snapshot.child("group_name").getValue().toString());

                                        if(posts.getGroupid().equals(currentGroupID)){
                                            viewHolder.mView.setVisibility(View.VISIBLE);
                                            viewHolder.lp.setVisibility(View.VISIBLE);
                                        }else{
                                            viewHolder.mView.setVisibility(View.GONE);
                                            viewHolder.lp.setVisibility(View.GONE);
                                        }

                                        viewHolder.profilell.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent profileIntent = new Intent(GroupDetailsActivity.this, ViewOthersProfile.class);
                                                profileIntent.putExtra("profileId",posts.getUid());
                                                startActivity(profileIntent);
                                            }
                                        });

                                        loadingBar.dismiss();

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                    }
                };

        specGrpFirebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                int itemCtr = specGrpFirebaseRecyclerAdapter.getItemCount();
                groupSpecificPostList.scrollToPosition(itemCtr);
            }
        });
        groupSpecificPostList.setAdapter(specGrpFirebaseRecyclerAdapter);
    }

    public void RetrieveUsers(){
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot suggestionSnapshot : dataSnapshot.getChildren()){
//                    if(!suggestionSnapshot.getKey().equals(currentUserID)){
                        String suggestion = suggestionSnapshot.child("fullname").getValue(String.class);
                        String profilepicstring = "";
                        if (suggestionSnapshot.hasChild("profileimage"))
                            profilepicstring = suggestionSnapshot.child("profileimage").getValue().toString();
                        else
                            profilepicstring = "";
                        final String finalProfilepicstring = profilepicstring;
                        if(suggestion != null){
                            if(!profileList.contains(new SearchAutoComplete(suggestion, profilepicstring, suggestionSnapshot.getKey()))){
                                profileList.add(new SearchAutoComplete(suggestion, profilepicstring, suggestionSnapshot.getKey()));
                            }

                            GroupsRef.child(currentGroupID).child("Members").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.hasChild(suggestionSnapshot.getKey())) {
                                        if(snapshot.child(suggestionSnapshot.getKey()).child("status").getValue().equals("Pending")){
                                            if (!requestSelectedList.contains(new GroupMemberProfile(currentGroupID, suggestionSnapshot.getKey(), suggestion, finalProfilepicstring, ""))) {
                                                requestSelectedList.add(new GroupMemberProfile(currentGroupID, suggestionSnapshot.getKey(), suggestion, finalProfilepicstring, ""));
                                                requestadapter.notifyDataSetChanged();
                                            }
                                        }else if(snapshot.child(suggestionSnapshot.getKey()).child("status").getValue().equals("Accepted")){
                                            if (!profileSelectedList.contains(new GroupMemberProfile(currentGroupID, suggestionSnapshot.getKey(), suggestion, finalProfilepicstring, snapshot.child(suggestionSnapshot.getKey()).child("role").getValue().toString()))) {
                                                profileSelectedList.add(new GroupMemberProfile(currentGroupID, suggestionSnapshot.getKey(), suggestion, finalProfilepicstring, snapshot.child(suggestionSnapshot.getKey()).child("role").getValue().toString()));
                                                profileadapter.notifyDataSetChanged();
                                            }
                                        }

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        }
//                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public class ViewMembersDialog {
        public void showDialog(Activity activity){
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog_members);

            profileSelectedView = (RecyclerView) dialog.findViewById(R.id.all_users_post_list);
            profileSelectedView.setHasFixedSize(true);
            profileSelectedView.setLayoutManager(new LinearLayoutManager(GroupDetailsActivity.this));

            profileadapter = new GroupSearchAdapter(profileSelectedList, GroupDetailsActivity.this, GroupsRef);
            profileSelectedView.setAdapter(profileadapter);
            profileadapter.notifyDataSetChanged();

            Button close_button = (Button) dialog.findViewById(R.id.new_convo_close);

//            AutoCompleteTextView searchEditText = dialog.findViewById(R.id.search_box_input_fragment);
//            SearchAutoCompleteAdapter searchadapter = new SearchAutoCompleteAdapter(activity, profileList);
//            searchEditText.setAdapter(searchadapter);
//
//            searchEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//
//                    GroupsRef.child(currentGroupID).child("Members").child(profileList.get(i).getProfileuid()).addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            GroupMemberProfile profileItem = null;
//                            if(snapshot.exists()){
//                                profileItem = new GroupMemberProfile(
//                                        currentGroupID, profileList.get(i).getProfileuid(), profileList.get(i).getProfilename(), profileList.get(i).getProfileimage(), snapshot.child("role").getValue().toString()
//                                );
//                            }else{
//
//                            }
//
//
//                            if(!profileSelectedList.contains(profileItem)){
//                                profileSelectedList.add(profileItem);
//                                profileadapter.notifyDataSetChanged();
//                                searchEditText.setText("");
//                            }else{
//                                Toast.makeText(activity, "This user is already in your list.", Toast.LENGTH_SHORT).show();
//                                searchEditText.setText("");
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
//
//                }
//            });

            close_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    searchEditText.setText("");
//                    profileSelectedList.clear();
                    dialog.dismiss();
                }
            });

            dialog.show();
        }
    }

    public class ViewJoinRequestDialog{
        public void showDialog(Activity activity){
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog_join_requests);

            requestSelectedView = (RecyclerView) dialog.findViewById(R.id.all_users_post_list);
            requestSelectedView.setHasFixedSize(true);
            requestSelectedView.setLayoutManager(new LinearLayoutManager(GroupDetailsActivity.this));

            requestadapter = new GroupRequestAdapter(requestSelectedList, GroupDetailsActivity.this);
            requestSelectedView.setAdapter(requestadapter);
            requestadapter.notifyDataSetChanged();

            Button close_button = (Button) dialog.findViewById(R.id.new_convo_close);

            close_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        }
    }

    public class ViewEditGroupDialog{
        public void showDialog(Activity activity) {
            final Dialog groupdialog = new Dialog(activity);
            groupdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            groupdialog.setCancelable(false);
            groupdialog.setContentView(R.layout.dialog_new_group);

            edit_group_name = (EditText) groupdialog.findViewById(R.id.setup_full_name);
            edit_group_image = (CircleImageView) groupdialog.findViewById(R.id.setup_group_profile_image);
            edit_group_type = (Spinner) groupdialog.findViewById(R.id.group_type);

            ArrayAdapter<CharSequence> staticAdapter = ArrayAdapter
                    .createFromResource(GroupDetailsActivity.this, R.array.group_type,
                            android.R.layout.simple_spinner_item);

            staticAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            edit_group_type.setAdapter(staticAdapter);

            edit_group_name.setText(groupName.getText().toString());

            if(grouppicstring.equals("")){
                Picasso.with(GroupDetailsActivity.this).load(R.drawable.profile).into(edit_group_image);
            }else{
                Picasso.with(GroupDetailsActivity.this).load(grouppicstring).placeholder(R.drawable.profile).into(edit_group_image);
            }

//            if (!groupType.getText().toString().equals(null)) {
            if(!grouptypehldr.equals(null)){
                int spinnerPosition = staticAdapter.getPosition(grouptypehldr);
                edit_group_type.setSelection(spinnerPosition);
            }


            Button close_button = (Button) groupdialog.findViewById(R.id.new_group_close);
            Button next_button = (Button) groupdialog.findViewById(R.id.new_group_proceed);

            next_button.setText("Update");

            edit_group_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent galleryIntent = new Intent();
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setType("image/*");
                    startActivityForResult(galleryIntent,Gallery_Pick);
                }
            });

            close_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    groupdialog.dismiss();
                }
            });

            next_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatabaseReference GroupRootRef = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference groupRef = GroupRootRef.child("Groups");
                    StorageReference groupImageRef;
                    String groupKey = currentGroupID;

                    groupImageRef = FirebaseStorage.getInstance().getReference().child("group_picture");

                    ProgressDialog loadingBar = new ProgressDialog(GroupDetailsActivity.this);
                    loadingBar.setTitle("Group");
                    loadingBar.setMessage("Please wait, while we are updating your Group...");
                    loadingBar.setCanceledOnTouchOutside(true);
                    loadingBar.show();

                    if(ImageUri != null){
                        final StorageReference filePath = groupImageRef.child(groupKey+".jpg");
                        filePath.putFile(ImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String downloadUrl = uri.toString();
                                        writeDataToDatabase(
                                                downloadUrl,
                                                groupKey,
                                                GroupRootRef,
                                                groupdialog,
                                                loadingBar);

                                    }
                                });
                            }
                        });
                    }else{
                        writeDataToDatabase(
                                "",
                                groupKey,
                                GroupRootRef,
                                groupdialog,
                                loadingBar);
                    }
                }
            });

            groupdialog.show();
        }


        private void writeDataToDatabase(String picUrl,
                                         String groupKey,
                                         DatabaseReference GroupRootRef,
                                         Dialog gcdialog,
                                         ProgressDialog loadingBar){

            DatabaseReference groupRef = GroupRootRef.child("Groups");

            groupRef.child(groupKey).child("group_name").setValue(edit_group_name.getText().toString().trim());
            if(!picUrl.equals("")){
                groupRef.child(groupKey).child("group_picture").setValue(picUrl);
            }
            groupRef.child(groupKey).child("group_type").setValue(edit_group_type.getSelectedItem().toString());

            Toast.makeText(GroupDetailsActivity.this, "Group Details update successful!", Toast.LENGTH_SHORT).show();
            loadingBar.dismiss();
            gcdialog.dismiss();
        }
    }

    public static class GroupPostsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        ImageButton LikeBtn;
        Button CommentBtn;
        TextView DisplayNoOfLikes, optionMenuP;
        int countLikes;
        String currentUserId;
        LinearLayout lp, profilell;
        DatabaseReference LikesRef;
        ImageView PostImage;

        public GroupPostsViewHolder(View itemView){
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

            LikesRef = FirebaseDatabase.getInstance().getReference().child("GroupLikes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        public void setLikeButtonStatus(final String PostKey){
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(PostKey).child("Likes").hasChild(currentUserId)){
                        countLikes = (int) dataSnapshot.child(PostKey).child("Likes").getChildrenCount();
                        LikeBtn.setImageResource(R.drawable.baseline_favorite_24);
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

        public void setDescription(String description) {
            TextView PostDescription = (TextView) mView.findViewById(R.id.post_description);
            PostDescription.setText(description);
        }

        public void setPostimage(Context ctx, String postimage) {
            ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.with(ctx).load(postimage).into(PostImage);
//            Picasso.with(ctx).load(postimage).fit().centerCrop().into(PostImage);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ProgressDialog loadingBar = new ProgressDialog(GroupDetailsActivity.this);

        if (requestCode==Gallery_Pick && resultCode==GroupDetailsActivity.this.RESULT_OK && data!=null){

            ImageUri = data.getData();
            loadingBar.setTitle("Profile Image");
            loadingBar.setMessage("Please wait, while we are updating your Group Profile Image...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            edit_group_image.setImageURI(ImageUri);

            loadingBar.dismiss();
        }
    }

}
