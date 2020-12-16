package ph.com.team.gobiker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private Button searchButton;
    private EditText SearchInputText;

    private RecyclerView SearchResultList;

    private DatabaseReference allUsersDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        allUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

        SearchResultList = findViewById(R.id.search_result_list);
        SearchResultList.setHasFixedSize(true);
        SearchResultList.setLayoutManager(new LinearLayoutManager(this));

        searchButton = findViewById(R.id.search_people_friends_button);
        SearchInputText = findViewById(R.id.search_box_input);

        if (!getIntent().getExtras().get("searchKey").toString().equals("")) {
            SearchInputText.setText(getIntent().getExtras().get("searchKey").toString());
            SearchPeopleAndFriends(getIntent().getExtras().get("searchKey").toString());
        }

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!SearchInputText.getText().toString().equals("")){
                    String searchBoxInput = SearchInputText.getText().toString();
                    SearchPeopleAndFriends(searchBoxInput);
                }
            }
        });
    }

    private void SearchPeopleAndFriends(String searchBoxInput) {
        //Toast.makeText(this,"Searching...", Toast.LENGTH_LONG).show();

        Query searchPeopleandFriendsQuery = allUsersDatabaseRef.orderByChild("fullname")
                .startAt(searchBoxInput).endAt(searchBoxInput + "\uf8ff");

        FirebaseRecyclerAdapter<FindFriends,FindFriendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>(
                FindFriends.class,
                R.layout.all_users_display_layout,
                FindFriendsViewHolder.class,
                searchPeopleandFriendsQuery
        ) {
            @Override
            protected void populateViewHolder(FindFriendsViewHolder findFriendsViewHolder, FindFriends findFriends, int i) {
                findFriendsViewHolder.setFullname(findFriends.getFullname());

                findFriendsViewHolder.setProfileimage(getApplicationContext(), findFriends.getProfileimage());
                final String visit_user_id = getRef(i).getKey();

                findFriendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent profileIntent =  new Intent(FindFriendsActivity.this,PersonProfileActivity.class);
                        profileIntent.putExtra("visit_user_id",visit_user_id);
                        startActivity(profileIntent);
                    }
                });
            }
        };
        SearchResultList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public FindFriendsViewHolder(View itemView){
            super(itemView);
            mView = itemView;
        }

        public void setProfileimage(Context ctx, String profileimage){
            CircleImageView myImage = mView.findViewById(R.id.all_users_profile_image);
            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(myImage);
        }

        public void setFullname(String fullname) {
            TextView myName = mView.findViewById(R.id.all_users_profile_name);
            myName.setText(fullname);
        }
    }
}
