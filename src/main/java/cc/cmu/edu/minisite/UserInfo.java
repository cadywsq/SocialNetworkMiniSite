package cc.cmu.edu.minisite;

/**
 * @author Siqi Wang siqiw1 on 3/22/16.
 */
public class UserInfo implements Comparable<UserInfo> {
    private String name;
    private String url;
    private String password;

    public UserInfo(String name, String url, String password) {
        setName(name);
        setUrl(url);
        setPassword(password);
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public int compareTo(UserInfo o) {
        if (!this.name.equals(o.name)) {
            return this.name.compareTo(o.name);
        }
        return this.url.compareTo(o.url);
    }
}
