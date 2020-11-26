package ph.com.team.gobiker.ui.home;

public class Groups {
    private String group_id;
    private String group_name;
    private String group_picture;
    private String group_type;
    private String status;
    private String create_date;
    private String curr_user_joined;

    public Groups(){

    }

    public Groups(String group_id, String group_name, String group_picture, String group_type, String status, String create_date, String curr_user_joined) {
        this.group_id = group_id;
        this.group_name = group_name;
        this.group_picture = group_picture;
        this.group_type = group_type;
        this.status = status;
        this.create_date = create_date;
        this.curr_user_joined = curr_user_joined;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public String getGroup_picture() {
        return group_picture;
    }

    public void setGroup_picture(String group_picture) {
        this.group_picture = group_picture;
    }

    public String getGroup_type() {
        return group_type;
    }

    public void setGroup_type(String group_type) {
        this.group_type = group_type;
    }

    public String getCreate_date() {
        return create_date;
    }

    public void setCreate_date(String create_date) {
        this.create_date = create_date;
    }


    public String getCurr_user_joined() {
        return curr_user_joined;
    }

    public void setCurr_user_joined(String curr_user_joined) {
        this.curr_user_joined = curr_user_joined;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return this.group_name; // What to display in the Spinner list.
    }


}
