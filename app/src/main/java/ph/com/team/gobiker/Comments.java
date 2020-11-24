package ph.com.team.gobiker;

public class Comments {
    public String username;
    public String time;
    public String date;
    public String comment;
    public String uid;
    public String profileimage;
    public Boolean isSeen;

    public Comments(){

    }

    public Comments(String username, String time, String date, String comment,String uid,String profileimage) {
        this.username = username;
        this.time = time;
        this.date = date;
        this.comment = comment;
        this.uid = uid;
        this.profileimage = profileimage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getComments() {
        return comment;
    }

    public void setComments(String comments) {
        this.comment = comments;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }


    public Boolean getSeen() {
        return isSeen;
    }

    public void setSeen(Boolean seen) {
        isSeen = seen;
    }
}
