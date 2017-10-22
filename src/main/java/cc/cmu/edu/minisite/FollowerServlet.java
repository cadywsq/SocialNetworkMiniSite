package cc.cmu.edu.minisite;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class FollowerServlet extends HttpServlet {
    /**
     * The private IP address of HBase master node.
     */
    private static String zkAddr = "172.31.60.48"; //TODO: change private IP of master.
    /**
     * The name of your HBase table.
     */
    private static String tableName = "followers";
    /**
     * HTable handler.
     */
    private static HTableInterface followerTable;
    /**
     * HBase connection.
     */
    private static HConnection conn;
    /**
     * Byte representation of column family.
     */
    private static byte[] bColFamily = Bytes.toBytes("data");
    /**
     * Logger.
     */
    private final static Logger logger = Logger.getRootLogger();

    public FollowerServlet() throws IOException {
        /*
            Your initialization code goes here
        */
        logger.setLevel(Level.WARN);
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.master", zkAddr + ":60000");
        conf.set("hbase.zookeeper.quorum", zkAddr);
        conf.set("hbase.zookeeper.property.clientport", "2181");
        if (!zkAddr.matches("\\d+.\\d+.\\d+.\\d+")) {
            System.out.print("HBase not configured!");
            return;
        }
        conn = HConnectionManager.createConnection(conf);
        followerTable = conn.getTable(Bytes.toBytes(tableName));
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        new FollowerServlet();
        String id = request.getParameter("id");
        ArrayList<JSONObject> followers = getFollowers(id);
        JSONArray result = new JSONArray();
        JSONObject output = new JSONObject();
        /*
            Task 2:
            Implement your logic to retrive the followers of this user. 
            You need to send back the Name and Profile Image URL of his/her Followers.

            You should sort the followers alphabetically in ascending order by Name. 
            If there is a tie in the followers name, 
	    sort alphabetically by their Profile Image URL in ascending order. 
        */
        for (JSONObject follower : followers) {
            result.put(follower);
        }
        output.put("followers",result);

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
     * Get user info for all followers.
     * @param followeeId the queried userID
     * @return list of followers info
     * @throws IOException
     */
    ArrayList<UserInfo> getFollowerInfo(String followeeId) throws IOException {
        Get get = new Get(Bytes.toBytes(followeeId));
        get.addColumn(bColFamily, Bytes.toBytes("followee"));
        get.addColumn(bColFamily, Bytes.toBytes("output"));
        Result result = followerTable.get(get);
        String output = new String(result.value());

        ArrayList<UserInfo> followerList = new ArrayList<UserInfo>();
        String[] followers = output.split("\\|");

        for (String follower : followers) {
            String[] tokens = follower.split(",");
            followerList.add(new UserInfo(tokens[1], tokens[2], null));
        }
        Collections.sort(followerList);
        return followerList;
    }

    // Transfer all followers info from UserInfo object to JSON object.
    ArrayList<JSONObject> getFollowers(String followeeId) throws IOException {
        List<UserInfo> followerList = getFollowerInfo(followeeId);
        ArrayList<JSONObject> followers = new ArrayList<JSONObject>();
        for (UserInfo userInfo : followerList) {
            JSONObject follower = new JSONObject();
            follower.put("name", userInfo.getName());
            follower.put("profile", userInfo.getUrl());
            followers.add(follower);
        }
        return followers;
    }
}


