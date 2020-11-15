package ph.com.team.gobiker.ui.map;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SendLocationAsync extends AsyncTask<Void, Void, Void> {
    public static MyLocation currentLocation;
    public static String currentUserUID;

    public SendLocationAsync(MyLocation currentLocation, String currentUserUID) {
        this.currentLocation = currentLocation;
        this.currentUserUID = currentUserUID;
    }


    @Override
    protected Void doInBackground(Void... voids) {
        DatabaseReference locRef = FirebaseDatabase.getInstance().getReference().child("Location").child(currentUserUID);
        Log.d("firebase", FirebaseDatabase.getInstance().getReference().toString());
        locRef.setValue(new CurrentLocation(currentLocation));
        Log.d("firebase", "sending location of user: " +  currentUserUID);
        return null;
    }

    //CurrentLocation currentLocationSend = new CurrentLocation(currentLocation);
    //        DatabaseReference locRef = FirebaseDatabase.getInstance().getReference().child("Location").child(currentUserUID);
    //        Log.d("firebase", FirebaseDatabase.getInstance().getReference().toString());
    //        locRef.setValue(currentLocationSend);
    //        Log.d("firebase", "sending location of user: " +  currentUserUID);
    //        try {
    //            Thread.sleep(2000);
    //        } catch (InterruptedException e) {
    //            e.printStackTrace();
    //        }
}
