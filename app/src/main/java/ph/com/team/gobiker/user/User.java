package ph.com.team.gobiker.user;

public class User {

    //to fix as the standard for user data
    private String UUID;
    private String fullname;

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    private int age;
    private float weight;
    private float height;
    private String gender;

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public User(String UUID) {
        this.UUID = UUID;
        this.fullname = "";
        this.age = 0;
        this.weight = 0;
        this.height = 0;
        this.gender = "";
    }
}
