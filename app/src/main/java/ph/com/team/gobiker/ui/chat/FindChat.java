package ph.com.team.gobiker.ui.chat;

public class FindChat {
    public String profileimage;
    public String fullname;
    public String message;
    public String datetime;

    public FindChat(){

    }

    public FindChat(String profileimage, String fullname, String message, String datetime) {
        this.profileimage = profileimage;
        this.fullname = fullname;
        this.message = message;
        this.datetime = datetime;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }
}
