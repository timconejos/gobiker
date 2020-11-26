package ph.com.team.gobiker.ui.notifications;

import android.app.Notification;

public class NotificationSeenCheck {
    private String description;
    private boolean isSeen;

    public NotificationSeenCheck(){

    }

    public NotificationSeenCheck(String description, boolean isSeen) {
        this.description = description;
        this.isSeen = isSeen;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    @Override
    public boolean equals(Object obj) {
        if (this.description.equals(((NotificationSeenCheck)obj).getDescription())) {
            return true;
        }

        return false;
    }
}
