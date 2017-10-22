package cc.cmu.edu.minisite;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.ascending;


public class HomepageServlet extends HttpServlet {
    private final MongoDatabase db;
    private final MongoClient mongoClient;

    public HomepageServlet() {
        /*
            Your initialization code goes here
        */
        mongoClient = new MongoClient(new ServerAddress("172.31.12.5", 27017));
        db = mongoClient.getDatabase("socialnetwork");
    }

    @Override
    protected void doGet(final HttpServletRequest request,
                         final HttpServletResponse response) throws ServletException, IOException {
        new HomepageServlet();
        String id = request.getParameter("id");
        JSONArray results = new JSONArray();
        JSONObject output = new JSONObject();
        /*
            Task 3:
            Implement your logic to return all the posts authored by this user.
            Return this posts as-is, but be cautious with the order.

            You will need to sort the posts by Timestamp in ascending order
	     (from the oldest to the latest one). 
        */
        ArrayList<JSONObject> posts = getAllPostsAscending(id);
        for (JSONObject post : posts) {
            results.put(post);
        }
        output.put("posts", results);
        PrintWriter writer = response.getWriter();
        writer.write(String.format("returnRes(%s)", output.toString()));
        writer.close();
    }

    @Override
    protected void doPost(final HttpServletRequest request,
                          final HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    ArrayList<JSONObject> getAllPostsAscending(String userId) {
        FindIterable<Document> iterable = db.getCollection("posts").find(eq("uid", Integer.parseInt(userId))).sort(ascending
                ("timestamp"));
        final ArrayList<JSONObject> posts = new ArrayList<JSONObject>();
        iterable.forEach(new Block<Document>() {
            @Override
            public void apply(final Document document) {
                JSONObject post = new JSONObject(document);
                posts.add(post);
            }
        });
        return posts;
    }

    // Get followees' latest 30 posts, for Timeline usage.
    ArrayList<JSONObject> getAllPostsDescending(ArrayList<String> followeeList) {
        FindIterable<Document> iterable;
        final ArrayList<JSONObject> posts = new ArrayList<JSONObject>();
        Bson[] uidFilters = new Bson[followeeList.size()];
        for (int i = 0; i < uidFilters.length; i++) {
            uidFilters[i] = Filters.eq("uid", Integer.parseInt(followeeList.get(i)));
        }
        iterable = db.getCollection("posts").find(Filters.or(uidFilters))
                .sort(new BasicDBObject("timestamp", -1).append("pid", 1)).limit(30);

        iterable.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                JSONObject post = new JSONObject(document);
                posts.add(0, post);
            }
        });
        return posts;
    }

}

