package ph.com.team.gobiker.ui.home;

public class GroupMemberProfile {
    public String gid;
    public String uid;
    public String description;
    public String profileimage;
    public String currentrole;

    public GroupMemberProfile(){

    }

    public GroupMemberProfile(String gid, String uid, String description, String profileimage, String currentrole) {
        this.gid = gid;
        this.uid = uid;
        this.description = description;
        this.profileimage = profileimage;
        this.currentrole = currentrole;
    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public String getCurrentrole() {
        return currentrole;
    }

    public void setCurrentrole(String currentrole) {
        this.currentrole = currentrole;
    }


}
