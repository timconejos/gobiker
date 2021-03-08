package ph.com.team.gobiker.ui.chat;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import ph.com.team.gobiker.ui.posts.PhotoFullScreenActivity;
import ph.com.team.gobiker.R;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>{
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersDatabaseRef;
    private Context context;
    private String FileNameHldr, FileNameExtension;
    private Uri urihldr;
    ProgressDialog pd;
    private long downloadID;


    public MessagesAdapter() {

    }

    public MessagesAdapter(List<Messages> userMessagesList, Context context){
        this.userMessagesList = userMessagesList;
        this.context = context;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView SenderMessageText, ReceiverMessageText;
        public ImageView SenderMessageFile, ReceiverMessageFile;
        public CircleImageView receiverProfileImage;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            SenderMessageText = itemView.findViewById(R.id.sender_message_text);
            ReceiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
            SenderMessageFile = itemView.findViewById(R.id.sender_message_file);
            ReceiverMessageFile = itemView.findViewById(R.id.receiver_message_file);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View V = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_layout_of_users,
                        parent, false);
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(V);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        if(!fromUserID.equals(messageSenderID)){
            usersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(messages.getFrom());
            usersDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() &&  dataSnapshot.child("profileimage").getValue() !=null){
                        String image = dataSnapshot.child("profileimage").getValue().toString();

                        Picasso.with(holder.receiverProfileImage.getContext()).load(image)
                                .placeholder(R.drawable.profile).into(holder.receiverProfileImage);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        if (fromMessageType.equals("text")){
            holder.ReceiverMessageFile.setVisibility(View.GONE);
            holder.SenderMessageFile.setVisibility(View.GONE);

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            try{
                Date date3 = sdf.parse(messages.getTime());
                SimpleDateFormat sdf2 = new SimpleDateFormat("hh:mm aa");
                GradientDrawable shape = new GradientDrawable();
                shape.setShape(GradientDrawable.RECTANGLE);
                shape.setCornerRadii(new float[] { 15, 15, 15, 15, 15, 15, 15, 15 });
                if (fromUserID.equals(messageSenderID)){
                    holder.receiverProfileImage.setVisibility(View.GONE);
                    holder.ReceiverMessageText.setVisibility(View.GONE);
                    holder.SenderMessageText.setVisibility(View.VISIBLE);

                    shape.setColor(Color.parseColor("#D6D6D6"));
                    holder.SenderMessageText.setBackground(shape);
                    holder.SenderMessageText.setTextColor(Color.rgb(51, 50, 48));
                    holder.SenderMessageText.setGravity(Gravity.LEFT);
                    holder.SenderMessageText.setText(messages.getMessage()+"\n"+messages.getDate()+" "+sdf2.format(date3));
                }
                else{
                    holder.receiverProfileImage.setVisibility(View.VISIBLE);
                    holder.ReceiverMessageText.setVisibility(View.VISIBLE);
                    holder.SenderMessageText.setVisibility(View.GONE);

                    shape.setColor(Color.parseColor("#3F6634"));
                    holder.ReceiverMessageText.setBackground(shape);
                    holder.ReceiverMessageText.setTextColor(Color.WHITE);
                    holder.ReceiverMessageText.setGravity(Gravity.LEFT);
                    holder.ReceiverMessageText.setText(messages.getFrom()+"\n"+messages.getMessage()+"\n"+messages.getDate()+" "+sdf2.format(date3));
                }
            }catch(ParseException e){
                e.printStackTrace();
            }
        }else if (fromMessageType.equals("image")){
            holder.ReceiverMessageFile.setVisibility(View.VISIBLE);
            holder.SenderMessageFile.setVisibility(View.VISIBLE);

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            try{
                Date date3 = sdf.parse(messages.getTime());
                SimpleDateFormat sdf2 = new SimpleDateFormat("hh:mm aa");
                GradientDrawable shape = new GradientDrawable();
                shape.setShape(GradientDrawable.RECTANGLE);
                shape.setCornerRadii(new float[] { 15, 15, 15, 15, 15, 15, 15, 15 });
                if (fromUserID.equals(messageSenderID)){
                    holder.receiverProfileImage.setVisibility(View.GONE);
                    holder.ReceiverMessageText.setVisibility(View.GONE);
                    holder.SenderMessageText.setVisibility(View.VISIBLE);

                    shape.setColor(Color.parseColor("#D6D6D6"));
                    holder.SenderMessageText.setBackground(shape);
                    holder.SenderMessageText.setTextColor(Color.rgb(74, 74, 74));
                    holder.SenderMessageText.setGravity(Gravity.LEFT);
                    holder.SenderMessageText.setText(messages.getMessage()+"\n"+messages.getDate()+" "+sdf2.format(date3));
                    Picasso.with(context)
                            .load(messages.getFileString())
                            .placeholder(R.drawable.image_placeholder)
                            .into(holder.SenderMessageFile);
                    holder.SenderMessageFile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, PhotoFullScreenActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("filestring", messages.getFileString());
                            context.startActivity(intent);
                        }
                    });
                }
                else{
                    holder.receiverProfileImage.setVisibility(View.VISIBLE);
                    holder.ReceiverMessageText.setVisibility(View.VISIBLE);
                    holder.SenderMessageText.setVisibility(View.GONE);

                    shape.setColor(Color.parseColor("#3F6634"));
                    holder.ReceiverMessageText.setBackground(shape);
                    holder.ReceiverMessageText.setTextColor(Color.WHITE);
                    holder.ReceiverMessageText.setGravity(Gravity.LEFT);
                    holder.ReceiverMessageText.setText(messages.getMessage()+"\n"+messages.getDate()+" "+sdf2.format(date3));
                    Picasso.with(context)
                            .load(messages.getFileString())
                            .placeholder(R.drawable.image_placeholder)
                            .into(holder.ReceiverMessageFile);
                    holder.ReceiverMessageFile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, PhotoFullScreenActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("filestring", messages.getFileString());
                            context.startActivity(intent);

                        }
                    });


                }
            }catch(ParseException e){
                e.printStackTrace();
            }
        }else if (fromMessageType.equals("file")){
            holder.ReceiverMessageFile.setVisibility(View.GONE);
            holder.SenderMessageFile.setVisibility(View.GONE);

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            try{
                Date date3 = sdf.parse(messages.getTime());
                SimpleDateFormat sdf2 = new SimpleDateFormat("hh:mm aa");
                GradientDrawable shape = new GradientDrawable();
                shape.setShape(GradientDrawable.RECTANGLE);
                shape.setCornerRadii(new float[] { 15, 15, 15, 15, 15, 15, 15, 15 });
                if (fromUserID.equals(messageSenderID)){
                    holder.receiverProfileImage.setVisibility(View.GONE);
                    holder.ReceiverMessageText.setVisibility(View.GONE);
                    holder.SenderMessageText.setVisibility(View.VISIBLE);

                    shape.setColor(Color.parseColor("#D6D6D6"));
                    holder.SenderMessageText.setBackground(shape);
                    holder.SenderMessageText.setTextColor(Color.rgb(74, 74, 74));
                    holder.SenderMessageText.setGravity(Gravity.LEFT);
                    holder.SenderMessageText.setText(messages.getMessage()+"\n"+messages.getDate()+" "+sdf2.format(date3));
                    holder.SenderMessageText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                downloadfile(messages.getFileString());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                else{
                    holder.receiverProfileImage.setVisibility(View.VISIBLE);
                    holder.ReceiverMessageText.setVisibility(View.VISIBLE);
                    holder.SenderMessageText.setVisibility(View.GONE);

                    shape.setColor(Color.parseColor("#3F6634"));
                    holder.ReceiverMessageText.setBackground(shape);
                    holder.ReceiverMessageText.setTextColor(Color.WHITE);
                    holder.ReceiverMessageText.setGravity(Gravity.LEFT);
                    holder.ReceiverMessageText.setText(messages.getMessage()+"\n"+messages.getDate()+" "+sdf2.format(date3));
                    holder.ReceiverMessageText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                downloadfile(messages.getFileString());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }catch(ParseException e){
                e.printStackTrace();
            }
        }
    }

    private void downloadfile(String storageurl) throws IOException {

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(storageurl);

        pd = new ProgressDialog(context);
        pd.setTitle("File Download");
        pd.setMessage("Please Wait..");
        pd.setIndeterminate(true);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.show();

        storageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                String filename = storageMetadata.getName();

                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String url = uri.toString();

                        downloadFile(context, filename,url);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void downloadFile(Context context, String fileName, String url) {

        DownloadManager downloadmanager = (DownloadManager) context.
                getSystemService(Context.DOWNLOAD_SERVICE);

        context.registerReceiver(onDownloadComplete,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        Uri uri = Uri.parse(url);
        FileNameHldr = fileName;

        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, "/gobiker_files", fileName);

        downloadID = downloadmanager.enqueue(request);
    }

    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Fetching the download id received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id) {
                Toast.makeText(context, "Download Completed", Toast.LENGTH_SHORT).show();
                pd.dismiss();

                String url = context.getExternalFilesDir("/gobiker_files/"+FileNameHldr).getAbsolutePath();
                Uri uri = Uri.fromFile(new File(url));

                Intent fileintent = new Intent(Intent.ACTION_VIEW);
                if (url.contains(".doc") || url.contains(".docx")) {
                    // Word document
                    fileintent.setDataAndType(uri, "application/msword");
                } else if (url.contains(".pdf")) {
                    // PDF file
                    fileintent.setDataAndType(uri, "application/pdf");
                } else if (url.contains(".ppt") || url.contains(".pptx")) {
                    // Powerpoint file
                    fileintent.setDataAndType(uri, "application/vnd.ms-powerpoint");
                } else if (url.contains(".xls") || url.contains(".xlsx")) {
                    // Excel file
                    fileintent.setDataAndType(uri, "application/vnd.ms-excel");
                } else if (url.contains(".zip") || url.contains(".rar")) {
                    // WAV audio file
                    fileintent.setDataAndType(uri, "application/x-wav");
                } else if (url.contains(".rtf")) {
                    // RTF file
                    fileintent.setDataAndType(uri, "application/rtf");
                } else if (url.contains(".wav") || url.contains(".mp3")) {
                    // WAV audio file
                    fileintent.setDataAndType(uri, "audio/x-wav");
                } else if (url.contains(".gif")) {
                    // GIF file
                    fileintent.setDataAndType(uri, "image/gif");
                } else if (url.contains(".jpg") || url.contains(".jpeg") || url.contains(".png")) {
                    // JPG file
                    fileintent.setDataAndType(uri, "image/jpeg");
                } else if (url.contains(".txt")) {
                    // Text file
                    fileintent.setDataAndType(uri, "text/plain");
                } else if (url.contains(".3gp") || url.contains(".mpg") || url.contains(".mpeg") || url.contains(".mpe") || url.contains(".mp4") || url.contains(".avi")) {
                    // Video files
                    fileintent.setDataAndType(uri, "video/*");
                }

                fileintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Intent chooserintent = Intent.createChooser(fileintent, "Open File");
                if (fileintent.resolveActivity(context.getPackageManager()) == null) {
                    // Show an error
                } else {
                    context.startActivity(chooserintent);
                }

            }
        }
    };


    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }
}
