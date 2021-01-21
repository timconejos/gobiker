package ph.com.team.gobiker.ui.search;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.material.tabs.TabLayout;
import ph.com.team.gobiker.R;
import ph.com.team.gobiker.ui.search.SearchViewPagerAdapter;

public class FindFriendsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private Button searchButton;
    private EditText SearchInputText;


    private TabLayout tabLayout;
    private ViewPager viewPager;
    private String searchTag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        searchButton = findViewById(R.id.search_people_friends_button);
        SearchInputText = findViewById(R.id.search_box_input);

        tabLayout = findViewById(R.id.searchnavbar);
        viewPager = findViewById(R.id.searchcontainer);

        if (!getIntent().getExtras().get("searchKey").toString().equals("")) {
            searchTag = getIntent().getExtras().get("searchKey").toString();
            SearchInputText.setText(searchTag);
        }

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
