package ph.com.team.gobiker.ui.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import ph.com.team.gobiker.ui.posts.ClickPostActivity;
import ph.com.team.gobiker.ui.posts.CommentsActivity;
import ph.com.team.gobiker.ui.posts.LikesActivity;
import ph.com.team.gobiker.R;
import ph.com.team.gobiker.ui.posts.SpecificPostActivity;
import ph.com.team.gobiker.ui.search.SearchAutoComplete;
import ph.com.team.gobiker.ui.search.SearchAutoCompleteAdapter;
import ph.com.team.gobiker.ui.chat.ChatProfile;
import ph.com.team.gobiker.ui.chat.ChatSearchAdapter;
import ph.com.team.gobiker.ui.profile.ViewOthersProfile;
import uk.co.senab.photoview.PhotoViewAttacher;

public class GroupFragment extends Fragment {

    private FirebaseAuth mAuth;
    private RecyclerView groupPostList;
    private DatabaseReference UsersRef, GroupsRef, PostsRef, LikesRef, GroupLikesRef;
    private String currentUserID, vid;
    private List<SearchAutoComplete> profileList;
    private RecyclerView profileSelectedView, groupView, yourGroupsView;
    private ChatSearchAdapter profileadapter;
    private GroupsRecyclerAdapter groupadapter, yourgroupsadapter;
    private List<ChatProfile> profileSelectedList;
    private List<Groups> groupList;
    private List<Groups> joinedGroupList;

    String gallery_type;
    final static int Gallery_Pick = 1;

    private Uri ImageUri;
    private Uri PostImageUri;

    //create group details
    EditText group_name;
    CircleImageView group_image;
    Spinner group_type;

    //create and view post details
    Dialog postdialog;
    ProgressDialog postLoadingBar;
    String current_user_name;
    FloatingActionButton addGroupPost;
    ImageButton SelectPostImage;
    Button UpdatePostButton, CancelPostButton;
    EditText PostDescription;
    Spinner GroupToPost;
    StorageReference PostsImageReference;

    TextView groupspeopleheader, yourgroupsheader, grouppostsheader;

    private String Description, saveCurrentDate, saveCurrentTime, postRandomName, groupDownloadUrl = "", current_user_id, saveCurrentDates;
    private long countPosts = 0;
    private String groupidhldr, usernamehldr;

    Boolean LikeChecker = false;
    private SwipeRefreshLayout swipe;
    private PhotoViewAttacher pAttacher;

    public static GroupFragment newInstance() {
        GroupFragment f = new GroupFragment();
        return f;
    }


    @Override
    public void onResume() {
        super.onResume();
        RetrieveGroupPosts();


    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_group_feed, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("GroupPosts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("GroupLikes");


        RetrieveCurrentUserDetails();

        groupspeopleheader = root.findViewById(R.id.active_groups_label);
        yourgroupsheader = root.findViewById(R.id.your_groups_label);
        grouppostsheader = root.findViewById(R.id.current_groups_label);

        groupspeopleheader.setVisibility(View.INVISIBLE);
        yourgroupsheader.setVisibility(View.INVISIBLE);
        grouppostsheader.setVisibility(View.INVISIBLE);

        groupView = (RecyclerView) root.findViewById(R.id.all_groups_view);
        groupView.setHasFixedSize(true);
        groupView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        groupList = new ArrayList<>();
        groupadapter = new GroupsRecyclerAdapter(groupList, getActivity());
        groupView.setAdapter(groupadapter);
        groupadapter.notifyDataSetChanged();

        yourGroupsView = (RecyclerView) root.findViewById(R.id.your_groups);
        yourGroupsView.setHasFixedSize(true);
        yourGroupsView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        joinedGroupList = new ArrayList<>();
        yourgroupsadapter = new GroupsRecyclerAdapter(joinedGroupList, getActivity());
        yourGroupsView.setAdapter(yourgroupsadapter);
        yourgroupsadapter.notifyDataSetChanged();

        groupPostList = root.findViewById(R.id.all_users_post_list);
        groupPostList.setHasFixedSize(true);
        groupPostList.setNestedScrollingEnabled(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        groupPostList.setLayoutManager(linearLayoutManager);

        swipe = root.findViewById(R.id.swiperefresh);
        swipe.setOnRefreshListener(() -> {

            new Handler().postDelayed(new Runnable() {
                @Override public void run() {
                    // Stop animation (This will be after 3 seconds)
                    RetrieveGroupPosts();
                    swipe.setRefreshing(false);
                }
            }, 2500);
        });

        profileList = new ArrayList<>();

        addGroupPost = root.findViewById(R.id.add_new_post);
        addGroupPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateGroupPostDialog alert = new CreateGroupPostDialog();
                alert.showDialog(getActivity());
            }
        });

        RetrieveGroups();
        RetrieveUsers();
        RetrieveGroupPosts();

        Button newChatBtn = root.findViewById(R.id.new_group);

        newChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewChatDialog alert = new ViewChatDialog();
                alert.showDialog(getActivity());
            }
        });

        return root;
    };

    public class ViewChatDialog {
        public void showDialog(Activity activity){
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog_new_chat);

            profileSelectedView = (RecyclerView) dialog.findViewById(R.id.all_users_post_list);
            profileSelectedView.setHasFixedSize(true);
            profileSelectedView.setLayoutManager(new LinearLayoutManager(getActivity()));
            profileSelectedList = new ArrayList<>();
            profileadapter = new ChatSearchAdapter(profileSelectedList, getActivity());
            profileSelectedView.setAdapter(profileadapter);
            profileadapter.notifyDataSetChanged();

            Button close_button = (Button) dialog.findViewById(R.id.new_convo_close);
            Button next_button = (Button) dialog.findViewById(R.id.new_convo_proceed);

            if(profileSelectedList != null){
                profileSelectedList.clear();
            }

            AutoCompleteTextView searchEditText = dialog.findViewById(R.id.search_box_input_fragment);
            SearchAutoCompleteAdapter searchadapter = new SearchAutoCompleteAdapter(activity, profileList);
            searchEditText.setAdapter(searchadapter);

            searchEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    ChatProfile profileItem = new ChatProfile(
                            profileList.get(i).getProfileuid(), profileList.get(i).getProfilename(), profileList.get(i).getProfileimage()
                    );

                    if(!profileSelectedList.contains(profileItem)){
                        profileSelectedList.add(profileItem);
                        profileadapter.notifyDataSetChanged();
                        searchEditText.setText("");
                    }else{
                        Toast.makeText(activity, "This user is already in your list.", Toast.LENGTH_SHORT).show();
                        searchEditText.setText("");
                    }
                }
            });

            close_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    searchEditText.setText("");
                    profileSelectedList.clear();
                    dialog.dismiss();
                }
            });

            next_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.hide();
                    CreateGroupDialog groupalertdialog = new CreateGroupDialog();
                    groupalertdialog.showDialog(getActivity(), profileSelectedList, dialog);

                }
            });

            dialog.show();
        }
    }

    public class CreateGroupDialog {
        public void showDialog(Activity activity, List<ChatProfile> groupparticipants, Dialog chatdialog){
            final Dialog groupdialog = new Dialog(activity);
            groupdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            groupdialog.setCancelable(false);
            groupdialog.setContentView(R.layout.dialog_new_group);

            group_name = (EditText) groupdialog.findViewById(R.id.setup_full_name);
            group_image = (CircleImageView) groupdialog.findViewById(R.id.setup_group_profile_image);
            group_type = (Spinner) groupdialog.findViewById(R.id.group_type);

            ArrayAdapter<CharSequence> staticAdapter = ArrayAdapter
                    .createFromResource(getActivity(), R.array.group_type,
                            android.R.layout.simple_spinner_item);

            staticAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            group_type.setAdapter(staticAdapter);

            Button close_button = (Button) groupdialog.findViewById(R.id.new_group_close);
            Button next_button = (Button) groupdialog.findViewById(R.id.new_group_proceed);

            group_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    gallery_type = "profile";
                    Intent galleryIntent = new Intent();
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setType("image/*");
                    startActivityForResult(galleryIntent,Gallery_Pick);
                }
            });

            close_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    chatdialog.show();
                    groupdialog.dismiss();
                }
            });

            next_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(TextUtils.isEmpty(group_name.getText().toString())){
                        Toast.makeText(activity, "Group Name cannot be empty. Please try again.", Toast.LENGTH_SHORT).show();
                    }else{
                        DatabaseReference GroupRootRef = FirebaseDatabase.getInstance().getReference();
                        DatabaseReference groupRef = GroupRootRef.child("Groups");
                        StorageReference groupImageRef;
                        String groupKey = groupRef.push().getKey();

                        groupImageRef = FirebaseStorage.getInstance().getReference().child("group_picture");

                        ProgressDialog loadingBar = new ProgressDialog(getActivity());
                        loadingBar.setTitle("Group");
                        loadingBar.setMessage("Please wait, while we are creating your Group...");
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
                                                    groupparticipants,
                                                    chatdialog,
                                                    groupdialog,
                                                    activity,
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
                                    groupparticipants,
                                    chatdialog,
                                    groupdialog,
                                    activity,
                                    loadingBar);
                        }
                    }

                }
            });
            groupdialog.show();
        }

        private void writeDataToDatabase(String picUrl,
                                         String groupKey,
                                         DatabaseReference GroupRootRef,
                                         List<ChatProfile> groupparticipants,
                                         Dialog chatdialog,
                                         Dialog gcdialog,
                                         Activity activity,
                                         ProgressDialog loadingBar){
            String saveCurrentDate, saveCurrentTime;

            DatabaseReference groupRef = GroupRootRef.child("Groups");
            DatabaseReference gcRef = GroupRootRef.child("GroupChats");
            String gcKey = gcRef.push().getKey();

            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("MM-dd-yyyy");
            saveCurrentDate = currentDate.format(calForDate.getTime());
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
            saveCurrentTime = currentTime.format(calForDate.getTime());

            groupRef.child(groupKey).child("group_name").setValue(group_name.getText().toString().trim());
            if(!picUrl.equals("")){
                groupRef.child(groupKey).child("group_picture").setValue(picUrl);
            }
            groupRef.child(groupKey).child("group_type").setValue(group_type.getSelectedItem().toString());
            groupRef.child(groupKey).child("create_date").setValue(saveCurrentDate);
            groupRef.child(groupKey).child("status").setValue("Active");
            groupRef.child(groupKey).child("Members").child(currentUserID).child("role").setValue("Admin");
            groupRef.child(groupKey).child("Members").child(currentUserID).child("status").setValue("Accepted");

            for(int x=0; x<groupparticipants.size(); x++){
                if(!groupparticipants.get(x).getUid().equals(currentUserID)){
                    groupRef.child(groupKey).child("Members").child(groupparticipants.get(x).getUid()).child("role").setValue("Member");
                    groupRef.child(groupKey).child("Members").child(groupparticipants.get(x).getUid()).child("status").setValue("Accepted");
                }
            }

            String message_sender_ref = "Messages/" + currentUserID + "/" +  gcKey;
            DatabaseReference user_message_key = GroupRootRef.child("Messages").child(currentUserID)
                    .child(gcKey).push();
            String message_push_id = user_message_key.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", "A new group chat has been made.");
            messageTextBody.put("time",saveCurrentTime);
            messageTextBody.put("date",saveCurrentDate);
            messageTextBody.put("type","text");
            messageTextBody.put("isSeen", false);
            messageTextBody.put("from",currentUserID);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(message_sender_ref+"/"+message_push_id,messageTextBody);

            ArrayList<String> userids = new ArrayList<String>();
            userids.add(currentUserID);

            for(int x=0; x<groupparticipants.size(); x++){
                if(!groupparticipants.get(x).getUid().equals(currentUserID)){
                    String message_receiver_ref = "Messages/" + groupparticipants.get(x).getUid() + "/" +  gcKey;
                    messageBodyDetails.put(message_receiver_ref+"/"+message_push_id,messageTextBody);
                }
                userids.add(groupparticipants.get(x).getUid());
            }

            gcRef.child(gcKey).child("gc_name").setValue(group_name.getText().toString().trim());
            if(!picUrl.equals("")){
                gcRef.child(gcKey).child("gc_picture").setValue(picUrl);
            }
            gcRef.child(gcKey).child("gc_participants").setValue(userids);

            GroupRootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        loadingBar.dismiss();
                        chatdialog.dismiss();
                        gcdialog.dismiss();
                        Toast.makeText(activity,"Group successfully created!",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        loadingBar.dismiss();
                        Toast.makeText(activity,"Error: "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }

    public class CreateGroupPostDialog {
        public void showDialog(Activity activity) {
            postdialog = new Dialog(activity);
            postdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            postdialog.setCancelable(false);
            postdialog.setContentView(R.layout.dialog_group_create_post);

            SelectPostImage = postdialog.findViewById(R.id.select_post_image);
            UpdatePostButton = postdialog.findViewById(R.id.update_post_button);
            CancelPostButton = postdialog.findViewById(R.id.cancel_post_button);
            PostDescription = postdialog.findViewById(R.id.post_description);
            GroupToPost = postdialog.findViewById(R.id.group_type);
            PostsImageReference = FirebaseStorage.getInstance().getReference();

            ArrayAdapter userAdapter = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, joinedGroupList);

            GroupToPost.setAdapter(userAdapter);
            GroupToPost.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Groups grouphldr = (Groups) parent.getSelectedItem();
                    groupidhldr = grouphldr.getGroup_id();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            SelectPostImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    gallery_type = "post";
                    Intent galleryIntent = new Intent();
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setType("image/*");
                    startActivityForResult(galleryIntent,Gallery_Pick);
                }
            });

            UpdatePostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ValidatePostInfo();
                }
            });

            CancelPostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    postdialog.dismiss();
                }
            });


            postdialog.show();
        }

        private void ValidatePostInfo() {
            Description = PostDescription.getText().toString();

            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("MM-dd-yyyy");
            saveCurrentDate = currentDate.format(calForDate.getTime());

            SimpleDateFormat currentDates = new SimpleDateFormat("MMMM dd, yyyy");
            saveCurrentDates = currentDates.format(calForDate.getTime());

            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
            saveCurrentTime = currentTime.format(calForDate.getTime());
            postRandomName = saveCurrentDate+saveCurrentTime;

            if(!Description.isEmpty()){
                postLoadingBar = new ProgressDialog(getActivity());
                postLoadingBar.setTitle("Add New Post");
                postLoadingBar.setMessage("Please wait, while we are adding your new post...");
                postLoadingBar.show();
                postLoadingBar.setCanceledOnTouchOutside(true);

                if(PostImageUri==null){
                    SavingPostInformationToDatabase();
                }
                else {
                    StoringImageToFirebaseStorage();
                }
            }else if(Description.isEmpty() && PostImageUri !=null){
                postLoadingBar = new ProgressDialog(getActivity());
                postLoadingBar.setTitle("Add New Post");
                postLoadingBar.setMessage("Please wait, while we are adding your new post...");
                postLoadingBar.show();
                postLoadingBar.setCanceledOnTouchOutside(true);

                StoringImageToFirebaseStorage();
            }else if(Description.isEmpty() && PostImageUri == null){
                Toast.makeText(getActivity(),"Cannot create this post since Description and Image are empty.",Toast.LENGTH_SHORT).show();
            }

        }

        private void StoringImageToFirebaseStorage() {
            final StorageReference filePath = PostsImageReference.child("post_images").child(PostImageUri.getLastPathSegment() + postRandomName + ".jpg");
            filePath.putFile(PostImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            groupDownloadUrl = uri.toString();
                            SavingPostInformationToDatabase();
                        }
                    });
                }
            });
        }

        private void SavingPostInformationToDatabase() {
            PostsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        countPosts = dataSnapshot.getChildrenCount();
                    }


                    HashMap postsMap = new HashMap();
                    postsMap.put("uid",currentUserID);
                    postsMap.put("date",saveCurrentDates);
                    postsMap.put("time",saveCurrentTime);
                    postsMap.put("description",Description);
                    if (!groupDownloadUrl.equals(""))
                        postsMap.put("postimage",groupDownloadUrl);
                    postsMap.put("fullname",current_user_name);
                    postsMap.put("counter",countPosts);
                    postsMap.put("groupid", groupidhldr);
                    PostsRef.child(currentUserID + postRandomName).updateChildren(postsMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(getActivity(),"New Post is added successfully",Toast.LENGTH_SHORT).show();
                                        postdialog.dismiss();
                                        postLoadingBar.dismiss();
                                        countPosts = 0;
                                        groupadapter.notifyDataSetChanged();
                                        yourgroupsadapter.notifyDataSetChanged();

                                    }
                                    else{
                                        Toast.makeText(getActivity(),"Error occurred while updating your post",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void RetrieveGroups(){
        GroupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                groupList.clear();
                joinedGroupList.clear();
                for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()){
                    Groups group = groupSnapshot.getValue(Groups.class);
                    String groupname = group.getGroup_name();
                    String profilepicstring = "";
                    if (groupSnapshot.hasChild("group_picture"))
                        profilepicstring = group.getGroup_picture();
                    else
                        profilepicstring = "";
                    String grouptype = group.getGroup_type();
                    String groupstatus = group.getStatus();
                    String groupcreatedate = group.getCreate_date();

                    if(groupname != null){
                        String finalProfilepicstring = profilepicstring;
                        UsersRef.child(currentUserID).child("following").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                    if(groupSnapshot.child("Members").hasChild(dataSnapshot.child("uid").getValue().toString())){
                                        if(groupSnapshot.child("Members").hasChild(currentUserID)){
                                            if(groupSnapshot.child("Members").child(currentUserID).child("status").getValue().toString().equals("Pending")){
                                                groupList.add(new Groups(groupSnapshot.getKey(), groupname, finalProfilepicstring, grouptype, groupstatus, groupcreatedate, ""));
                                            }
                                        }else{
                                            groupList.add(new Groups(groupSnapshot.getKey(), groupname, finalProfilepicstring, grouptype, groupstatus, groupcreatedate, ""));
                                        }

                                    }
                                    Collections.shuffle(groupList);
                                    groupadapter.notifyDataSetChanged();
                                    yourgroupsadapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        if(groupSnapshot.child("Members").hasChild(currentUserID)){
                            String groupmembershipstatus = "Pending";
                            if(groupSnapshot.child("Members").child(currentUserID).child("status").getValue().toString().equals("Accepted")){
                                groupmembershipstatus = "Joined";
                            }
                            joinedGroupList.add(new Groups(groupSnapshot.getKey(), groupname, profilepicstring, grouptype, groupstatus, groupcreatedate, groupmembershipstatus));
                        }
                        Collections.shuffle(joinedGroupList);
                        groupadapter.notifyDataSetChanged();
                        yourgroupsadapter.notifyDataSetChanged();

                        if(!groupList.isEmpty()){
                            groupspeopleheader.setVisibility(View.VISIBLE);
                            groupspeopleheader.setText("Groups with people you follow");
                        }else{
                            groupspeopleheader.setVisibility(View.VISIBLE);
                            groupspeopleheader.setText("Follow people to see what groups they follow");
                        }

                        if(!joinedGroupList.isEmpty()){
                            yourgroupsheader.setVisibility(View.VISIBLE);
                            yourgroupsheader.setText("Your Groups");
                        }else{
                            yourgroupsheader.setVisibility(View.VISIBLE);
                            yourgroupsheader.setText("Join groups to see them here");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void RetrieveUsers(){
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot suggestionSnapshot : dataSnapshot.getChildren()){
                    if(!suggestionSnapshot.getKey().equals(currentUserID)){
                        String suggestion = suggestionSnapshot.child("fullname").getValue(String.class);
                        String profilepicstring = "";
                        if (suggestionSnapshot.hasChild("profileimage"))
                            profilepicstring = suggestionSnapshot.child("profileimage").getValue().toString();
                        else
                            profilepicstring = "";

                        if(suggestion != null){
                            profileList.add(new SearchAutoComplete(suggestion, profilepicstring, suggestionSnapshot.getKey()));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void RetrieveCurrentUserDetails(){
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                current_user_name = dataSnapshot.child("fullname").getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void RetrieveGroupPosts() {
        Query SortPostsInDescendingOrder = PostsRef.orderByChild("counter");
        FirebaseRecyclerAdapter<GroupsPost, GroupFragment.PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<GroupsPost, GroupFragment.PostsViewHolder>(GroupsPost.class,
                        R.layout.all_posts_layout,
                        GroupFragment.PostsViewHolder.class,
                        SortPostsInDescendingOrder) {
                    @Override
                    protected void populateViewHolder(final GroupFragment.PostsViewHolder viewHolder, final GroupsPost posts, int position) {

                        final String PostKey = getRef(position).getKey();

                        UsersRef.child(posts.getUid()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    usernamehldr = dataSnapshot.child("fullname").getValue().toString();
                                    viewHolder.setFullname(dataSnapshot.child("fullname").getValue().toString());
                                    if (dataSnapshot.hasChild("profileimage"))
                                        viewHolder.setProfileimage(getActivity(), dataSnapshot.child("profileimage").getValue().toString());
                                    else
                                        viewHolder.setProfileimage(getActivity(), "");
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

                        viewHolder.optionMenuP.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //creating a popup menu
                                PopupMenu popup = new PopupMenu(getActivity(), viewHolder.optionMenuP);
                                //inflating menu from xml resource
                                popup.inflate(R.menu.post_menu);


                                //set enabled/disabled menu items
                                if(posts.getUid().equals(currentUserID)){
                                    popup.getMenu().findItem(R.id.post_view_menu).setVisible(true);
                                    popup.getMenu().findItem(R.id.post_edit_menu).setVisible(true);
                                    popup.getMenu().findItem(R.id.post_delete_menu).setVisible(true);
                                }else{
                                    popup.getMenu().findItem(R.id.post_view_menu).setVisible(true);
                                    popup.getMenu().findItem(R.id.post_edit_menu).setVisible(false);
                                    popup.getMenu().findItem(R.id.post_delete_menu).setVisible(false);
                                }

                                //adding click listener
                                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        switch (item.getItemId()) {
                                            case R.id.post_view_menu:
                                                Intent viewIntent = new Intent(getActivity(), SpecificPostActivity.class);
                                                viewIntent.putExtra("post_id", PostKey);
                                                viewIntent.putExtra("feed_type", "GroupFeed");
                                                startActivity(viewIntent);
                                                return true;
                                            case R.id.post_edit_menu:
                                                Intent clickPostIntent = new Intent(getActivity(), ClickPostActivity.class);
                                                clickPostIntent.putExtra("PostKey", PostKey);
                                                clickPostIntent.putExtra("from_feed", "GroupFeed");
                                                startActivity(clickPostIntent);
                                                return true;
                                            case R.id.post_delete_menu:
                                                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        switch (which){
                                                            case DialogInterface.BUTTON_POSITIVE:
                                                                PostsRef.child(PostKey).removeValue();
                                                                RetrieveGroupPosts();
                                                                Toast.makeText(getActivity(),"Post has been deleted.",Toast.LENGTH_SHORT).show();
                                                                break;
                                                        }
                                                    }
                                                };

                                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme);
                                                builder.setMessage("Are you sure you want to delete this post?")
                                                        .setPositiveButton("Yes", dialogClickListener)
                                                        .setNegativeButton("No", dialogClickListener);

                                                AlertDialog alert = builder.create();
                                                alert.setOnShowListener(arg0 -> {
                                                    alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
                                                    alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                                                });
                                                alert.show();
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
                                commentsIntent.putExtra("FeedType", "GroupFeed");
                                startActivity(commentsIntent);
                            }
                        });

                        viewHolder.DisplayNoOfLikes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent likesIntent = new Intent(getActivity(), LikesActivity.class);
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
                                                LikesRef.child(PostKey).child("Likes").child(currentUserID).child("isSeen").removeValue();
                                                LikesRef.child(PostKey).child("Likes").child(currentUserID).child("groupID").removeValue();
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
                                                LikesRef.child(PostKey).child("Likes").child(currentUserID).child("groupID").setValue(posts.getGroupid());
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
                                    if(snapshot.child("Members").child(currentUserID).exists()){

                                        grouppostsheader.setVisibility(View.VISIBLE);
                                        grouppostsheader.setText("Group Posts");

                                        viewHolder.mView.setVisibility(View.VISIBLE);
                                        viewHolder.lp.setVisibility(View.VISIBLE);
                                    }else{
                                        viewHolder.mView.setVisibility(View.GONE);
                                        viewHolder.lp.setVisibility(View.GONE);
                                    }

                                    viewHolder.username.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent profileIntent =  new Intent(getActivity(), ViewOthersProfile.class);
                                            profileIntent.putExtra("profileId",posts.getUid());
                                            getActivity().startActivity(profileIntent);
                                        }
                                    });

                                    viewHolder.image.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent profileIntent =  new Intent(getActivity(), ViewOthersProfile.class);
                                            profileIntent.putExtra("profileId",posts.getUid());
                                            getActivity().startActivity(profileIntent);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                };
        firebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                int itemCtr = firebaseRecyclerAdapter.getItemCount();
                groupPostList.scrollToPosition(itemCtr);
            }
        });
        groupPostList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        ImageButton LikeBtn;
        Button CommentBtn;
        TextView DisplayNoOfLikes, optionMenuP, username;
        CircleImageView image;
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

            username = (TextView) mView.findViewById(R.id.post_user_name);
            image = (CircleImageView) mView.findViewById(R.id.post_profile_image);

            LikesRef = FirebaseDatabase.getInstance().getReference().child("GroupLikes");
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

        public void setFullname(String fullname) {
            username.setText(fullname);
        }

        public void setProfileimage(Context ctx, String profileimage) {
            if (profileimage.equals(""))
                Picasso.with(ctx).load(R.drawable.profile).into(image);
            else
                Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(image);
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
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

        ProgressDialog loadingBar = new ProgressDialog(getActivity());

        if (requestCode==Gallery_Pick && resultCode==getActivity().RESULT_OK && data!=null){

            if(gallery_type.equals("profile")){
                ImageUri = data.getData();
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait, while we are updating your Profile Image...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                group_image.setImageURI(ImageUri);
            }else if(gallery_type.equals("post")){
                PostImageUri = data.getData();
                loadingBar.setTitle("Post Image");
                loadingBar.setMessage("Please wait, while we are saving your Post Image...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                SelectPostImage.setImageURI(PostImageUri);
            }

            loadingBar.dismiss();
        }
    }

}
