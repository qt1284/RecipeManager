package Controller;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.Scanner;

/**
 * File: UserManager
 * Description: UserManager handles the functionality for User such as
 * signing up for an account or logging in to existing account.
 * Authors: Eric Barron (enb3521), Dao Tran (dat1614), Quang Tran (qt1284), Quan Do (qdd7858)
 */
public class UserManager {

    /**
     * The user's username
     */
    private String username;

    /**
     * The user's password
     */
    private String password;

    /**
     * Scanner for user input
     */
    private final Scanner s = new Scanner(System.in);

    private final Properties props;
    private final String url;

    /**
     * Constructor initializes UserManager.
     */
    public UserManager( Properties props, String url){
        this.props = props;
        this.url = url;
    }

    /**
     * Method get the user's username.
     * @return the string of username
     */
    public String getUsername(){
        return username;
    }

    /**
     * Method that asks for and gets the input from user.
     * @return false if user wants to exist; otherwise true
     */
    public boolean welcome() {
        System.out.println("Would you like to signup or login or exit? (enter 'signup' or 'login' or 'exit') ");
        System.out.print("> ");
        String signUpOrLogIn = s.nextLine();
        while (!(signUpOrLogIn.equalsIgnoreCase("signup") ||
                signUpOrLogIn.equalsIgnoreCase("login") ||
                signUpOrLogIn.equalsIgnoreCase("exit"))){
            System.out.println("Please enter 'signup' or 'login' or 'exit': ");
            System.out.print("> ");
            signUpOrLogIn = s.nextLine();
        }
        try {
            if (signUpOrLogIn.equalsIgnoreCase("signup")) {
                return createNewUser();
            } else if (signUpOrLogIn.equalsIgnoreCase("login")) {
                return loginExisting();
            } else {
                return false;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Method that gets and validates the username and password.
     * @return true if the username and password are correct; otherwise false
     */
    private boolean loginExisting() {
        System.out.println("Enter your username: ");
        System.out.print("> ");
        username = s.nextLine();
        while(username.equalsIgnoreCase("exit")){
            System.out.println("Username cannot be 'exit', please try again: ");
            System.out.print("> ");
            username = s.nextLine();
        }
        System.out.println("Enter your password: ");
        System.out.print("> ");
        password = s.nextLine();

        try {
            // TODO: merge update login session together when validate user
            while(!validateUser()) {
                System.out.println("Incorrect login information");
                System.out.println("Enter your username, " +
                        "or enter 'exit' to end the program:");
                System.out.print("> ");
                username = s.nextLine();
                if(username.equalsIgnoreCase("exit")){
                    return false;
                    //I add this to fix error of login error then hit 'create' cause problem
                }
                System.out.println("Enter your password: ");
                System.out.print("> ");
                password = s.nextLine();
            }
            updateLogin();

        }
        catch(Exception e){
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Method that gets the access date and update that to the user's account.
     */
    private void updateLogin() throws SQLException{
        Connection conn = null;
        try{
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            conn = DriverManager.getConnection(url, props);
            PreparedStatement ps = conn.prepareStatement("update person set accessdate = ? where username = ?");
            ps.setString(1, dtf.format(now));
            ps.setString(2, username);
            ps.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        }
    }

    /**
     * Method that check if the username is available.
     * @param name the username
     * @return true if the username is available; otherwise, false
     */
    private boolean isAvailableUsername(String name) throws SQLException{
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, props);
            ResultSet rs = conn.createStatement().executeQuery("select username from person");
            while(rs.next()){
                if(rs.getString("username").equals(name)){
                    return false;
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        } finally{
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        }
        return true;
    }

    /**
     * Method that creates the new account.
     * @return true if the new account is created; otherwise, false
     */
    private boolean createNewUser() {
        System.out.println("Enter your desired username: ");
        System.out.print("> ");
        username = s.nextLine();
        if(username.equalsIgnoreCase("exit")){
            return false;
        }

        try {
            while(!isAvailableUsername(username)){
                System.out.println("Sorry that username is not available, please try again");
                System.out.println("Enter your desired username: ");
                System.out.print("> ");
                username = s.nextLine();
                if(username.equalsIgnoreCase("exit")){
                    return false;
                }
            }
            System.out.println("Enter your password: ");
            System.out.print("> ");
            password = s.nextLine();

            insertNewUser();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Method that inserts the new account into the Person table in database.
     */
    private void insertNewUser() throws SQLException{
        Connection conn = null;
        try{
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();

            conn = DriverManager.getConnection(url, props);
            PreparedStatement ps = conn.prepareStatement("insert into person values(?, ?, ?, ?)");
            ps.setString(1, username);
            ps.setString(2, dtf.format(now));
            ps.setString(3, dtf.format(now));
            ps.setString(4, password);
            ps.executeUpdate();
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        }
    }

    /**
     * Method that validates the user.
     * @return true if the username and password are correct; otherwise, false
     */
    private boolean validateUser() throws SQLException{
        Connection conn = null;
        boolean valid = false;
        try{
            conn = DriverManager.getConnection(url, props);
            PreparedStatement ps = conn.prepareStatement("select password from person where username = ?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                valid = rs.getString("password").equals(password);
            }

        } catch (Exception e){
            e.printStackTrace();
        } finally{
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        }
        return valid;
    }
}
