package ph.com.team.gobiker.ui.chat;

import java.util.ArrayList;

public class GroupChats {
    private String gc_name;
    private String gc_picture;
    private ArrayList<String> gc_participants;

    public GroupChats(){

    }

    public GroupChats(String gc_name, String gc_picture, ArrayList<String> gc_participants) {
        this.gc_name = gc_name;
        this.gc_picture = gc_picture;
        this.gc_participants = gc_participants;
    }

    public String getGc_name() {
        return gc_name;
    }

    public void setGc_name(String gc_name) {
        this.gc_name = gc_name;
    }

    public String getGc_picture() {
        return gc_picture;
    }

    public void setGc_picture(String gc_picture) {
        this.gc_picture = gc_picture;
    }

    public ArrayList<String> getGc_participants() {
        return gc_participants;
    }

    public void setGc_participants(ArrayList<String> gc_participants) {
        this.gc_participants = gc_participants;
    }
}
