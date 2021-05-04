package ph.com.team.gobiker.ui.posts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import ph.com.team.gobiker.NavActivity;
import ph.com.team.gobiker.R;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ClickPostActivity extends AppCompatActivity {

    private ImageView PostImage;
    private TextView user, datetime_post;
    private EditText PostDescription;
    private Button EditPostButton;
    private StorageReference PostsImageReference;
    private DatabaseReference ClickPostRef, UsersRef;
    private String PostKey, currentUserID, databaseUserID, description, image, from_feed, downloadUrl="";
    private CircleImageView click_post_profile_image;
    private FirebaseAuth mAuth;
    private PhotoViewAttacher pAttacher;
    private static final int Gallery_Pick = 1;
    private Uri ImageUri;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        loadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        PostKey = getIntent().getExtras().get("PostKey").toString();
        from_feed = getIntent().getExtras().get("from_feed").toString();

        if(from_feed.equals("HomeFeed")){
            ClickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);
        }else if(from_feed.equals("GroupFeed")){
            ClickPostRef = FirebaseDatabase.getInstance().getReference().child("GroupPosts").child(PostKey);
        }

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsImageReference = FirebaseStorage.getInstance().getReference();

        PostImage = findViewById(R.id.click_post_image);
        PostDescription = findViewById(R.id.click_post_description);
//        DeletePostButton = findViewById(R.id.delete_post_button);
        EditPostButton = findViewById(R.id.edit_post_button);
        click_post_profile_image = findViewById(R.id.click_post_profile_image);
        user = findViewById(R.id.click_post_user_name);
        datetime_post = findViewById(R.id.click_post_time);

//        DeletePostButton.setVisibility(View.INVISIBLE);
        EditPostButton.setVisibility(View.INVISIBLE);

        PostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pAttacher = new PhotoViewAttacher(PostImage);
                pAttacher.setZoomable(true);
                pAttacher.update();
            }
        });


        ClickPostRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    description = dataSnapshot.child("description").getValue().toString();
                    if (dataSnapshot.hasChild("postimage")) {
                        image = dataSnapshot.child("postimage").getValue().toString();
                        Picasso.with(ClickPostActivity.this).load(image).into(PostImage);
                        PostImage.setVisibility(View.VISIBLE);
                    }
                    else{
                        PostImage.setVisibility(View.GONE);
                    }
                    databaseUserID = dataSnapshot.child("uid").getValue().toString();

                    datetime_post.setText(dataSnapshot.child("date").getValue().toString()+" "+dataSnapshot.child("time").getValue().toString());

                    PostDescription.setText(description);

                    if (currentUserID.equals(databaseUserID)) {
//                        DeletePostButton.setVisibility(View.VISIBLE);
                        EditPostButton.setVisibility(View.VISIBLE);
                    }

                    UsersRef.child(databaseUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                user.setText(snapshot.child("fullname").getValue().toString());
                                if (snapshot.hasChild("profileimage")) {
                                    image = snapshot.child("profileimage").getValue().toString();
                                    Picasso.with(ClickPostActivity.this).load(image).into(click_post_profile_image);
                                    click_post_profile_image.setVisibility(View.VISIBLE);
                                }
                                else{
                                    click_post_profile_image.setVisibility(View.GONE);
                                }
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

//        DeletePostButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                DeleteCurrentPost();
//            }
//        });

        EditPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingBar.setTitle("Loading");
                loadingBar.setMessage("Please wait, while we are updating your post...");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                if(ImageUri==null){
                    EditCurrentPost();
                }
                else {
                    StoringImageToFirebaseStorage();
                }
            }
        });

        PostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenGallery();
            }
        });
    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null){
            ImageUri = data.getData();
            PostImage.setImageURI(ImageUri);
        }
    }

    private void StoringImageToFirebaseStorage() {
        final StorageReference filePath = PostsImageReference.child("post_images").child(ImageUri.getLastPathSegment() + PostKey + ".jpg");

        filePath.putFile(ImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        downloadUrl = uri.toString();
                        //Toast.makeText(PostActivity.this, "Image uploaded successfully to storage", Toast.LENGTH_SHORT).show();
                        EditCurrentPost();
                    }
                });
            }
        });
    }

    private void EditCurrentPost() {
        ClickPostRef.child("description").setValue(PostDescription.getText().toString());
        if (!downloadUrl.equals(""))
            ClickPostRef.child("postimage").setValue(downloadUrl);
        loadingBar.dismiss();
        SendUserToMainActivity();
        Toast.makeText(ClickPostActivity.this,"Post updated successfully",Toast.LENGTH_SHORT).show();
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(ClickPostActivity.this, NavActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
