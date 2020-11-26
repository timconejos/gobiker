package ph.com.team.gobiker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.GeoApiContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ph.com.team.gobiker.ui.login.MainLoginActivity;
import ph.com.team.gobiker.ui.notifications.NotificationSeenCheck;
import ph.com.team.gobiker.ui.notifications.Notifications;
import ph.com.team.gobiker.ui.notifications.NotificationsFragment;

public class NavActivity extends AppCompatActivity implements NotificationsFragment.Listener{

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    public static GeoApiContext context;
    protected PowerManager.WakeLock mWakeLock;
    public BottomNavigationView navView;
    private NotificationsFragment f;
    private boolean NotifFragmentStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);
        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        navView = findViewById(R.id.nav_view);

        // creating the Fragment
        f = new NotificationsFragment();
        // register activity as listener
        f.setListener(this);
        f.initializeVariables();
        f.notificationListener("fromnavactivity");


        context = new GeoApiContext.Builder().apiKey(getString(R.string.google_maps_key)).build();
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications, R.id.navigation_map,R.id.navigation_chat)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "MyTag:");
        this.mWakeLock.acquire();
    }

    @Override
    protected void onStart(){
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null){
            SendUserToLoginActivity();
        }
        else{
            CheckUserExistence();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateUserStatus("offline");
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserStatus("online");
    }

    private void CheckUserExistence() {
        final String current_user_id = mAuth.getCurrentUser().getUid();
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(current_user_id)){
                    if (!dataSnapshot.child(current_user_id).hasChild("fullname")) {
                        SendUserToSetupActivity();
                    }
                    else{
                        //updateUserStatus("online");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToSetupActivity() {
        Intent setupIntent = new Intent(NavActivity.this, SetupActivity.class);
        startActivity(setupIntent);
        finish();
    }

    private void SendUserToLoginActivity() {
        Intent setupIntent = new Intent(NavActivity.this, login.class);
        startActivity(setupIntent);
        finish();
    }

    private void updateUserStatus(String state){
        String saveCurrentDate, saveCurrentTime;
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        Map currentStateMap = new HashMap<>();
        currentStateMap.put("time",saveCurrentTime);
        currentStateMap.put("date",saveCurrentDate);
        currentStateMap.put("type",state);

        UsersRef.child(mAuth.getCurrentUser().getUid()).child("userState")
                .updateChildren(currentStateMap);
    }

    @Override
    protected void onDestroy() {
        this.mWakeLock.release();
        super.onDestroy();
    }

    @Override
    public void setFragmentStatus(boolean fragmentStatus){
        NotifFragmentStatus = fragmentStatus;
    }

    @Override
    public void passNotifCtr(ArrayList<NotificationSeenCheck> notifarr) {
        if(!NotifFragmentStatus){
            int falsectr = 0;
            for(int x=0; x<notifarr.size(); x++){
                if(!notifarr.get(x).isSeen()){
                    falsectr++;
                }
            }

            if(falsectr != 0){
                navView.getOrCreateBadge(R.id.navigation_notifications).setNumber(falsectr);
            }else{
                navView.removeBadge(R.id.navigation_notifications);
            }
        }else{
            navView.removeBadge(R.id.navigation_notifications);
        }
    }
}
