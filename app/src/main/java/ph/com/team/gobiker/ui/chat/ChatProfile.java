package ph.com.team.gobiker.ui.chat;

import java.io.Serializable;

@SuppressWarnings("serial") //With this annotation we are going to hide compiler warnings
public class ChatProfile implements Serializable {
    public String uid;
    public String description;
    public String profileimage;

    public ChatProfile(){

    }

    public ChatProfile(String uid, String description,String profileimage) {
        this.uid = uid;
        this.description = description;
        this.profileimage = profileimage;
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

    @Override
    public boolean equals(Object obj) {
        if(this == obj){
            return true;
        }

        if((obj == null) || (obj.getClass() != this.getClass())){
            return false;
        }

        ChatProfile that = (ChatProfile) obj;

        // Use the equality == operator to check if the argument is the reference to this object,
        // if yes. return true. This saves time when actual comparison is costly.
        return  uid == that.uid &&
                (description == that.description || (description != null && description.equals(that.description)));

    }


}
