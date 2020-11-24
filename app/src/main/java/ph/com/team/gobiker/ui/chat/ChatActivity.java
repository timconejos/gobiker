package ph.com.team.gobiker.ui.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import ph.com.team.gobiker.Messages;
import ph.com.team.gobiker.MessagesAdapter;
import ph.com.team.gobiker.R;
import ph.com.team.gobiker.ui.home.GroupDetailsActivity;

public class ChatActivity extends AppCompatActivity {
    private Toolbar ChattoolBar;
    private ImageButton SendMessageButton, SendImagefileButton;
    private EditText userMessageInput;
    private RecyclerView userMessagesList;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messageAdapter;

    private String messageReceiverID, messageSenderName, messageSenderID, saveCurrentDate, saveCurrentTime;

    private TextView receiverName, userLastSeen;
    private CircleImageView receiverProfileImage;
    private DatabaseReference RootRef, UsersRef;
    private FirebaseAuth mAuth;

    //chat attachments variables
    final static int PICTURE_RESULT = 0;
    final static int GALLERY_PICK = 1;
    final static int FILE_RESULT = 123;
    private Uri ImageUri;
    private Uri cameraUri;
    ContentValues values;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        RootRef = FirebaseDatabase.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();

        //messageReceiverName = getIntent().getExtras().get("userName").toString();

        values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        cameraUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        InitializeFields();
        DisplayReceiverInfo();

        SendImagefileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Choose a File", "Cancel"};

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("File Upload");

                builder.setItems(options, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int item) {

                        if (options[item].equals("Take Photo")) {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
                            startActivityForResult(intent, PICTURE_RESULT);
                        } else if (options[item].equals("Choose from Gallery")) {
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
        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if (dataSnapshot.exists()) {
                            Messages messages = dataSnapshot.getValue(Messages.class);
                            messagesList.add(messages);
                            messageAdapter.notifyDataSetChanged();
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

    private void SendMessage() {
        updateUserStatus("online");
        String messageText = userMessageInput.getText().toString();

        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "Please type a message first", Toast.LENGTH_SHORT).show();
        } else {
            String message_sender_ref = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String message_receiver_ref = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference user_message_key = RootRef.child("Messages").child(messageSenderID)
                    .child(messageReceiverID).push();
            String message_push_id = user_message_key.getKey();

            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("MM-dd-yyyy");
            saveCurrentDate = currentDate.format(calForDate.getTime());
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
            saveCurrentTime = currentTime.format(calForDate.getTime());

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(message_sender_ref + "/" + message_push_id, messageTextBody);
            messageBodyDetails.put(message_receiver_ref + "/" + message_push_id, messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ChatActivity.this, "Message Sent Successfully", Toast.LENGTH_SHORT).show();
                        userMessageInput.setText("");
                    } else {
                        Toast.makeText(ChatActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        userMessageInput.setText("");
                    }

                }
            });
        }
    }

    private void updateUserStatus(String state) {
        String saveCurrentDate, saveCurrentTime;
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        Map currentStateMap = new HashMap<>();
        currentStateMap.put("time", saveCurrentTime);
        currentStateMap.put("date", saveCurrentDate);
        currentStateMap.put("type", state);

        UsersRef.child(messageSenderID).child("userState")
                .updateChildren(currentStateMap);
    }

    private void DisplayReceiverInfo() {

        RootRef.child("Users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    receiverName.setText(dataSnapshot.child("fullname").getValue().toString());

                    if (dataSnapshot.hasChild("userState")) {
                        final String type = dataSnapshot.child("userState").child("type").getValue().toString();
                        final String lastDate = dataSnapshot.child("userState").child("date").getValue().toString();
                        final String lastTime = dataSnapshot.child("userState").child("time").getValue().toString();

                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                        try {
                            Date date3 = sdf.parse(lastTime);
                            SimpleDateFormat sdf2 = new SimpleDateFormat("hh:mm aa");
                            if (type.equals("online")) {
                                userLastSeen.setText("online");
                            } else {
                                userLastSeen.setText("Last seen: " + lastDate + " " + sdf2.format(date3));
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }


                    } else {
                        userLastSeen.setText("");
                    }

                    if (dataSnapshot.hasChild("profileimage")) {
                        final String profileImage = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(ChatActivity.this).load(profileImage).placeholder(R.drawable.profile).into(receiverProfileImage);
                    } else {
                        Picasso.with(ChatActivity.this).load(R.drawable.profile).into(receiverProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        RootRef.child("Users").child(messageSenderID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    messageSenderName = dataSnapshot.child("fullname").getValue().toString();
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
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

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
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);
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
                            setFileInChat(data.getData(), "image",  getfileExtension(data.getData()));


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
        ProgressDialog loadingBar = new ProgressDialog(ChatActivity.this);
        ImageUri = data;
        loadingBar.setTitle("Loading");
        loadingBar.setMessage("Please wait, while we are sending your Photo...");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();

        String message_sender_ref = "Messages/" + messageSenderID + "/" + messageReceiverID;
        String message_receiver_ref = "Messages/" + messageReceiverID + "/" + messageSenderID;

        DatabaseReference user_message_key = RootRef.child("Messages").child(messageSenderID)
                .child(messageReceiverID).push();
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

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(message_sender_ref + "/" + message_push_id, messageTextBody);
                            messageBodyDetails.put(message_receiver_ref + "/" + message_push_id, messageTextBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ChatActivity.this, "Message Sent Successfully", Toast.LENGTH_SHORT).show();
                                        userMessageInput.setText("");
                                    } else {
                                        Toast.makeText(ChatActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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

    private String getfileExtension(Uri uri)
    {
        String extension;
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        extension= mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
        return extension;
    }

}

