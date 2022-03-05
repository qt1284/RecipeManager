import Controller.RecipeManager;
import com.jcraft.jsch.*;

import java.io.FileOutputStream;
import java.sql.*;
import java.util.*;
import java.util.Random;
//My imports


public class TestDatabaseConnection {
    public static void main(String[] args) throws SQLException {

        int lport = 5432;
        String rhost = "starbug.cs.rit.edu";
        int rport = 5432;
        String user = "user"; //change to your username
        String password = "pass"; //change to your password
        String databaseName = "p320_06"; //change to your database name

        String driverName = "org.postgresql.Driver";
        Connection conn = null;
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
            System.out.println("Connected");
            int assigned_port = session.setPortForwardingL(lport, "localhost", rport);
            System.out.println("Port Forwarded");

            // Assigned port could be different from 5432 but rarely happens
            String url = "jdbc:postgresql://localhost:"+ assigned_port + "/" + databaseName;

            System.out.println("database Url: " + url);
            Properties props = new Properties();
            props.put("user", user);
            props.put("password", password);

            Class.forName(driverName);
            conn = DriverManager.getConnection(url, props);
            System.out.println("Database connection established");

            // Do something with the database....
            // Here is an example note that you need to
            // change the value inserted because it has to be unique
            Statement stmt = conn.createStatement();
//            String sql = "select recipeno from recipe";
//            ResultSet res = stmt.executeQuery(sql);
//            PreparedStatement ps;
//            while(res.next()){
//                int recipeno = res.getInt("recipeno");
//                ResultSet res2 = conn.createStatement().executeQuery("select avg(star_rate) as avg from rates where recipeno = " + recipeno);
//                if (res2.next()){
//                    double avg = res2.getDouble("avg");
//                    ps = conn.prepareStatement("update recipe set rate_avg = " + avg + " where recipeno = "+ recipeno);
//                    ps.executeUpdate();
//                }
//
//            }
            //stmt.executeUpdate("insert into test values (5)");

            // Here is how we retrieve information from a table
//            ResultSet res = stmt.executeQuery("select id from test");
//            res.next();
//            System.out.println(res.getInt("id"));
//            String name = "aa";

//            ResultSet res = stmt.executeQuery("select name from recipe where recipeno in (" +
//                                        "select recipeno from recipe_ingredient where itemno in (" +
//                                        "select itemno from item where itemname = 'onion'))");
//            while (res.next()){
//                System.out.println(res.getString("name"));
//            }
//            Random rand = new Random();
//            int count = 0;
//            ResultSet res = stmt.executeQuery("select username from person");
//            List<Integer> randd = getRandomSet();
//            while (res.next()){
//                PreparedStatement ps;
//                ps = conn.prepareStatement("insert into creates values(?, ?, ?)");
//                ps.setString(1,res.getString("username"));
//                ps.setInt(2,randd.get(count));
//                ps.setString(3, getRandomDate(2018,2019));
//                ps.executeUpdate();
//                count++;
//                if (count == 50)
//                    break;
//            }
//            ResultSet res = stmt.executeQuery("select recipe_ingredient.recipeno, sum(quantity) as a, recipe.cooktime\n" +
//                    "                    from recipe_ingredient inner join recipe on recipe_ingredient.recipeno = recipe.recipeno group by recipe_ingredient.recipeno, recipe.cooktime");
//            while (res.next()){
//                PreparedStatement ps;
//                ps = conn.prepareStatement("insert into time_ingredient values(?, ?, ?)");
//                ps.setInt(1,res.getInt("recipeno"));
//                ps.setInt(2,res.getInt("a"));
//                ps.setInt(3,res.getInt("cooktime"));
//                ps.executeUpdate();
//            }
//            PreparedStatement ps;
//            ps = conn.prepareStatement("insert into rates values(?, ?, ?)");
//            ps.executeUpdate();


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Closing Database Connection");
                conn.close();
            }
            if (session != null && session.isConnected()) {
                System.out.println("Closing SSH Connection");
                session.disconnect();
            }
        }
    }
//    public static List<Integer> getRandomSet(){
//        Random randNum = new Random();
//        List<Integer>set = new ArrayList<>();
//        while (set.size() < 50) {
//            int a = randNum.nextInt(50)+1;
//            if (!set.contains(a))
//                set.add(a);
//        }
//        return set;
//    }
//    public static int randBetween(int start, int end) {
//        return start + (int)Math.round(Math.random() * (end - start));
//    }
//    public static String getRandomDate(int startYear, int endYear){
//        Random rand = new Random();
//        GregorianCalendar gc = new GregorianCalendar();
//        int year = randBetween(startYear, endYear);
//        gc.set(gc.YEAR, year);
//        int dayOfYear = randBetween(1, gc.getActualMaximum(gc.DAY_OF_YEAR));
//        gc.set(gc.DAY_OF_YEAR, dayOfYear);
//        int hour = (rand.nextInt(12)+1);
//        String hourToString;
//        if (hour < 10){
//            hourToString = "0" + hour;
//        } else
//            hourToString = ""+ hour;
//        return timeToString((gc.get(gc.MONTH) + 1)) + "-" + timeToString(gc.get(gc.DAY_OF_MONTH)) + "-" + gc.get(gc.YEAR)
//                + " " + hourToString + ":" + timeToString((rand.nextInt(60)+1)) + ":" + timeToString((rand.nextInt(60)+1)) ;
//    }
//    public static String timeToString(int time){
//        String hourToString;
//        if (time < 10){
//            hourToString = "0" + time;
//        } else
//            hourToString = ""+ time;
//        return hourToString;
//    }
}
