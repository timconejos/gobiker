package ph.com.team.gobiker.ui.notifications;

import java.util.Comparator;

public class Notifications  {
    public String uid;
    public String time;
    public String date;
    public String description;
    public String profileimage;
    public String notiftype;
    public String postid;
    public Boolean isSeen;

    public Notifications(){

    }

    public Notifications(String uid, String time, String date, String description,String profileimage, String notiftype, String postid, Boolean isSeen) {
        this.uid = uid;
        this.time = time;
        this.date = date;
        this.description = description;
        this.profileimage = profileimage;
        this.notiftype = notiftype;
        this.postid = postid;
        this.isSeen = isSeen;
    }
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getNotiftype(){ return notiftype; }

    public void setNotiftype(String notiftype) { this.notiftype = notiftype; }

    public String getPostid(){ return postid; }

    public void setPostid(String postid) { this.postid = postid; }


    public Boolean getSeen() {
        return isSeen;
    }

    public void setSeen(Boolean seen) {
        isSeen = seen;
    }

    @Override
    public boolean equals(Object obj) {
        if (this.description.equals(((Notifications)obj).getDescription())) {
            return true;
        }

        return false;
    }

}
