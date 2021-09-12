package ph.com.team.gobiker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;

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
import androidx.core.app.NotificationCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import ph.com.team.gobiker.ui.chat.ChatActivity;
import ph.com.team.gobiker.ui.chat.ChatFragment;
import ph.com.team.gobiker.ui.chat.ChatGroupActivity;
import ph.com.team.gobiker.ui.notifications.NotificationSeenCheck;
import ph.com.team.gobiker.ui.notifications.NotificationsFragment;

public class NavActivity extends AppCompatActivity implements NotificationsFragment.Listener, ChatFragment.Listener{

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    public static GeoApiContext context;
    protected PowerManager.WakeLock mWakeLock;
    public static BottomNavigationView navView;
    private NotificationsFragment notifFrag;
    private ChatFragment chatFrag;
    private boolean NotifFragmentStatus = false;
    private boolean ChatFragmentStatus = false;
    private ArrayList<String> chatNotifArr;
    public static int globalchatctr = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);
        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser() != null){
            UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
            navView = findViewById(R.id.nav_view);

            //retrieve notification counter for nav badge
            notifFrag = new NotificationsFragment();
            notifFrag.setListener(this);
            notifFrag.initializeVariables();
            notifFrag.notificationListener("fromnavactivity");

            chatFrag = new ChatFragment();
            chatFrag.setListener(this);
            chatFrag.InitializeVariables();
            chatFrag.chatNotifListener();

            chatNotifArr = new ArrayList<>();


            context = new GeoApiContext.Builder().apiKey(getString(R.string.google_maps_key)).build();
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications, R.id.navigation_map,R.id.navigation_chat)
                    .build();
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(navView, navController);
        }

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
        Intent setupIntent = new Intent(NavActivity.this, MainScreenActivity.class);
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
//                    notifyThis("Notification", notifarr.get(x).getDescription());
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

    @Override
    public void setChatFragmentStatus(boolean fragStatus) {
        ChatFragmentStatus = fragStatus;
    }

    @Override
    public void passChatCtr(int chatctr, String from, String message, String messageid, String chatid, String chattype) {
        if(!ChatFragmentStatus){
            if(chatctr != 0){
                navView.getOrCreateBadge(R.id.navigation_chat).setNumber(chatctr);
            }else{
                navView.removeBadge(R.id.navigation_chat);
            }
            if(!chatNotifArr.contains(messageid)){
                chatNotifArr.add(messageid);
                notifyThis(from, message, chatctr, chatid, chattype);
            }
            globalchatctr = chatctr;
        }else{
            chatNotifArr.clear();
            navView.removeBadge(R.id.navigation_chat);
        }
    }

    private void notifyThis(String title, String message, int notifid, String chatid, String chattype){
        if(title != "none"){
            NotificationManager mNotificationManager;

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(NavActivity.this, "notify_001");

            if(chattype.equals("single")){
                Intent ii = new Intent(NavActivity.this, ChatActivity.class);
                ii.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ii.putExtra("visit_user_id",chatid);
                ii.putExtra("from", "notification");
                ii.setAction("notification");
                PendingIntent pendingIntent = PendingIntent.getActivity(NavActivity.this, 0, ii, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(pendingIntent);
            }else{
                Intent ii = new Intent(NavActivity.this, ChatGroupActivity.class);
                ii.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ii.putExtra("gcKey", chatid);
                ii.putExtra("from", "notification");
                ii.setAction("notification");
                PendingIntent pendingIntent = PendingIntent.getActivity(NavActivity.this, 0, ii, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(pendingIntent);
            }

            NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();;
            mBuilder.setSmallIcon(R.drawable.main_logo_wbg);
            mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.main_logo_wbg));
            mBuilder.setContentTitle(title);
            mBuilder.setContentText(message);
            mBuilder.setPriority(Notification.PRIORITY_MAX);
            mBuilder.setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
            mBuilder.setContentInfo("GoBiker");
            mBuilder.setStyle(bigText);

            mNotificationManager =
                    (NotificationManager) NavActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                String channelId = "1003";
                NotificationChannel channel = new NotificationChannel(
                        channelId,
                        "GoBiker Notification Channel",
                        NotificationManager.IMPORTANCE_HIGH);
                channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null);
                channel.setLightColor(Color.GREEN);
                channel.enableVibration(true);
                channel.enableLights(true);
                mNotificationManager.createNotificationChannel(channel);


                mBuilder.setChannelId(channelId);
            }
            mNotificationManager.notify(notifid, mBuilder.build());
        }
    }
}
