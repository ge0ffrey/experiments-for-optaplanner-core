package be.ge0ffrey.mysandbox;

/**
 * @author Geoffrey De Smet
 */
public class Dog {

    private String name;
    private int age;
    private String favoriteBone;
    private Person owner;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getFavoriteBone() {
        return favoriteBone;
    }

    public void setFavoriteBone(String favoriteBone) {
        this.favoriteBone = favoriteBone;
    }

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

}
