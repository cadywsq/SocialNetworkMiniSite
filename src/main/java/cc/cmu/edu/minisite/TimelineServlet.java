package cc.cmu.edu.minisite;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class TimelineServlet extends HttpServlet {


    private final ProfileServlet profileServlet;
    private final FollowerServlet followerServlet;
    private final HomepageServlet homepageServlet;

    public TimelineServlet() throws IOException {
        /*
            Your initialization code goes here
        */
        profileServlet = new ProfileServlet();
        followerServlet = new FollowerServlet();
        homepageServlet = new HomepageServlet();
    }

    @Override
    protected void doGet(final HttpServletRequest request,
                         final HttpServletResponse response) throws ServletException, IOException {
        JSONObject output = new JSONObject();
        String id = request.getParameter("id");
        JSONArray followersArray = new JSONArray();
        JSONArray postsArray = new JSONArray();
        /*
            Task 4 (1):
            Get the name and profile of the user as you did in Task 1
            Put them as fields in the result JSON object
        */
        UserInfo userInfo = profileServlet.getUserInfo(id);
        output.put("name", userInfo.getName());
        output.put("profile", userInfo.getUrl());
        /*
            Task 4 (2);
            Get the follower name and profiles as you did in Task 2
            Put them in the result JSON object as one array
        */
        ArrayList<JSONObject> followers = followerServlet.getFollowers(id);
        for (JSONObject follower : followers) {
            followersArray.put(follower);
        }

        /*
            Task 4 (3):
            Get the 30 LATEST followee posts and put them in the
            result JSON object as one array.

            The posts should be sorted:
            First in ascending timestamp order
            Then numerically in ascending order by their PID (PostID) 
	    if there is a tie on timestamp
        */
        ArrayList<String> followeeList = findFollowee(id);
        ArrayList<JSONObject> posts = homepageServlet.getAllPostsDescending(followeeList);
        for (JSONObject post : posts) {
            postsArray.put(post);
        }

        output.put("followers", followersArray);
        output.put("posts", postsArray);

        PrintWriter out = response.getWriter();
        out.print(String.format("returnRes(%s)", output.toString()));
        out.close();
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    /**
     * Find out all followees of the queried user by querying MySQL database.
     * @param userId
     * @return
     */
    ArrayList<String> findFollowee(String userId) {
        PreparedStatement pstmt = null;
        ResultSet rs;
        ArrayList<String> followees = new ArrayList<String>();
        try {
            pstmt = ProfileServlet.conn.prepareStatement("SELECT followee FROM followees WHERE follower=?");
            pstmt.setString(1, userId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                followees.add(rs.getString("followee"));
            }
            return followees;
        } catch (SQLException e) {
            System.out.println("SQL syntax error.");
            return followees;
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    System.out.println("SQL syntax error.");
                }
            }
        }
    }


}

