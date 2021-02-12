package ph.com.team.gobiker.ui.posts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ph.com.team.gobiker.R;

public class ClickCommentActivity extends AppCompatActivity {
    private EditText PostDescription;
    private Button DeletePostButton, EditPostButton;
    private DatabaseReference ClickPostRef;
    private String PostKey, CommentsKey, currentUserID, description;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_comment);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        PostKey = getIntent().getExtras().get("PostKey").toString();
        CommentsKey = getIntent().getExtras().get("CommentsKey").toString();
        ClickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey).child("Comments").child(CommentsKey);
        PostDescription = findViewById(R.id.edit_comment_text);
        EditPostButton = findViewById(R.id.edit_comment_button);
        DeletePostButton = findViewById(R.id.delete_comment_button);

        ClickPostRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    description = dataSnapshot.child("comment").getValue().toString();

                    PostDescription.setText(description);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DeletePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeleteCurrentPost();
            }
        });

        EditPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClickPostRef.child("comment").setValue(PostDescription.getText().toString());
                Toast.makeText(ClickCommentActivity.this,"Comment updated successfully",Toast.LENGTH_SHORT).show();
                SendUsertoCommentsActivity();
            }
        });
    }

    private void DeleteCurrentPost() {
        ClickPostRef.removeValue();
        Toast.makeText(this,"Comment has been deleted.",Toast.LENGTH_SHORT).show();
        SendUsertoCommentsActivity();
    }

    private void SendUsertoCommentsActivity(){
        Intent commentsIntent = new Intent(ClickCommentActivity.this, CommentsActivity.class);
        commentsIntent.putExtra("PostKey", PostKey);
        startActivity(commentsIntent);
    }
}