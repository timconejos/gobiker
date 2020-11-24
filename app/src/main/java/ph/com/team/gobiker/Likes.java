package ph.com.team.gobiker;

public class Likes {
    public String profileimage, fullname, uid;
    public boolean isSeen;

    public Likes(){

    }

    public Likes(String profileimage, String fullname) {
        this.profileimage = profileimage;
        this.fullname = fullname;
        this.uid = uid;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }
}
