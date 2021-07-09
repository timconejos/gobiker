package ph.com.team.gobiker.ui.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import ph.com.team.gobiker.NavActivity;
import ph.com.team.gobiker.R;
import ph.com.team.gobiker.ui.search.SearchAutoComplete;
import ph.com.team.gobiker.ui.search.SearchAutoCompleteAdapter;

public class ChatGroupActivity extends AppCompatActivity {
    private Toolbar ChattoolBar;
    private TextView ChatNotice;
    private ImageButton SendMessageButton, SendImagefileButton;
    private EditText userMessageInput;
    private RecyclerView userMessagesList;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messageAdapter;


    private String messageReceiverID, messageSenderName, messageSenderID, saveCurrentDate, saveCurrentTime;
    private List<String> messageReceivers = new ArrayList<>();
    private String gcKey, activityFrom;

    private TextView receiverName, userLastSeen;
    private CircleImageView receiverProfileImage;
    private DatabaseReference RootRef, UsersRef, GroupChatsRef;
    private FirebaseAuth mAuth;

    //new chat dialog
    private List<SearchAutoComplete> profileList;
    private RecyclerView profileSelectedView;
    private ChatSearchAdapter profileadapter;
    private List<ChatProfile> profileSelectedList;

    private String gc_name_hldr, gc_picture_hldr;
    private String curr_name_hldr;
    private EditText edit_gc_name;
    private CircleImageView edit_gc_picture;

    //chat attachments variables
    private String gallery_type;
    final static int PICTURE_RESULT = 0;
    final static int GALLERY_PICK = 1;
    final static int FILE_RESULT = 123;
    private Uri ImageUri;
    private Uri cameraUri;
    ContentValues values;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private ChildEventListener ChildEventListener;
    public static boolean chatNotifier = false;
    private NavActivity navActivity;

    @Override
    public void onPause() {
        super.onPause();
        chatNotifier = false;
        RootRef.child("Messages").child(messageSenderID).child(gcKey).removeEventListener(ChildEventListener);
    }

    @Override
    public void onStop(){
        super.onStop();
        chatNotifier = false;
        RootRef.child("Messages").child(messageSenderID).child(gcKey).removeEventListener(ChildEventListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chatNotifier = true;

        RootRef = FirebaseDatabase.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupChatsRef = FirebaseDatabase.getInstance().getReference().child("GroupChats");
        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        gcKey = (String) getIntent().getExtras().get("gcKey");
        activityFrom = (String) getIntent().getExtras().get("from");

        if(activityFrom.equals("notification")){
            if(navActivity.globalchatctr != 0){
                navActivity.navView.getOrCreateBadge(R.id.navigation_chat).setNumber(navActivity.globalchatctr - 1);
            }
            if(navActivity.globalchatctr - 1 == 0){
                navActivity.navView.removeBadge(R.id.navigation_chat);
            }
        }
        
        InitializeFields();
        DisplayReceiverInfo();

        SendImagefileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Choose a File", "Cancel"};

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatGroupActivity.this);
                builder.setTitle("File Upload");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (checkPermissionREAD_EXTERNAL_STORAGE(ChatGroupActivity.this)) {
                            values = new ContentValues();
                            values.put(MediaStore.Images.Media.TITLE, "New Picture");
                            values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                            cameraUri = getContentResolver().insert(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                            if (options[item].equals("Take Photo")) {
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
                                startActivityForResult(intent, PICTURE_RESULT);
                            } else if (options[item].equals("Choose from Gallery")) {
                                gallery_type = "gc_picture_attachment";
                                Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(pickPhoto, GALLERY_PICK);

                            } else if (options[item].equals("Choose a File")) {
                                Intent intent = new Intent()
                                        .setType("*/*")
                                        .setAction(Intent.ACTION_GET_CONTENT);
                                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                startActivityForResult(Intent.createChooser(intent, "Select a file"), 123);

                            } else if (options[item].equals("Cancel")) {
                                dialog.dismiss();
                            }
                        }else{
                            showDialog("External storage", ChatGroupActivity.this,
                                    Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                    }
                });
                builder.show();
            }
        });

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });

        FetchMessages();
    }

    private void FetchMessages() {
        ChildEventListener =  RootRef.child("Messages").child(messageSenderID).child(gcKey)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if (dataSnapshot.exists()){
                            Messages messages = dataSnapshot.getValue(Messages.class);
                            messagesList.add(messages);
                            Collections.sort(messagesList, new TimeStampComparator());
                            userMessagesList.smoothScrollToPosition(messageAdapter.getItemCount());
                            messageAdapter.notifyDataSetChanged();

                            if(dataSnapshot.child("isSeen").exists()){
                                if(!(boolean) dataSnapshot.child("isSeen").getValue()){
                                    RootRef.child("Messages").child(messageSenderID).child(gcKey).child(dataSnapshot.getKey()).child("isSeen").setValue(true);
                                }
                            }
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public class TimeStampComparator implements Comparator<Messages> {
        public int compare(Messages left, Messages right) {
            return left.getDate().compareTo(right.getDate());
        }
    }

    private void SendMessage() {
        updateUserStatus("online");
        String messageText = userMessageInput.getText().toString();

        if (TextUtils.isEmpty(messageText)){
            Toast.makeText(this,"Please type a message first",Toast.LENGTH_SHORT).show();
        }
        else{
                String message_sender_ref = "Messages/" + messageSenderID + "/" +gcKey;

                DatabaseReference user_message_key = RootRef.child("Messages").child(messageSenderID).child(gcKey).push();
                String message_push_id = user_message_key.getKey();

                Calendar calForDate = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("MM-dd-yyyy");
                saveCurrentDate = currentDate.format(calForDate.getTime());
                SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
                saveCurrentTime = currentTime.format(calForDate.getTime());

                Map messageTextBody = new HashMap();
                Map receiverTextBody = new HashMap();
                Map senderTextBody = new HashMap();

                messageTextBody.put("message",messageText);
                messageTextBody.put("time",saveCurrentTime);
                messageTextBody.put("date",saveCurrentDate);
                messageTextBody.put("type","text");
                messageTextBody.put("from",messageSenderID);

                senderTextBody.putAll(messageTextBody);
                senderTextBody.put("isSeen", true);

                receiverTextBody.putAll(messageTextBody);
                receiverTextBody.put("isSeen", false);

                Map messageBodyDetails = new HashMap();
                messageBodyDetails.put(message_sender_ref+"/"+message_push_id,senderTextBody);

                for(int x=0; x<messageReceivers.size(); x++){
                    String message_receiver_ref = "Messages/" + messageReceivers.get(x).split(" / ")[0] + "/" +  gcKey;
                    messageBodyDetails.put(message_receiver_ref+"/"+message_push_id,receiverTextBody);
                }

                RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){
                            Toast.makeText(ChatGroupActivity.this,"Message Sent Successfully",Toast.LENGTH_SHORT).show();
                            userMessageInput.setText("");
                        }
                        else{
                            Toast.makeText(ChatGroupActivity.this,"Error: "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            userMessageInput.setText("");
                        }

                    }
                });
        }
    }

    private void updateUserStatus(String state){
        String saveCurrentDate, saveCurrentTime;
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        Map currentStateMap = new HashMap<>();
        currentStateMap.put("time",saveCurrentTime);
        currentStateMap.put("date",saveCurrentDate);
        currentStateMap.put("type",state);

        UsersRef.child(messageSenderID).child("userState")
                .updateChildren(currentStateMap);
    }

    private void DisplayReceiverInfo() {
        GroupChatsRef.child(gcKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    GroupChats gcdata = dataSnapshot.getValue(GroupChats.class);

                    gc_name_hldr = gcdata.getGc_name();
                    receiverName.setText(gc_name_hldr);
                    userLastSeen.setText("");
                    if (dataSnapshot.hasChild("gc_picture")){
                        gc_picture_hldr = gcdata.getGc_picture();
                        final String profileImage = gc_picture_hldr;
                        Picasso.with(ChatGroupActivity.this).load(profileImage).placeholder(R.drawable.profile).into(receiverProfileImage);
                    }
                    else{
                        gc_picture_hldr = "";
                        Picasso.with(ChatGroupActivity.this).load(R.drawable.profile).into(receiverProfileImage);
                    }
                    if(dataSnapshot.hasChild("gc_participants")){
                        if(!gcdata.getGc_participants().contains(messageSenderID)){
                            SendMessageButton.setVisibility(View.GONE);
                            SendImagefileButton.setVisibility(View.GONE);
                            userMessageInput.setVisibility(View.GONE);
                            ChatNotice.setVisibility(View.VISIBLE);
                            receiverProfileImage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            });

                        }else{
                            SendMessageButton.setVisibility(View.VISIBLE);
                            SendImagefileButton.setVisibility(View.VISIBLE);
                            userMessageInput.setVisibility(View.VISIBLE);
                            ChatNotice.setVisibility(View.GONE);

                            receiverProfileImage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ViewChatDialog alert = new ViewChatDialog();
                                    alert.showDialog(ChatGroupActivity.this);
                                }
                            });

                            for(int x=0; x<gcdata.getGc_participants().size();x++){
                                String uidhldr = gcdata.getGc_participants().get(x);
                                UsersRef.child(uidhldr).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            if(uidhldr.equals(messageSenderID)){
                                                curr_name_hldr = dataSnapshot.child("fullname").getValue().toString();
                                            }
                                            messageReceivers.add(uidhldr+" / "+dataSnapshot.child("fullname").getValue().toString());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                if(x<gcdata.getGc_participants().size()){
                                    RetrieveUsers();
                                }
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void InitializeFields() {
        ChattoolBar = findViewById(R.id.chat_bar_layout);
        setSupportActionBar(ChattoolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);

        ChatNotice = findViewById(R.id.chat_notice);
        SendMessageButton = findViewById(R.id.send_message_button);
        SendImagefileButton = findViewById(R.id.send_image_file_button);
        userMessageInput = findViewById(R.id.input_message);

        receiverName = findViewById(R.id.custom_profile_name);
        userLastSeen = findViewById(R.id.custom_user_last_seen);
        receiverProfileImage = findViewById(R.id.custom_profile_image);

        messageAdapter = new MessagesAdapter(messagesList, this);
        userMessagesList = findViewById(R.id.messages_list_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setHasFixedSize(true);
        userMessagesList.setNestedScrollingEnabled(false);
        userMessagesList.setItemViewCacheSize(20);
        userMessagesList.setDrawingCacheEnabled(true);
        userMessagesList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

        profileList = new ArrayList<>();
        profileSelectedList = new ArrayList<>();
        profileadapter = new ChatSearchAdapter(profileSelectedList, ChatGroupActivity.this);
        profileadapter.notifyDataSetChanged();
    }

    public class ViewChatDialog {
        public void showDialog(Context context){
            final Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog_gc_details);

            Toolbar dialogtoolbar = (Toolbar) dialog.findViewById(R.id.toolbar);
            setSupportActionBar(dialogtoolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("CHAT DETAILS");
            dialogtoolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            CircleImageView gc_pic = (CircleImageView) dialog.findViewById(R.id.gc_pic);
            TextView gc_name = (TextView) dialog.findViewById(R.id.gc_name);
            Button edit_gc_members = (Button) dialog.findViewById(R.id.edit_gc_members);
            Button edit_gc_details = (Button) dialog.findViewById(R.id.edit_gc_details);

            gc_name.setText(gc_name_hldr);
            if (!gc_picture_hldr.equals("")){
                Picasso.with(ChatGroupActivity.this).load(gc_picture_hldr).placeholder(R.drawable.profile).into(gc_pic);
            }
            else{
                Picasso.with(ChatGroupActivity.this).load(R.drawable.profile).into(gc_pic);
            }

            edit_gc_members.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditGcMembers alert = new EditGcMembers();
                    alert.showDialog(ChatGroupActivity.this, dialog);
                }
            });

            edit_gc_details.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditGcDetails alert = new EditGcDetails();
                    alert.showDialog(ChatGroupActivity.this, dialog);
                }
            });

            dialog.show();
        }
    }

    public class EditGcMembers {
        public void showDialog(Context context, Dialog maindialog){
            final Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog_new_chat);

            Toolbar dialogtoolbar = (Toolbar) dialog.findViewById(R.id.toolbar);
            setSupportActionBar(dialogtoolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            dialogtoolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            Button close_button = (Button) dialog.findViewById(R.id.new_convo_close);
            Button next_button = (Button) dialog.findViewById(R.id.new_convo_proceed);

            profileSelectedView = (RecyclerView) dialog.findViewById(R.id.all_users_post_list);
            profileSelectedView.setHasFixedSize(true);
            profileSelectedView.setLayoutManager(new LinearLayoutManager(ChatGroupActivity.this));

            profileadapter = new ChatSearchAdapter(profileSelectedList, ChatGroupActivity.this);
            profileSelectedView.setAdapter(profileadapter);
            profileadapter.notifyDataSetChanged();

            SearchAutoCompleteAdapter searchadapter = new SearchAutoCompleteAdapter(ChatGroupActivity.this, profileList);

            AutoCompleteTextView searchEditText = dialog.findViewById(R.id.search_box_input_fragment);
            searchadapter = new SearchAutoCompleteAdapter(ChatGroupActivity.this, profileList);
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
                        Toast.makeText(ChatGroupActivity.this, "This user is already in your list.", Toast.LENGTH_SHORT).show();
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
                    ProgressDialog loadingBar = new ProgressDialog(ChatGroupActivity.this);
                    loadingBar.setTitle("Group Chat Members");
                    loadingBar.setMessage("Please wait, while we are updating your Group Chat Members...");
                    loadingBar.setCanceledOnTouchOutside(true);
                    loadingBar.show();

                    Calendar calForDate = Calendar.getInstance();
                    SimpleDateFormat currentDate = new SimpleDateFormat("MM-dd-yyyy");
                    saveCurrentDate = currentDate.format(calForDate.getTime());
                    SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
                    saveCurrentTime = currentTime.format(calForDate.getTime());

                    List<String> selectedListHldr = new ArrayList<>();
                    List<String> idHldr = new ArrayList<>();
                    ArrayList<Map> messageArr = new ArrayList<>();
                    ArrayList<Map> messageSenderArr = new ArrayList<>();

                    idHldr.add(messageSenderID);
                    for(int x=0; x<profileSelectedList.size(); x++){
                        if(!messageReceivers.contains(profileSelectedList.get(x).getUid()+" / "+profileSelectedList.get(x).getDescription())){
                            Toast.makeText(context, curr_name_hldr+" has added "+profileSelectedList.get(x).getDescription()+" to the group chat.", Toast.LENGTH_SHORT).show();

                            Map messageTextBody = new HashMap();
                            Map receiverTextBody = new HashMap();
                            Map senderTextBody = new HashMap();

                            messageTextBody.put("message", curr_name_hldr+" has added "+profileSelectedList.get(x).getDescription()+" to the group chat.");
                            messageTextBody.put("time",saveCurrentTime);
                            messageTextBody.put("date",saveCurrentDate);
                            messageTextBody.put("type","text");
                            messageTextBody.put("from",messageSenderID);

                            senderTextBody.putAll(messageTextBody);
                            senderTextBody.put("isSeen", true);

                            receiverTextBody.putAll(messageTextBody);
                            receiverTextBody.put("isSeen", false);

                            if(!messageArr.contains(receiverTextBody)){
                                messageArr.add(receiverTextBody);
                            }

                            if(!messageSenderArr.contains(senderTextBody)){
                                messageSenderArr.add(senderTextBody);
                            }

                        }
                        selectedListHldr.add(profileSelectedList.get(x).getUid()+" / "+profileSelectedList.get(x).getDescription());
                        idHldr.add(profileSelectedList.get(x).getUid());
                    }

                    for(int y=0; y<messageReceivers.size(); y++){
                        if(!messageReceivers.get(y).equals(messageSenderID+" / "+curr_name_hldr)){
                            if(!selectedListHldr.contains(messageReceivers.get(y))){
                                Toast.makeText(context, curr_name_hldr+" has removed "+messageReceivers.get(y).split(" / ")[1]+" from the group chat.", Toast.LENGTH_SHORT).show();
                                Map messageTextBody = new HashMap();
                                Map receiverTextBody = new HashMap();
                                Map senderTextBody = new HashMap();

                                messageTextBody.put("message", curr_name_hldr+" has removed "+messageReceivers.get(y).split(" / ")[1]+" from the group chat.");
                                messageTextBody.put("time",saveCurrentTime);
                                messageTextBody.put("date",saveCurrentDate);
                                messageTextBody.put("type","text");
                                messageTextBody.put("from",messageSenderID);

                                senderTextBody.putAll(messageTextBody);
                                senderTextBody.put("isSeen", true);

                                receiverTextBody.putAll(messageTextBody);
                                receiverTextBody.put("isSeen", false);

                                if(!messageArr.contains(receiverTextBody)){
                                    messageArr.add(receiverTextBody);
                                }

                                if(!messageSenderArr.contains(senderTextBody)){
                                    messageSenderArr.add(senderTextBody);
                                }
                            }
                        }
                    }

                    for(int z=0; z<messageArr.size(); z++){
                        String message_sender_ref = "Messages/" + messageSenderID + "/" +  gcKey;
                        DatabaseReference user_message_key = RootRef.child("Messages").child(messageSenderID)
                                .child(gcKey).push();
                        String message_push_id = user_message_key.getKey();

                        Map messageBodyDetails = new HashMap();
                        messageBodyDetails.put(message_sender_ref+"/"+message_push_id,messageSenderArr.get(z));
                        for(int a=0; a<profileSelectedList.size(); a++){
                            if(!profileSelectedList.get(a).getUid().equals(messageSenderID)){
                                String message_receiver_ref = "Messages/" + profileSelectedList.get(a).getUid() + "/" +  gcKey;
                                messageBodyDetails.put(message_receiver_ref+"/"+message_push_id,messageArr.get(z));
                            }
                        }
                        int finalZ = z;
                        RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()){
                                    if(finalZ < messageArr.size()){
                                        profileList.clear();
                                        RootRef.child("GroupChats").child(gcKey).child("gc_participants").removeValue();
                                        RootRef.child("GroupChats").child(gcKey).child("gc_participants").setValue(idHldr);
                                        messageReceivers = selectedListHldr;
                                        profileSelectedList.clear();
                                        profileadapter.notifyDataSetChanged();
                                        loadingBar.dismiss();
                                        maindialog.dismiss();
                                        dialog.dismiss();
                                        Toast.makeText(ChatGroupActivity.this, "Group Chat members successfully updated.", Toast.LENGTH_SHORT).show();
                                    }

                                }
                                else{
                                    loadingBar.dismiss();
                                    Toast.makeText(ChatGroupActivity.this,"Error: "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                }

                            }
                        });

                    }



                }
            });

            dialog.show();
        }
    }

    public class EditGcDetails {
        public void showDialog(Activity activity, Dialog chatdialog){
            final Dialog gcdialog = new Dialog(activity);
            gcdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            gcdialog.setCancelable(false);
            gcdialog.setContentView(R.layout.dialog_new_gc);

            edit_gc_name = (EditText) gcdialog.findViewById(R.id.setup_full_name);
            edit_gc_picture = (CircleImageView) gcdialog.findViewById(R.id.setup_gc_profile_image);

            Button close_button = (Button) gcdialog.findViewById(R.id.new_gc_close);
            Button next_button = (Button) gcdialog.findViewById(R.id.new_gc_proceed);

            edit_gc_name.setText(gc_name_hldr);
            next_button.setText("Update");
            if (!gc_picture_hldr.equals("")){
                Picasso.with(ChatGroupActivity.this).load(gc_picture_hldr).placeholder(R.drawable.profile).into(edit_gc_picture);
            }
            else{
                Picasso.with(ChatGroupActivity.this).load(R.drawable.profile).into(edit_gc_picture);
            }

            edit_gc_picture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    gallery_type = "gc_picture_change";
                    Intent galleryIntent = new Intent();
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setType("image/*");
                    startActivityForResult(galleryIntent,GALLERY_PICK);
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

                    gcImageRef = FirebaseStorage.getInstance().getReference().child("gc_picture");

                    ProgressDialog loadingBar = new ProgressDialog(ChatGroupActivity.this);
                    loadingBar.setTitle("Group Chat");
                    loadingBar.setMessage("Please wait, while we are updating your Group Chat Details...");
                    loadingBar.setCanceledOnTouchOutside(true);
                    loadingBar.show();

                    Calendar calForDate = Calendar.getInstance();
                    SimpleDateFormat currentDate = new SimpleDateFormat("MM-dd-yyyy");
                    saveCurrentDate = currentDate.format(calForDate.getTime());
                    SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
                    saveCurrentTime = currentTime.format(calForDate.getTime());

                    Map messageTextBody = new HashMap();
                    Map receiverTextBody = new HashMap();
                    Map senderTextBody = new HashMap();

                    ArrayList<Map> messageArr = new ArrayList<>();
                    ArrayList<Map> messageSenderArr = new ArrayList<>();

                    if(!edit_gc_name.getText().toString().equals(gc_name_hldr)){
                        messageTextBody.put("message", curr_name_hldr+" has changed group chat name to "+edit_gc_name.getText().toString());
                        messageTextBody.put("time",saveCurrentTime);
                        messageTextBody.put("date",saveCurrentDate);
                        messageTextBody.put("type","text");
                        messageTextBody.put("from",messageSenderID);

                        senderTextBody.putAll(messageTextBody);
                        senderTextBody.put("isSeen", true);

                        receiverTextBody.putAll(messageTextBody);
                        receiverTextBody.put("isSeen", false);

                        if(!messageArr.contains(receiverTextBody)){
                            messageArr.add(receiverTextBody);
                        }

                        if(!messageSenderArr.contains(senderTextBody)){
                            messageSenderArr.add(senderTextBody);
                        }

                        gcRef.child(gcKey).child("gc_name").setValue(edit_gc_name.getText().toString().trim());
                    }

                    if(ImageUri != null){
                        final StorageReference filePath = gcImageRef.child(gcKey+".jpg");
                        filePath.putFile(ImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String downloadUrl = uri.toString();
                                        Map messageTextBody = new HashMap();
                                        Map receiverTextBody = new HashMap();
                                        Map senderTextBody = new HashMap();
                                        messageTextBody.put("message", curr_name_hldr+" has changed group chat picture");
                                        messageTextBody.put("time",saveCurrentTime);
                                        messageTextBody.put("date",saveCurrentDate);
                                        messageTextBody.put("type","text");
                                        messageTextBody.put("from",messageSenderID);

                                        senderTextBody.putAll(messageTextBody);
                                        senderTextBody.put("isSeen", true);

                                        receiverTextBody.putAll(messageTextBody);
                                        receiverTextBody.put("isSeen", false);

                                        if(!messageArr.contains(receiverTextBody)){
                                            messageArr.add(receiverTextBody);
                                        }

                                        if(!messageSenderArr.contains(senderTextBody)){
                                            messageSenderArr.add(senderTextBody);
                                        }

                                        gcRef.child(gcKey).child("gc_picture").setValue(downloadUrl);
                                        gc_name_hldr = edit_gc_name.getText().toString();

                                        writeDataToDatabase(
                                                messageArr,
                                                messageSenderArr,
                                                chatdialog,
                                                gcdialog,
                                                activity,
                                                loadingBar
                                        );

                                    }
                                });
                            }
                        });
                    }else{
                        gc_name_hldr = edit_gc_name.getText().toString();
                        writeDataToDatabase(
                                messageArr,
                                messageSenderArr,
                                chatdialog,
                                gcdialog,
                                activity,
                                loadingBar
                        );
                    }

                }
            });
            gcdialog.show();
        }

        private void writeDataToDatabase(ArrayList<Map> messageTextBody,
                                         ArrayList<Map> messageSenderTextBody,
                                         Dialog chatdialog,
                                         Dialog gcdialog,
                                         Activity activity,
                                         ProgressDialog loadingBar){


            for(int y=0; y<messageTextBody.size(); y++){
                String message_sender_ref = "Messages/" + messageSenderID + "/" +  gcKey;
                DatabaseReference user_message_key = RootRef.child("Messages").child(messageSenderID)
                        .child(gcKey).push();
                String message_push_id = user_message_key.getKey();

                Map messageBodyDetails = new HashMap();
                messageBodyDetails.put(message_sender_ref+"/"+message_push_id,messageSenderTextBody.get(y));
                for(int x=0; x<messageReceivers.size(); x++){
                    if(!messageReceivers.get(x).equals(messageSenderID+" / "+curr_name_hldr)){
                        String message_receiver_ref = "Messages/" + messageReceivers.get(x).split(" / ")[0] + "/" +  gcKey;
                        messageBodyDetails.put(message_receiver_ref+"/"+message_push_id,messageTextBody.get(y));
                    }
                }
                int finalY = y;
                RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){
                            if(finalY < messageTextBody.size()){
                                loadingBar.dismiss();
                                chatdialog.dismiss();;
                                gcdialog.dismiss();
                                Toast.makeText(activity, "Group Chat details successfully updated.", Toast.LENGTH_SHORT).show();
                            }

                        }
                        else{
                            loadingBar.dismiss();
                            Toast.makeText(activity,"Error: "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }

        }
    }

    private void RetrieveUsers(){
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                profileList.clear();
                profileSelectedList.clear();
                for (DataSnapshot suggestionSnapshot : dataSnapshot.getChildren()){
                    if(!suggestionSnapshot.getKey().equals(messageSenderID)){
                        String suggestion = suggestionSnapshot.child("fullname").getValue(String.class);
                        String profilepicstring = "";
                        if (suggestionSnapshot.hasChild("profileimage"))
                            profilepicstring = suggestionSnapshot.child("profileimage").getValue().toString();
                        else
                            profilepicstring = "";

                        if(suggestion != null){
                            if(!profileList.contains(new SearchAutoComplete(suggestion, profilepicstring, suggestionSnapshot.getKey()))){
                                profileList.add(new SearchAutoComplete(suggestion, profilepicstring, suggestionSnapshot.getKey()));
                            }
                            for(int x=0; x<messageReceivers.size(); x++){
                                if(!messageSenderID.equals(messageReceivers.get(x).split(" / ")[0])){
                                    if(suggestionSnapshot.getKey().equals(messageReceivers.get(x).split(" / ")[0])){
                                        if(!profileSelectedList.contains(new ChatProfile(suggestionSnapshot.getKey(), suggestion, profilepicstring))){
                                            profileSelectedList.add(new ChatProfile(suggestionSnapshot.getKey(), suggestion, profilepicstring));
                                            profileadapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            }
                        }
                    }else if(suggestionSnapshot.getKey().equals(messageSenderID)){
                        messageSenderName = suggestionSnapshot.child("fullname").getValue(String.class);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case PICTURE_RESULT:
                    if (requestCode == PICTURE_RESULT)
                        if (resultCode == RESULT_OK) {
                            try {
                                setFileInChat(cameraUri, "image", ".jpg");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                case GALLERY_PICK:
                    if(requestCode == GALLERY_PICK){
                        if (resultCode == RESULT_OK && data !=null ) {
                            if(gallery_type.equals("gc_picture_change")){
                                ImageUri = data.getData();
                                edit_gc_picture.setImageURI(ImageUri);

                            }else if(gallery_type.equals("gc_picture_attachment")){
                                setFileInChat(data.getData(), "image",  getfileExtension(data.getData()));
                            }
                        }
                    }
                    break;
                case FILE_RESULT:
                    if(requestCode == FILE_RESULT) {
                        if ((data != null) && (data.getData() != null)) {
                            setFileInChat(data.getData(), "file", getfileExtension(data.getData()));
                        }
                    }
            }
        }
    }

    private void setFileInChat(Uri data, String filetype, String extension){
        ProgressDialog loadingBar = new ProgressDialog(ChatGroupActivity.this);
        ImageUri = data;
        loadingBar.setTitle("Loading");
        loadingBar.setMessage("Please wait, while we are sending your Photo...");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();

        String message_sender_ref = "Messages/" + messageSenderID + "/" +gcKey;

        DatabaseReference user_message_key = RootRef.child("Messages").child(messageSenderID).child(gcKey).push();
        String message_push_id = user_message_key.getKey();

        StorageReference msgImageRef = null;

        if(filetype.equals("image")){
            msgImageRef = FirebaseStorage.getInstance().getReference().child("message_image");
        }else if(filetype.equals("file")){
            msgImageRef = FirebaseStorage.getInstance().getReference().child("message_file");
        }

        if (ImageUri != null) {
            final StorageReference filePath = msgImageRef.child(message_push_id +"."+extension);
            filePath.putFile(ImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Calendar calForDate = Calendar.getInstance();
                            SimpleDateFormat currentDate = new SimpleDateFormat("MM-dd-yyyy");
                            saveCurrentDate = currentDate.format(calForDate.getTime());
                            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
                            saveCurrentTime = currentTime.format(calForDate.getTime());
                            Map messageTextBody = new HashMap();
                            Map receiverTextBody = new HashMap();
                            Map senderTextBody = new HashMap();

                            if(filetype.equals("image")){
                                messageTextBody.put("message", messageSenderName + " sent an image");
                            }else if(filetype.equals("file")){
                                messageTextBody.put("message", messageSenderName + " sent a file. You can click here to download the file.");
                            }

                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);
                            messageTextBody.put("type", filetype);
                            messageTextBody.put("from", messageSenderID);
                            messageTextBody.put("fileString", uri.toString());

                            senderTextBody.putAll(messageTextBody);
                            senderTextBody.put("isSeen", true);

                            receiverTextBody.putAll(messageTextBody);
                            receiverTextBody.put("isSeen", false);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(message_sender_ref + "/" + message_push_id, senderTextBody);

                            for(int x=0; x<messageReceivers.size(); x++){
                                String message_receiver_ref = "Messages/" + messageReceivers.get(x).split(" / ")[0] + "/" +  gcKey;
                                messageBodyDetails.put(message_receiver_ref + "/" + message_push_id, receiverTextBody);
                            }

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ChatGroupActivity.this, "Message Sent Successfully", Toast.LENGTH_SHORT).show();
                                        userMessageInput.setText("");
                                    } else {
                                        Toast.makeText(ChatGroupActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        userMessageInput.setText("");
                                    }

                                }
                            });
                            loadingBar.dismiss();
                        }
                    });
                }
            });
        }

        loadingBar.dismiss();
    }

    private String getfileExtension(Uri uri) {
        String extension;
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        extension= mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
        return extension;
    }

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context,
                            Manifest.permission.READ_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    public void showDialog(final String msg, final Context context,
                           final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                    Toast.makeText(ChatGroupActivity.this, "GET_ACCOUNTS Denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }
}
