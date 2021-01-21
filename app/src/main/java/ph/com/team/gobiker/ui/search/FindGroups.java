package ph.com.team.gobiker.ui.search;

public class FindGroups {
    public String group_picture, group_name, status;

    public FindGroups(){

    }

    public FindGroups(String group_picture, String group_name, String status) {
        this.group_picture = group_picture;
        this.group_name = group_name;
        this.status = status;
    }

    public String getGroup_picture() {
        return group_picture;
    }

    public void setGroup_picture(String group_picture) {
        this.group_picture = group_picture;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
