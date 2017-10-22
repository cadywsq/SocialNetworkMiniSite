package cc.cmu.edu.minisite;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Siqi Wang siqiw1 on 3/22/16.
 */
public class DatabaseFormatter {
    public static void main(String[] args) throws IOException {
        formatHBase();
//        formatLinks();
    }

    // Format for HBase output, with followee id as row key, all followers including id, name, profile image url as
    // column for return.
    private static void formatHBase() throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(System.in));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
            String line;
            String lastFollowee = null;
            List<UserInfoForDatabase> followers = new ArrayList<UserInfoForDatabase>();
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                String followee = tokens[0];
                if (!followee.equals(lastFollowee)) {
                    if (!followers.isEmpty()) {
                        bw.write(formatOutput(followers, lastFollowee));
                        bw.newLine();
                        followers.clear();
                    }
                    lastFollowee = followee;
                }
                UserInfoForDatabase follower = new UserInfoForDatabase(tokens[1], tokens[2], tokens[3]);
                followers.add(follower);
            }
            if (!followers.isEmpty()) {
                bw.write(formatOutput(followers, lastFollowee));
            }
        } catch (IOException e) {
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }


    private static String formatOutput(List<UserInfoForDatabase> followers, String lastFollowee) {
        Collections.sort(followers);
        StringBuilder sb = new StringBuilder();
        for (UserInfoForDatabase follower : followers) {
            sb.append(String.format("%s,%s,%s|", follower.getId(), follower.getName(), follower.getUrl()));
        }
        return lastFollowee + "\t" + sb.toString().substring(0, sb.length() - 1);
    }

//    private static void formatLinks() throws IOException {
//        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
//            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out))) {
//                String line;
//                while ((line = br.readLine()) != null) {
//                    String[] tokens = line.split(",");
//                    if (!tokens[0].equals(tokens[2])) {
//                        bw.write(line);
//                        bw.newLine();
//                    }
//                }
//            }
//        }
//    }
}
