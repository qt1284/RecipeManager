package Model;

/**
 * File: User
 * Description: User class holds the Pantry Object and username.
 * Authors: Eric Barron (enb3521), Dao Tran (dat1614), Quang Tran (qt1284), Quan Do (qdd7858)
 */
public class User {
    private String username;
    private Pantry pantry;

    public User(String username){
        this.username = username;
        pantry = new Pantry(username);
    }
}
