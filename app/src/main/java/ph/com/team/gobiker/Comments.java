package ph.com.team.gobiker;

public class Comments {
    public String username;
    public String time;
    public String date;
    public String comment;

    public Comments(){

    }

    public Comments(String username, String time, String date, String comment) {
        this.username = username;
        this.time = time;
        this.date = date;
        this.comment = comment;
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

}
