package ph.com.team.gobiker.ui.notifications;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ph.com.team.gobiker.Comments;
import ph.com.team.gobiker.Likes;
import ph.com.team.gobiker.R;
import ph.com.team.gobiker.ui.home.Posts;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef, LikesRef, GroupsRef, GroupPostsRef, GroupLikesRef;
    private String currentUserID;
    private RecyclerView notificationList;
    private RecyclerAdapter adapter;
    private List<Notifications> listItems;
    private ArrayList<NotificationSeenCheck> notificationSeenItems;
    public boolean fragmentActive = false;

    @Override
    public void onResume() {
        super.onResume();
        fragmentActive = true;
        mListener.setFragmentStatus(fragmentActive);
        if(listItems != null){
            listItems.clear();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        fragmentActive = false;
        mListener.setFragmentStatus(fragmentActive);
        if(listItems != null){
            listItems.clear();
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        fragmentActive = false;
        mListener.setFragmentStatus(fragmentActive);
        if(listItems != null){
            listItems.clear();
        }
    }

    public interface Listener {
        public void setFragmentStatus(boolean fragStatus);
        public void passNotifCtr(ArrayList<NotificationSeenCheck> notifarr);
    }

    private Listener mListener;

    public void setListener(Listener listener) {
        mListener = listener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (Listener) context;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                ViewModelProviders.of(this).get(NotificationsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        final TextView textView = root.findViewById(R.id.text_notifications);
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
//                textView.setText(s);
            }
        });

        initializeVariables();

        notificationList = (RecyclerView) root.findViewById(R.id.all_notifications_list);
        notificationList.setHasFixedSize(true);
        notificationList.setLayoutManager(new LinearLayoutManager(getActivity()));
        listItems = new ArrayList<>();
        adapter = new RecyclerAdapter(listItems, getActivity());
        notificationList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        notificationListener("fromfragment");
        return root;
    }

    public void initializeVariables(){
        mAuth = FirebaseAuth.getInstance();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        GroupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        GroupPostsRef = FirebaseDatabase.getInstance().getReference().child("GroupPosts");
        currentUserID = mAuth.getCurrentUser().getUid();

        listItems = new ArrayList<>();
        notificationSeenItems = new ArrayList<>();
    }

    public void notificationListener(String fromtype){
        PostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                checkPostActivities(fromtype);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        LikesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                checkPostActivities(fromtype);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                checkPostActivities(fromtype);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        GroupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                checkPostActivities(fromtype);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        GroupPostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                checkPostActivities(fromtype);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void checkPostActivities(String fromtype){
        listItems.clear();
        notificationSeenItems.clear();
        mListener.passNotifCtr(notificationSeenItems);

        PostsRef.orderByChild("counter").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                            for(DataSnapshot postKeySnapshot: postSnapshot.getChildren()){
                                if(postKeySnapshot.exists()){
                                    for(DataSnapshot commentSnapshot: postKeySnapshot.getChildren()){
                                        if(commentSnapshot.exists()){
                                            Posts post = postSnapshot.getValue(Posts.class);
                                            Comments comment = commentSnapshot.getValue(Comments.class);
                                            if(currentUserID.equals(post.getUid())){
                                                UsersRef.child(comment.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.exists()) {
                                                            String profilepicstring = "";
                                                            boolean isSeenVal;
                                                            if (dataSnapshot.hasChild("profileimage"))
                                                                profilepicstring = dataSnapshot.child("profileimage").getValue().toString();
                                                            else
                                                                profilepicstring = "";

                                                            if(commentSnapshot.hasChild("isSeen")){
                                                                isSeenVal = comment.getSeen();

                                                            }else{
                                                                isSeenVal = true;
                                                            }

                                                            //check if call is from this fragment or the main nav activity
                                                            if(fromtype.equals("fromfragment")){
                                                                Notifications listItem = new Notifications(
                                                                        comment.getUid(), comment.getTime(), comment.getDate(), comment.getUsername()+" commented on your post: "+comment.getComments(), profilepicstring, "comment", postSnapshot.getKey(), isSeenVal
                                                                );

                                                                if(!listItems.contains(listItem)){
                                                                    listItems.add(listItem);
                                                                    adapter.notifyDataSetChanged();
                                                                }

                                                                // check if NotificationsFragment is currently opened. IF YES update is SEEN value
                                                                if(fragmentActive){
                                                                    PostsRef.child(postSnapshot.getKey()).child("Comments").child(commentSnapshot.getKey()).child("isSeen").setValue(true);
                                                                }

                                                            }else{
                                                                if(!isSeenVal){
                                                                    NotificationSeenCheck seenItem = new NotificationSeenCheck(
                                                                            comment.getUsername()+" commented on your post: "+comment.getComments(), isSeenVal
                                                                    );
                                                                    if(!notificationSeenItems.contains(seenItem)){
                                                                        notificationSeenItems.add(seenItem);
                                                                        mListener.passNotifCtr(notificationSeenItems);
                                                                    }

                                                                }
                                                            }


                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        }
                                    }
                                }
                            }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        LikesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot idSnapshot: dataSnapshot.getChildren()) {
                        PostsRef.child(idSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot postsnapshot) {
                                if(postsnapshot.exists()){
                                    if(postsnapshot.child("uid").getValue().toString().equals(currentUserID)){
                                        for(DataSnapshot likeHldrSnap: idSnapshot.getChildren()){
                                            for(DataSnapshot likesSnapshot: likeHldrSnap.getChildren()){
                                                Likes like = likesSnapshot.getValue(Likes.class);
                                                UsersRef.child(likesSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.exists()) {
                                                            String fullnamestring = "";
                                                            String profilepicstring = "";
                                                            boolean isSeenVal;
                                                            fullnamestring = dataSnapshot.child("fullname").getValue().toString();
                                                            if (dataSnapshot.hasChild("profileimage"))
                                                                profilepicstring = dataSnapshot.child("profileimage").getValue().toString();
                                                            else
                                                                profilepicstring = "";

                                                            String timestamphldr = "";
                                                            if(likesSnapshot.child("Timestamp").exists()){
                                                                timestamphldr = likesSnapshot.child("Timestamp").getValue().toString();
                                                            }else {
                                                                timestamphldr="-- --";
                                                            }

                                                            if(likesSnapshot.hasChild("isSeen")){
                                                                isSeenVal = like.isSeen();
                                                            }else{
                                                                isSeenVal = true;
                                                            }

                                                            //check if call is from this fragment or the main nav activity
                                                            if(fromtype.equals("fromfragment")){
                                                                if(likesSnapshot.child("groupID").exists()){
                                                                    String finalTimestamphldr = timestamphldr;
                                                                    String finalFullnamestring = fullnamestring;
                                                                    String finalProfilepicstring = profilepicstring;
                                                                    GroupsRef.child(likesSnapshot.child("groupID").getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(@NonNull DataSnapshot groupsnapshot) {
                                                                            Notifications listItem = new Notifications(
                                                                                    likesSnapshot.getKey(), finalTimestamphldr.split(" ")[1], finalTimestamphldr.split(" ")[0], finalFullnamestring +" liked your post in "+groupsnapshot.child("group_name").getValue().toString(), finalProfilepicstring, "like", idSnapshot.getKey(), isSeenVal
                                                                            );

                                                                            if(!listItems.contains(listItem)){
                                                                                listItems.add(listItem);
                                                                                Collections.sort(listItems, new TimeStampComparator());
                                                                                Collections.reverse(listItems);
                                                                                adapter.notifyDataSetChanged();
                                                                            }
                                                                        }

                                                                        @Override
                                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                                        }
                                                                    });
                                                                }else{
                                                                    Notifications listItem = new Notifications(
                                                                            likesSnapshot.getKey(), timestamphldr.split(" ")[1], timestamphldr.split(" ")[0], fullnamestring+" liked your post.", profilepicstring, "like", idSnapshot.getKey(), isSeenVal
                                                                    );

                                                                    if(!listItems.contains(listItem)){
                                                                        listItems.add(listItem);
                                                                        Collections.sort(listItems, new TimeStampComparator());
                                                                        Collections.reverse(listItems);
                                                                        adapter.notifyDataSetChanged();
                                                                    }
                                                                }

                                                                // check if NotificationsFragment is currently opened. IF YES update is SEEN value
                                                                if(fragmentActive){
                                                                    LikesRef.child(postsnapshot.getKey()).child("Likes").child(likesSnapshot.getKey()).child("isSeen").setValue(true);
                                                                }

                                                            }else{
                                                                if(!isSeenVal){
                                                                    NotificationSeenCheck seenItem = new NotificationSeenCheck(
                                                                            fullnamestring+" liked your post.", isSeenVal
                                                                    );
                                                                    if(!notificationSeenItems.contains(seenItem)){
                                                                        notificationSeenItems.add(seenItem);
                                                                        mListener.passNotifCtr(notificationSeenItems);
                                                                    }
                                                                }
                                                            }

                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot idsnapshot: snapshot.getChildren()) {
                        if(!idsnapshot.getKey().equals(currentUserID)){
                            if(idsnapshot.hasChild("following")){
                                if(idsnapshot.child("following").hasChild(currentUserID)){
                                    UsersRef.child(idsnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                String fullnamestring = "";
                                                String profilepicstring = "";
                                                String timestamphldr = "";
                                                boolean isSeenVal;
                                                fullnamestring = dataSnapshot.child("fullname").getValue().toString();
                                                if (dataSnapshot.hasChild("profileimage"))
                                                    profilepicstring = dataSnapshot.child("profileimage").getValue().toString();
                                                else
                                                    profilepicstring = "";


                                                if(idsnapshot.child("following").child(currentUserID).hasChild("Timestamp")){
                                                    timestamphldr = idsnapshot.child("following").child(currentUserID).child("Timestamp").getValue().toString();
                                                }else {
                                                    timestamphldr="-- --";
                                                }

                                                if(idsnapshot.child("following").child(currentUserID).hasChild("isSeen")){
                                                    isSeenVal = (boolean) idsnapshot.child("following").child(currentUserID).child("isSeen").getValue();
                                                    if(!isSeenVal){

                                                    }
                                                }else{
                                                    isSeenVal = true;
                                                }

                                                Notifications listItem = new Notifications(
                                                        idsnapshot.getKey(), timestamphldr.split(" ")[1], timestamphldr.split(" ")[0], fullnamestring+" now follows you", profilepicstring, "follow", "n/a", isSeenVal
                                                );

                                                //check if call is from this fragment or the main nav activity
                                                if(fromtype.equals("fromfragment")){
                                                    if(!listItems.contains(listItem)){
                                                        listItems.add(listItem);
                                                        Collections.sort(listItems, new TimeStampComparator());
                                                        Collections.reverse(listItems);
                                                        adapter.notifyDataSetChanged();
                                                    }

                                                    // check if NotificationsFragment is currently opened. IF YES update is SEEN value
                                                    if(fragmentActive){
                                                        UsersRef.child(idsnapshot.getKey()).child("following").child(currentUserID).child("isSeen").setValue(true);
                                                    }

                                                }else{
                                                    if(!isSeenVal){
                                                        NotificationSeenCheck seenItem = new NotificationSeenCheck(
                                                                fullnamestring+" now follows you", isSeenVal
                                                        );
                                                        if(!notificationSeenItems.contains(seenItem)){
                                                            notificationSeenItems.add(seenItem);
                                                            mListener.passNotifCtr(notificationSeenItems);
                                                        }
                                                    }
                                                }

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        GroupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot idsnapshot : snapshot.getChildren()) {
                        if(idsnapshot.child("Members").hasChild(currentUserID)){
                            if(idsnapshot.child("group_type").getValue().toString().equals("Private")){
                                GroupsRef.child(idsnapshot.getKey()).child("Members").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot groupmembersnap) {
                                        for (DataSnapshot groupusersnapshot : groupmembersnap.getChildren()) {
                                            if(!groupusersnapshot.getKey().equals(currentUserID)){
                                                if(groupusersnapshot.child("status").getValue().toString().equals("Pending")){
                                                    UsersRef.child(groupusersnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot profilesnapshot) {
                                                            if(profilesnapshot.exists()){
                                                                String profilepicstring = "";
                                                                boolean isSeenVal = false;
                                                                if (profilesnapshot.hasChild("profileimage"))
                                                                    profilepicstring = profilesnapshot.child("profileimage").getValue().toString();
                                                                else
                                                                    profilepicstring = "";

                                                                Log.w("username", profilesnapshot.child("fullname").getValue().toString());

                                                                if(fromtype.equals("fromfragment")){
                                                                    Notifications listItem = new Notifications(
                                                                            groupusersnapshot.getKey(), "--", "--", profilesnapshot.child("fullname").getValue().toString()+" has a join request on your group: "+idsnapshot.child("group_name").getValue().toString(), profilepicstring, "group_join", idsnapshot.getKey(), isSeenVal
                                                                    );

                                                                    if(!listItems.contains(listItem)){
                                                                        listItems.add(listItem);
                                                                        Collections.sort(listItems, new TimeStampComparator());
                                                                        Collections.reverse(listItems);
                                                                        adapter.notifyDataSetChanged();
                                                                    }

                                                                }else{
                                                                    if(!isSeenVal){
                                                                        NotificationSeenCheck seenItem = new NotificationSeenCheck(
                                                                                profilesnapshot.child("fullname").getValue().toString()+" has a join request on your group: "+idsnapshot.child("group_name").getValue().toString(), isSeenVal
                                                                        );
                                                                        if(!notificationSeenItems.contains(seenItem)){
                                                                            notificationSeenItems.add(seenItem);
                                                                            mListener.passNotifCtr(notificationSeenItems);
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                                }
                                            }

                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }

//                            GroupPostsRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                    for (DataSnapshot postSnapshot: snapshot.getChildren()) {
//                                        for (DataSnapshot postKeySnapshot : postSnapshot.getChildren()) {
//                                            if (postKeySnapshot.exists()) {
//                                                for (DataSnapshot commentSnapshot : postKeySnapshot.getChildren()) {
//                                                    if (commentSnapshot.exists()) {
//                                                        Posts post = postSnapshot.getValue(Posts.class);
//                                                        Comments comment = commentSnapshot.getValue(Comments.class);
//                                                        if(currentUserID.equals(post.getUid())){
//                                                            UsersRef.child(comment.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
//                                                                @Override
//                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                                                    if (dataSnapshot.exists()) {
//                                                                        String profilepicstring = "";
//                                                                        boolean isSeenVal;
//                                                                        if (dataSnapshot.hasChild("profileimage"))
//                                                                            profilepicstring = dataSnapshot.child("profileimage").getValue().toString();
//                                                                        else
//                                                                            profilepicstring = "";
//
//                                                                        if(commentSnapshot.hasChild("isSeen")){
//                                                                            isSeenVal = comment.getSeen();
//
//                                                                        }else{
//                                                                            isSeenVal = true;
//                                                                        }
//
//                                                                        //check if call is from this fragment or the main nav activity
//                                                                        if(fromtype.equals("fromfragment")){
//                                                                            Notifications listItem = new Notifications(
//                                                                                    comment.getUid(), comment.getTime(), comment.getDate(), comment.getUsername()+" commented on your post: "+comment.getComments(), profilepicstring, "comment", postSnapshot.getKey(), isSeenVal
//                                                                            );
//
//                                                                            if(!listItems.contains(listItem)){
//                                                                                listItems.add(listItem);
//                                                                                adapter.notifyDataSetChanged();
//                                                                            }
//
//                                                                            // check if NotificationsFragment is currently opened. IF YES update is SEEN value
//                                                                            if(fragmentActive){
//                                                                                GroupPostsRef.child(postSnapshot.getKey()).child("Comments").child(commentSnapshot.getKey()).child("isSeen").setValue(true);
//                                                                            }
//
//                                                                        }else{
//                                                                            if(!isSeenVal){
//                                                                                NotificationSeenCheck seenItem = new NotificationSeenCheck(
//                                                                                        comment.getUsername()+" commented on your post: "+comment.getComments(), isSeenVal
//                                                                                );
//                                                                                if(!notificationSeenItems.contains(seenItem)){
//                                                                                    notificationSeenItems.add(seenItem);
//                                                                                    mListener.passNotifCtr(notificationSeenItems);
//                                                                                }
//                                                                            }
//                                                                        }
//
//
//                                                                    }
//                                                                }
//
//                                                                @Override
//                                                                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                                                                }
//                                                            });
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError error) {
//
//                                }
//                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public class TimeStampComparator implements Comparator<Notifications> {
        public int compare(Notifications left, Notifications right) {
            return left.getDate().compareTo(right.getDate());
        }
    }

}
