package ph.com.team.gobiker.ui.map;

import android.location.Location;

import java.util.Calendar;
import java.util.Date;

public class CurrentLocation {
    private MyLocation location;
    private Date lastUpdated;

    public CurrentLocation() { }
    public CurrentLocation(MyLocation location) {
        this.location = location;
        this.lastUpdated = Calendar.getInstance().getTime();
    }

    public MyLocation getLocation() {
        return location;
    }

    public void setLocation(MyLocation location) {
        this.location = location;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
