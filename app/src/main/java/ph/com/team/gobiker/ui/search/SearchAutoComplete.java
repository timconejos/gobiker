package ph.com.team.gobiker.ui.search;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchAutoComplete {
    private String profileuid;
    private String profilename;
    private String profileimage;

    public SearchAutoComplete(String profilename, String profileimage, String profileuid){
        this.profilename = profilename;
        this.profileimage = profileimage;
        this.profileuid = profileuid;
    }

    public String getProfilename() {
        return profilename;
    }

    public void setProfilename(String profilename) {
        this.profilename = profilename;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getProfileuid() {
        return profileuid;
    }

    public void setProfileuid(String profileuid) {
        this.profileuid = profileuid;
    }


}
