package ph.com.team.gobiker.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import ph.com.team.gobiker.ClickPostActivity;
import ph.com.team.gobiker.CommentsActivity;
import ph.com.team.gobiker.CreateAccount;
import ph.com.team.gobiker.FindFriendsActivity;
import ph.com.team.gobiker.LikesActivity;
import ph.com.team.gobiker.PostActivity;
import ph.com.team.gobiker.R;
import ph.com.team.gobiker.ui.dashboard.FeedFragment;
import ph.com.team.gobiker.ui.dashboard.ProfileFragment;
import ph.com.team.gobiker.ui.dashboard.ViewPagerAdapter;
import ph.com.team.gobiker.ui.login.MainLoginActivity;
import uk.co.senab.photoview.PhotoViewAttacher;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, GroupsRef;
    private Button searchButton;
    private AutoCompleteTextView SearchInputText;
    private String currentUserID;
    private View root;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private ArrayList<String> suggestionsTemp = new ArrayList<String>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        mAuth = FirebaseAuth.getInstance();

        viewPager = root.findViewById(R.id.profilecontainer);
        tabLayout = root.findViewById(R.id.profilenavbar);

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        currentUserID = mAuth.getCurrentUser().getUid();
        searchButton = root.findViewById(R.id.search_people_friends_button_fragment);

        final ArrayAdapter<String> autoComplete = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
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

//        GroupsRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot suggestionSnapshot : dataSnapshot.getChildren()){
//                    String suggestion = suggestionSnapshot.child("group_name").getValue(String.class);
//                    if(suggestion != null){
//                        autoComplete.add(suggestion+" (Group)");
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

        SearchInputText = root.findViewById(R.id.search_box_input_fragment);
        SearchInputText.setAdapter(autoComplete);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!SearchInputText.getText().toString().equals("")) {
                    String searchBoxInput = SearchInputText.getText().toString();
                    Intent intent = new Intent(getActivity(), FindFriendsActivity.class);
                    intent.putExtra("searchKey", searchBoxInput);
                    startActivity(intent);
                }
            }
        });

        return root;
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new HomeFeedFragment(), "Feed", currentUserID);
        adapter.addFragment(new GroupFragment(), "Groups", currentUserID);
        viewPager.setAdapter(adapter);
    }
}
