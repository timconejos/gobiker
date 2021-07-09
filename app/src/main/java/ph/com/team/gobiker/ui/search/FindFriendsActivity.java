package ph.com.team.gobiker.ui.search;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import ph.com.team.gobiker.R;
import ph.com.team.gobiker.ui.search.SearchViewPagerAdapter;

public class FindFriendsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private Button searchButton;

    private AutoCompleteTextView SearchInputText;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private String searchTag;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, GroupsRef;


    private ArrayList<String> suggestionsTemp = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        searchButton = findViewById(R.id.search_people_friends_button);

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        mAuth = FirebaseAuth.getInstance();

        tabLayout = findViewById(R.id.searchnavbar);
        viewPager = findViewById(R.id.searchcontainer);

        SearchInputText = findViewById(R.id.search_box_input_fragment);

        if (!getIntent().getExtras().get("searchKey").toString().equals("")) {
            searchTag = getIntent().getExtras().get("searchKey").toString();
            SearchInputText.setText(searchTag);
        }

        final ArrayAdapter<String> autoComplete = new ArrayAdapter<String>(FindFriendsActivity.this, android.R.layout.simple_list_item_1);
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot suggestionSnapshot : dataSnapshot.getChildren()){
                    String suggestion = suggestionSnapshot.child("fullname").getValue(String.class);
                    if(suggestion != null){
                        if(!suggestionsTemp.contains(suggestion)){
                            autoComplete.add(suggestion);
                            suggestionsTemp.add(suggestion);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        GroupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot suggestionSnapshot : dataSnapshot.getChildren()){
                    String suggestion = suggestionSnapshot.child("group_name").getValue(String.class);
                    if(suggestion != null){
                        autoComplete.add(suggestion);
                        suggestionsTemp.add(suggestion);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        SearchInputText.setAdapter(autoComplete);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!SearchInputText.getText().toString().equals("")){
                    String searchBoxInput = SearchInputText.getText().toString();
                    searchTag = searchBoxInput;
                    setupViewPager(viewPager, searchTag);
                }
            }
        });


        setupViewPager(viewPager, searchTag);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager, String searchTag) {
        SearchViewPagerAdapter adapter = new SearchViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new PersonSearchFragment(), "People", searchTag);
        adapter.addFragment(new GroupSearchFragment(), "Groups", searchTag);
        viewPager.setAdapter(adapter);
    }
}
