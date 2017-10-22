package cc.cmu.edu.minisite;

import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProfileServlet extends HttpServlet {

    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_NAME = "profile";
    private static final String URL = "jdbc:mysql://mysqlinstance15619.cqoe5h7exved.us-east-1.rds.amazonaws.com:3306/" +
            DB_NAME;

    private static final String DB_USER = "siqiw1";
    private static final String DB_PWD = "db15619root";

    static Connection conn;

    public ProfileServlet() {
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(URL, DB_USER, DB_PWD);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        new ProfileServlet();
        JSONObject result = new JSONObject();

        String id = request.getParameter("id");
        String pwd = request.getParameter("pwd");
        /*
            Task 1:
            This query simulates the login process of a user, 
            and tests whether your backend system is functioning properly. 
            Your web application will receive a pair of UserID and Password, 
            and you need to check in your backend database to see if the 
	    UserID and Password is a valid pair. 
            You should construct your response accordingly:

            If YES, send back the user's Name and Profile Image URL.
            If NOT, set Name as "Unauthorized" and Profile Image URL as "#".
        */
        PrintWriter writer = response.getWriter();
        UserInfo userInfo = getUserInfo(id);
        if (pwd.equals(userInfo.getPassword())) {
            result.put("name", userInfo.getName());
            result.put("profile", userInfo.getUrl());
        } else {
            result.put("name", "Unauthorized");
            result.put("profile", "#");
        }

        writer.write(String.format("returnRes(%s)", result.toString()));
        writer.close();
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    // Query MySQL database for user information regarding password, name, profile image URL.
    // Database schema users.userid | users.password + userinfo.userid | userinfo.name | userinfo.url
    UserInfo getUserInfo(String userId) {
        PreparedStatement pstmt = null;
        ResultSet rs;
        try {
            pstmt = conn.prepareStatement("\n" +
                    "SELECT users.password, userinfo.name, userinfo.url \n" +
                    "FROM users, userinfo\n" +
                    "WHERE(users.userid=? AND userinfo.userid=?)");
            pstmt.setString(1, userId);
            pstmt.setString(2, userId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                String actualPwd = rs.getString("password");
                String url = rs.getString("url");
                String name = rs.getString("name");
                return new UserInfo(name, url, actualPwd);
            }
            return new UserInfo(null, "#", null);

        } catch (SQLException e) {
            System.out.println("SQL syntax error");
            return new UserInfo(null, "#", null);
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
