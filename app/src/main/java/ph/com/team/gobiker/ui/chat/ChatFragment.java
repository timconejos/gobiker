package ph.com.team.gobiker.ui.chat;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import ph.com.team.gobiker.NavActivity;
import ph.com.team.gobiker.R;
import ph.com.team.gobiker.ui.search.SearchAutoComplete;
import ph.com.team.gobiker.ui.search.SearchAutoCompleteAdapter;

public class ChatFragment extends Fragment {

    private ChatViewModel chatViewModel;
    private FirebaseAuth mAuth;
    private DatabaseReference MessagesRef,UsersRef, GroupChatRef;
    private String currentUserID, vid;
    private Button newChatBtn;
    public boolean fragmentActive = false;
    public ChatGroupActivity gcActivity;
    public ChatActivity chatActivity;

    //new chat variables
    private List<SearchAutoComplete> profileList;
    private RecyclerView profileSelectedView;
    private ChatSearchAdapter profileadapter;
    private List<ChatProfile> profileSelectedList;


    EditText gc_name;
    CircleImageView gc_image;

    //chat list variables
    private RecyclerView chatList;
    private ChatRecyclerAdapter chatadapter;
    private List<FindChat> chatitems;
    private int chatctr = 0;
    private ArrayList<String> idTempArr;

    //attachments variables
    final static int Gallery_Pick = 1;
    private Uri ImageUri;

    @Override
    public void onResume() {
        super.onResume();
        fragmentActive = true;
        mListener.setChatFragmentStatus(fragmentActive);
        chatNotifListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        fragmentActive = false;
        mListener.setChatFragmentStatus(fragmentActive);
    }

    @Override
    public void onStop(){
        super.onStop();
        fragmentActive = false;
        mListener.setChatFragmentStatus(fragmentActive);
    }

    public interface Listener {
        public void setChatFragmentStatus(boolean fragStatus);
        public void passChatCtr(int chatctr, String from, String message, String messageid, String chatid, String chattype);
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
        chatViewModel =
                ViewModelProviders.of(this).get(ChatViewModel.class);
        View root = inflater.inflate(R.layout.fragment_chat, container, false);
        final TextView textView = root.findViewById(R.id.text_chat);
        chatViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        newChatBtn = root.findViewById(R.id.new_convo);
        newChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewChatDialog alert = new ViewChatDialog();
                alert.showDialog(getActivity());
            }
        });

        InitializeVariables();
        chatNotifListener();

        chatList = (RecyclerView) root.findViewById(R.id.all_users_msgs_list);

        //new chat users array
        profileList = new ArrayList<>();

        //chat messages adapter
        chatList.setHasFixedSize(true);
        chatList.setNestedScrollingEnabled(false);
        chatList.setItemViewCacheSize(20);
        chatList.setDrawingCacheEnabled(true);
        chatList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        chatList.setLayoutManager(new LinearLayoutManager(getActivity()));
        chatitems = new ArrayList<>();
        chatadapter = new ChatRecyclerAdapter(chatitems, getActivity());
        chatList.setAdapter(chatadapter);
        chatadapter.notifyDataSetChanged();

        vid = "";

        RetrieveUsers();
        RetrieveAllUsersMsgs();

        return root;
    }

    public void InitializeVariables(){
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            currentUserID = mAuth.getCurrentUser().getUid();
            UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
            GroupChatRef = FirebaseDatabase.getInstance().getReference().child("GroupChats");

            if(currentUserID != null){
                MessagesRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(currentUserID);
            }
        }
    }

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
                    if(profileSelectedList.size() == 1){
                        Intent chatIntent = new Intent(activity,ChatActivity.class);
                        chatIntent.putExtra("visit_user_id",profileSelectedList.get(0).getUid());
                        chatIntent.putExtra("from", profileSelectedList.get(0).getUid());
                        startActivity(chatIntent);
                    }else{
                        dialog.hide();
                        CreateGroupChatDialog gcalertdialog = new CreateGroupChatDialog();
                        gcalertdialog.showDialog(getActivity(), profileSelectedList, dialog);
                    }

                }
            });

            dialog.show();
        }
    }

    public class CreateGroupChatDialog {
        public void showDialog(Activity activity, List<ChatProfile> gcparticipants, Dialog chatdialog){
            final Dialog gcdialog = new Dialog(activity);
            gcdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            gcdialog.setCancelable(false);
            gcdialog.setContentView(R.layout.dialog_new_gc);

            gc_name = (EditText) gcdialog.findViewById(R.id.setup_full_name);
            gc_image = (CircleImageView) gcdialog.findViewById(R.id.setup_gc_profile_image);

            Button close_button = (Button) gcdialog.findViewById(R.id.new_gc_close);
            Button next_button = (Button) gcdialog.findViewById(R.id.new_gc_proceed);

            gc_image.setOnClickListener(new View.OnClickListener() {
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
                    chatdialog.show();
                    gcdialog.dismiss();
                }
            });

            next_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatabaseReference GcRootRef = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference gcRef = GcRootRef.child("GroupChats");
                    StorageReference gcImageRef;
                    String gcKey = gcRef.push().getKey();

                    gcImageRef = FirebaseStorage.getInstance().getReference().child("gc_picture");

                    ProgressDialog loadingBar = new ProgressDialog(getActivity());
                    loadingBar.setTitle("Group Chat");
                    loadingBar.setMessage("Please wait, while we are creating your Group Chat...");
                    loadingBar.setCanceledOnTouchOutside(true);
                    loadingBar.show();

                    if(ImageUri != null){
                        final StorageReference filePath = gcImageRef.child(gcKey+".jpg");
                        filePath.putFile(ImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String downloadUrl = uri.toString();
                                        writeDataToDatabase(
                                                downloadUrl,
                                                gcKey,
                                                GcRootRef,
                                                gcparticipants,
                                                chatdialog,
                                                gcdialog,
                                                activity,
                                                loadingBar);

                                    }
                                });
                            }
                        });
                    }else{
                        writeDataToDatabase(
                                "",
                                gcKey,
                                GcRootRef,
                                gcparticipants,
                                chatdialog,
                                gcdialog,
                                activity,
                                loadingBar);
                    }

                }
            });

            gcdialog.show();

        }

        private void writeDataToDatabase(String picUrl,
                                         String gcKey,
                                         DatabaseReference GcRootRef,
                                         List<ChatProfile> gcparticipants,
                                         Dialog chatdialog,
                                         Dialog gcdialog,
                                         Activity activity,
                                         ProgressDialog loadingBar){
            String saveCurrentDate, saveCurrentTime;

            DatabaseReference gcRef = GcRootRef.child("GroupChats");

            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("MM-dd-yyyy");
            saveCurrentDate = currentDate.format(calForDate.getTime());
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
            saveCurrentTime = currentTime.format(calForDate.getTime());

            String message_sender_ref = "Messages/" + currentUserID + "/" +  gcKey;
            DatabaseReference user_message_key = GcRootRef.child("Messages").child(currentUserID)
                    .child(gcKey).push();
            String message_push_id = user_message_key.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", "A new group chat has been made.");
            messageTextBody.put("time",saveCurrentTime);
            messageTextBody.put("date",saveCurrentDate);
            messageTextBody.put("type","text");
            messageTextBody.put("from",currentUserID);
            messageTextBody.put("isSeen", false);


            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(message_sender_ref+"/"+message_push_id,messageTextBody);

            ArrayList<String> userids = new ArrayList<String>();
            userids.add(currentUserID);

            for(int x=0; x<gcparticipants.size(); x++){
                if(!gcparticipants.get(x).getUid().equals(currentUserID)){
                    String message_receiver_ref = "Messages/" + gcparticipants.get(x).getUid() + "/" +  gcKey;
                    messageBodyDetails.put(message_receiver_ref+"/"+message_push_id,messageTextBody);
                }
                userids.add(gcparticipants.get(x).getUid());
            }

            gcRef.child(gcKey).child("gc_name").setValue(gc_name.getText().toString().trim());
            if(!picUrl.equals("")){
                gcRef.child(gcKey).child("gc_picture").setValue(picUrl);
            }
            gcRef.child(gcKey).child("gc_participants").setValue(userids);

            GcRootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        loadingBar.dismiss();
                        chatdialog.dismiss();;
                        gcdialog.dismiss();
                        Intent profileIntent =  new Intent(getActivity().getApplicationContext(), ChatGroupActivity.class);
                        profileIntent.putExtra("gcKey", gcKey);
                        profileIntent.putExtra("from", "chatfragment");
                        startActivity(profileIntent);
                    }
                    else{
                        loadingBar.dismiss();
                        Toast.makeText(activity,"Error: "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }

    private void RetrieveUsers(){
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                profileList.clear();
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

    public void chatNotifListener(){
        if(mAuth.getCurrentUser() != null){
            MessagesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(!gcActivity.chatNotifier && !chatActivity.chatNotifier){
                        chatctr = 0;
                        idTempArr = new ArrayList<>();
                        for(DataSnapshot idsnapshot : snapshot.getChildren()) {
                            Query q = MessagesRef.child(idsnapshot.getKey()).orderByKey().limitToLast(1);
                            q.addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(@NonNull DataSnapshot childSnapshot, @Nullable String previousChildName) {
                                    UsersRef.child(childSnapshot.child("from").getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                boolean isSeen;
                                                if(!idTempArr.contains(childSnapshot.getKey()+","+childSnapshot.child("from").getValue().toString())){
                                                    if(dataSnapshot.hasChild("fullname")){
                                                        isSeen = (boolean) childSnapshot.child("isSeen").getValue();
                                                        if(!isSeen){
                                                            if(!childSnapshot.child("from").getValue().toString().equals(currentUserID)){
                                                                UsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                        chatctr++;
                                                                        if(snapshot.hasChild(idsnapshot.getKey())){
                                                                            mListener.passChatCtr(chatctr, dataSnapshot.child("fullname").getValue().toString(), childSnapshot.child("message").getValue().toString(), childSnapshot.getKey(), idsnapshot.getKey(), "single");
                                                                        }else{
                                                                            mListener.passChatCtr(chatctr, dataSnapshot.child("fullname").getValue().toString(), childSnapshot.child("message").getValue().toString(), childSnapshot.getKey(), idsnapshot.getKey(), "group");
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                    }
                                                                });
                                                            }else{
                                                                mListener.passChatCtr(chatctr, "none", "", "", "", "");
                                                            }
                                                        }else{
                                                            mListener.passChatCtr(chatctr, "none", "", "", "", "");
                                                        }
                                                        idTempArr.add(childSnapshot.getKey()+","+childSnapshot.child("from").getValue().toString());
                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }

                                @Override
                                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                }

                                @Override
                                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                                }

                                @Override
                                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }else{
                        chatctr = 0;
                        idTempArr = new ArrayList<>();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }


    private void RetrieveAllUsersMsgs(){
        MessagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!chatitems.isEmpty()){
                    chatitems.clear();
                    chatadapter.notifyDataSetChanged();
                }
                for(DataSnapshot idsnapshot : snapshot.getChildren()) {
                    UsersRef.child(idsnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                if(dataSnapshot.hasChild("fullname")){
                                    Query q = MessagesRef.child(idsnapshot.getKey()).orderByKey().limitToLast(1);
                                    q.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot querySnapshot) {
                                            for (DataSnapshot childSnapshot: querySnapshot.getChildren()) {
                                                String profilepicexists = "";

                                                if (dataSnapshot.hasChild("profileimage"))
                                                    profilepicexists = dataSnapshot.child("profileimage").getValue().toString();

                                                String datetimehldr = "";
                                                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                                                SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");
                                                try{
                                                    Date convertedDate = format.parse(childSnapshot.child("date").getValue().toString());
                                                    String newDateHldr = DateFormat.getDateInstance(DateFormat.MEDIUM).format(convertedDate);

                                                    Date date3 = sdf.parse(childSnapshot.child("time").getValue().toString());
                                                    SimpleDateFormat sdf2 = new SimpleDateFormat("hh:mm aa");
                                                    datetimehldr = newDateHldr+" "+sdf2.format(date3);
                                                }catch(ParseException e){
                                                    e.printStackTrace();
                                                }

                                                String finalDatetimehldr = datetimehldr;

                                                boolean isSeen;
                                                if(childSnapshot.child("isSeen").exists()){
                                                    isSeen = (boolean) childSnapshot.child("isSeen").getValue();
                                                }else{
                                                    isSeen = true;
                                                }

                                                FindChat chatItem = new FindChat(
                                                        idsnapshot.getKey(), profilepicexists, dataSnapshot.child("fullname").getValue().toString(), childSnapshot.child("message").getValue().toString(), finalDatetimehldr, "single", isSeen
                                                );
                                                if(!chatitems.contains(chatItem)){
                                                    chatitems.add(chatItem);
                                                }
                                                Collections.sort(chatitems, new TimeStampComparator());
                                                Collections.reverse(chatitems);
                                                chatadapter.notifyDataSetChanged();
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

                    GroupChatRef.child(idsnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String grouppicexists = "";
                                GroupChats gcdata = dataSnapshot.getValue(GroupChats.class);

                                if (dataSnapshot.hasChild("gc_picture"))
                                    grouppicexists = gcdata.getGc_picture();

                                Query lastQuery = MessagesRef.child(idsnapshot.getKey()).orderByKey().limitToLast(1);
                                String finalGrouppicexists = grouppicexists;
                                lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot groupquerysnapshot) {
                                        for (DataSnapshot childGroupSnapshot: groupquerysnapshot.getChildren()) {
                                            String datetimehldr = "";
                                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                                            SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");
                                            try{
                                                Date convertedDate = format.parse(childGroupSnapshot.child("date").getValue().toString());
                                                String newDateHldr = DateFormat.getDateInstance(DateFormat.MEDIUM).format(convertedDate);

                                                Date date3 = sdf.parse(childGroupSnapshot.child("time").getValue().toString());
                                                SimpleDateFormat sdf2 = new SimpleDateFormat("hh:mm aa");
                                                datetimehldr = newDateHldr + " " + sdf2.format(date3);
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }

                                            String finalDatetimehldr = datetimehldr;

                                            boolean isSeen;
                                            if(childGroupSnapshot.child("isSeen").exists()){
                                                isSeen = (boolean) childGroupSnapshot.child("isSeen").getValue();
                                            }else{
                                                isSeen = true;
                                            }

                                            FindChat chatItem = new FindChat(
                                                    idsnapshot.getKey(), finalGrouppicexists, gcdata.getGc_name(), childGroupSnapshot.child("message").getValue().toString(), finalDatetimehldr, "group", isSeen
                                            );
                                            if(!chatitems.contains(chatItem)){
                                                chatitems.add(chatItem);
                                            }
                                            Collections.sort(chatitems, new TimeStampComparator());
                                            Collections.reverse(chatitems);
                                            chatadapter.notifyDataSetChanged();
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
     public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ProgressDialog loadingBar = new ProgressDialog(getActivity());

        if (requestCode==Gallery_Pick && resultCode==getActivity().RESULT_OK && data!=null){
            ImageUri = data.getData();
            loadingBar.setTitle("Profile Image");
            loadingBar.setMessage("Please wait, while we are updating your Profile Image...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            gc_image.setImageURI(ImageUri);

            loadingBar.dismiss();
        }
    }

    public class TimeStampComparator implements Comparator<FindChat> {
        public int compare(FindChat left, FindChat right) {
            SimpleDateFormat datetimeformat = new SimpleDateFormat("dd MMM yyyy hh:mm aa");
            SimpleDateFormat datetimeformat2 = new SimpleDateFormat("MM-dd-yyyy hh:mm aa");
            Date dateHldrL = new Date();
            Date dateHldrR = new Date();
            String newDateL = "";
            String newDateR = "";
            try{
                dateHldrL = datetimeformat.parse(left.getDatetime());
                dateHldrR = datetimeformat.parse(right.getDatetime());

                newDateL = datetimeformat2.format(dateHldrL);
                newDateR = datetimeformat2.format(dateHldrR);

            }catch(ParseException e){
                e.printStackTrace();
            }
            return newDateL.compareTo(newDateR);
        }
    }
}
