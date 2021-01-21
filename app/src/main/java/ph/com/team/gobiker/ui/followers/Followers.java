package ph.com.team.gobiker.ui.followers;

public class Followers {
    public String profileimage, fullname, uid;

    public Followers(){

    }

    public Followers(String profileimage, String fullname) {
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
}
