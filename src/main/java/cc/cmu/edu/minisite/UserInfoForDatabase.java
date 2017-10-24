package cc.cmu.edu.minisite;

/**
 * Class for database formatting.
 *
 * @author Siqi Wang siqiw1 on 3/22/16.
 */
public class UserInfoForDatabase implements Comparable<UserInfoForDatabase> {
    private String id;
    private String name;
    private String url;

    public UserInfoForDatabase(String id, String name, String url) {
        setId(id);
        setName(name);
        setUrl(url);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    //sort the followers alphabetically in ascending order by Name.
    //If there is a tie in the followers name, sort alphabetically by their Profile Image URL in ascending order.
    @Override
    public int compareTo(UserInfoForDatabase o) {
        if (!this.getName().equals(o.getName())) {
            return this.getName().compareTo(o.getName());
        }
        return this.getUrl().compareTo(o.getUrl());
    }

    @Override
    public String toString() {
        return String.format("%s,%s|", getName(), getUrl());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
