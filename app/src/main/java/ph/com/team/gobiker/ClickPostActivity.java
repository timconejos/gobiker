package ph.com.team.gobiker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import ph.com.team.gobiker.user.User;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ClickPostActivity extends AppCompatActivity {

    private ImageView PostImage;
    private TextView user, datetime_post;
    private EditText PostDescription;
    private Button DeletePostButton, EditPostButton;
    private DatabaseReference ClickPostRef, UsersRef;
    private String PostKey, currentUserID, databaseUserID, description, image;
    private CircleImageView click_post_profile_image;
    private FirebaseAuth mAuth;
    private PhotoViewAttacher pAttacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        PostKey = getIntent().getExtras().get("PostKey").toString();
        ClickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

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


        ClickPostRef.addValueEventListener(new ValueEventListener() {
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

                    UsersRef.child(databaseUserID).addValueEventListener(new ValueEventListener() {
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
                EditCurrentPost(description);
            }
        });
    }

    private void EditCurrentPost(String description) {
        ClickPostRef.child("description").setValue(PostDescription.getText().toString());
        SendUserToMainActivity();
        Toast.makeText(ClickPostActivity.this,"Post updated successfully",Toast.LENGTH_SHORT).show();
//        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
//        builder.setTitle("Edit Post: ");
//        final EditText inputField = new EditText(ClickPostActivity.this);
//        inputField.setText(description);
//        builder.setView(inputField);
//
//        builder.setPositiveButton(Html.fromHtml("<font color='#3F6634'>Update</font>"), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                ClickPostRef.child("description").setValue(inputField.getText().toString());
//                Toast.makeText(ClickPostActivity.this,"Post updated successfully",Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        builder.setNegativeButton(Html.fromHtml("<font color='#757575'>Cancel</font>"), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                dialogInterface.cancel();
//            }
//        });
//
//        Dialog dialog = builder.create();
//        dialog.show();
//
//        dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
    }

//    private void DeleteCurrentPost() {
//        ClickPostRef.removeValue();
//        SendUserToMainActivity();
//        Toast.makeText(this,"Post has been deleted.",Toast.LENGTH_SHORT).show();
//    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(ClickPostActivity.this,NavActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
