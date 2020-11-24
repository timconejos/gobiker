package ph.com.team.gobiker.ui.chat;

public class FindChat {
    public String id;
    public String profileimage;
    public String fullname;
    public String message;
    public String datetime;
    public String chattype;

    public FindChat(){

    }

    public FindChat(String id, String profileimage, String fullname, String message, String datetime, String chattype) {
        this.id = id;
        this.profileimage = profileimage;
        this.fullname = fullname;
        this.message = message;
        this.datetime = datetime;
        this.chattype = chattype;
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

    public String getChattype() {
        return chattype;
    }

    public void setChattype(String chattype) {
        this.chattype = chattype;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
