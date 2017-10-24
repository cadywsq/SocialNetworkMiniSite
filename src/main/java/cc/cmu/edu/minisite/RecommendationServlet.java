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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class RecommendationServlet extends HttpServlet {
    FollowerServlet followerServlet;
    TimelineServlet timelineServlet;
    ProfileServlet profileServlet;

    class Followee implements Comparable<Followee> {
        private String followeeId;
        private int score;
        private String name;
        private String url;

        public Followee(String followeeId, int score, String name, String url) {
            this.setFolloweeId(followeeId);
            this.setScore(score);
            setName(name);
            setUrl(url);
        }

        public String getFolloweeId() {
            return followeeId;
        }

        public void setFolloweeId(String followeeId) {
            this.followeeId = followeeId;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        @Override
        public int compareTo(Followee o) {
            if (this.score != o.score) {
                return o.score - this.score;
            }
            return Integer.valueOf(this.followeeId) - Integer.valueOf(o.followeeId);
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
    }

    public RecommendationServlet() throws IOException {
        /*
            Your initialization code goes here
         */
        followerServlet = new FollowerServlet();
        timelineServlet = new TimelineServlet();
        profileServlet = new ProfileServlet();
    }

    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        new RecommendationServlet();
        String id = request.getParameter("id");
        System.out.println("query id: " + id);
        JSONArray result = new JSONArray();
        JSONObject output = new JSONObject();
        /**
         * Bonus task:
         *
         * Recommend at most 10 people to the given user with simple collaborative filtering.
         *
         * Store your results in the result object in the following JSON format:
         * recommendation: [
         *        {name:<name_1>, profile:<profile_1>}
         *        {name:<name_2>, profile:<profile_2>}
         *        {name:<name_3>, profile:<profile_3>}
         * 		...
         *        {name:<name_10>, profile:<profile_10>}
         * ]
         *
         * Notice: make sure the input has no duplicate!
         */
        ArrayList<JSONObject> recommendationList = getRecommendationList(id);
        for (JSONObject recommendation : recommendationList) {
            result.put(recommendation);
        }
        output.put("recommendation", result);

        PrintWriter writer = response.getWriter();
        writer.write(String.format("returnRes(%s)", output.toString()));
        writer.close();

    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }


    /**
     * Search for all followees with relation distance 2 for the queried user.
     *
     * @param userId
     * @return map of second level followees, id as key and score as value.
     */
    private Map<String, Integer> findSecondLevelFollowee(String userId) {
        PreparedStatement pstmt = null;
        ResultSet rs;
        Map<String, Integer> followerMap = new HashMap<String, Integer>();
        ArrayList<String> firstLevelFollowee = timelineServlet.findFollowee(userId);
        for (String uid : firstLevelFollowee) {
            try {
                pstmt = ProfileServlet.conn.prepareStatement("SELECT followee FROM followees WHERE follower=?");
                pstmt.setString(1, uid);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    String followeeId = rs.getString("followee");
                    if (followerMap.containsKey(followeeId)) {
                        followerMap.put(followeeId, followerMap.get(followeeId) + 1);
                    } else {
                        followerMap.put(followeeId, 1);
                    }
                }
            } catch (SQLException e) {
                System.out.println("SQL syntax error.");
                return null;
            }
        }
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        followerMap.remove(userId);
        for (String s : firstLevelFollowee) {
            followerMap.remove(s);
        }
        return followerMap;
    }

    /**
     * Transfer all second level followee user info to list, and sort as required.
     *
     * @param userId
     * @return List of all recommended followees.
     */
    private ArrayList<Followee> getAllRecommendation(String userId) {
        Map<String, Integer> secondLevelFolloweeMap = findSecondLevelFollowee(userId);
        ArrayList<Followee> allRecommendationList = new ArrayList<Followee>();
        for (Map.Entry<String, Integer> entry : secondLevelFolloweeMap.entrySet()) {
            String id = entry.getKey();
            UserInfo userInfo = profileServlet.getUserInfo(id);
            Followee followee = new Followee(id, entry.getValue(), userInfo.getName(), userInfo.getUrl());
            allRecommendationList.add(followee);
        }
        Collections.sort(allRecommendationList);
        for (int i = 0; i < 10; i++) {
            Followee followee = allRecommendationList.get(i);
            System.out.println("id: " + followee.getFolloweeId() + "\t" + "name: " + followee.getName() + "\t" +
                    "score: " + followee.getScore() + "\n");
        }
        return allRecommendationList;
    }

    /**
     * Return list of the top ten recommended user in JSON format.
     *
     * @param userId
     * @return
     */
    private ArrayList<JSONObject> getRecommendationList(String userId) {
        ArrayList<JSONObject> selectedRecommend = new ArrayList<JSONObject>();
        ArrayList<Followee> recommendList = getAllRecommendation(userId);
        assert !recommendList.isEmpty();
        if (recommendList.size() >= 10) {
            for (int i = 0; i < 10; i++) {
                JSONObject followee = new JSONObject();
                followee.put("name", recommendList.get(i).getName());
                followee.put("profile", recommendList.get(i).getUrl());
                selectedRecommend.add(followee);
            }
        } else {
            for (Followee followee1 : recommendList) {
                JSONObject followee = new JSONObject();
                followee.put("name", followee1.getName());
                followee.put("profile", followee1.getUrl());
                selectedRecommend.add(followee);
            }
        }
        return selectedRecommend;
    }
}

