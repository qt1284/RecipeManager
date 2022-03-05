package Controller;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.sql.Connection;
import java.util.Properties;

/**
 * File: WelcomePage.java
 * Description: The driver class, it will get the information needed for a connection, handle the user
 * login, and direct the user to the main application functionality
 * Authors: Eric Barron (enb3521), Dao Tran (dat1614), Quang Tran (qt1284), Quan Do (qdd7858)
 *
 */
public class WelcomePage {

    /**
     * The main class, it will log a user in before allowing them to access the
     * application functionality
     */
    public static void main(String [] args) {
        int lport = 5432;
        String rhost = "starbug.cs.rit.edu";
        int rport = 5432;
        String user = args[0]; //change to your username
        String password = args[1]; //change to your password
        String databaseName = "p320_06"; //change to your database name

        String driverName = "org.postgresql.Driver";
        Session session = null;
        try {

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            session = jsch.getSession(user, rhost, 22);
            session.setPassword(password);
            session.setConfig(config);
            session.setConfig("PreferredAuthentications","publickey,keyboard-interactive,password");
            session.connect();
            int assigned_port = session.setPortForwardingL(lport, "localhost", rport);
            String url = "jdbc:postgresql://localhost:"+ assigned_port + "/" + databaseName;
            Properties props = new Properties();
            props.put("user", user);
            props.put("password", password);
            Class.forName(driverName);

            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("Welcome to the Chicken Recipe Database!");

            UserManager userManager = new UserManager(props, url);
            boolean loggedIn = userManager.welcome();

            if(loggedIn){
                System.out.println("Successfully logged in!");
                RecipeApp app =  new RecipeApp(props, url);
                app.run(userManager.getUsername());
            } else {
                System.out.println("You are successfully exit. Thank you!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }

    }


}
