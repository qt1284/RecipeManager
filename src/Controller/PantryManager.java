package Controller;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * File: PantryManager
 * Description: PantryManager handles the functionality for a pantry such as adding
 * or removing items and creating a new pantry.
 * Authors: Eric Barron (enb3521), Dao Tran (dat1614), Quang Tran (qt1284), Quan Do (qdd7858)
 */
public class PantryManager {
    /**
     * Scanner for user input
     */
    private final Scanner s = new Scanner(System.in);

    private final Connection connection;

    private final String currentUser;

    /**
     * Constructor initializes PantryManager
     */
    public PantryManager(Connection connection, String currentUser){
        this.connection = connection;
        this.currentUser = currentUser;
    }

    /**
     * manageIngredient will direct to user to storing or withdrawing an ingredient
     */
    public void manageIngredient() throws Exception{
        int pantryNo = findPantry();
        System.out.println("Would you like to store more ingredients or withdraw ingredients? (store/withdraw)");
        System.out.print("> ");
        String input = s.nextLine();
        while(!input.equalsIgnoreCase("store") && !input.equalsIgnoreCase("withdraw")){
            System.out.println("Please enter store or withdraw.");
            System.out.println("Would you like to store more ingredients or withdraw ingredients? (store/withdraw)");
            System.out.print("> ");
            input = s.nextLine();
        }
        if(input.equalsIgnoreCase("store")){
            addIngredient(pantryNo);
        }
        else{
            removeIngredient(pantryNo);
        }
    }

    /**
     * printPantryContents will print the contents of a users pantry
     * @param pantryNo  The pantry whose contents should be printed
     */
    private void printPantryContents(int pantryNo) throws Exception{
        ResultSet items = getPantryItems(pantryNo);
        while(items.next()){
            int itemNo = items.getInt("itemno");
            String expirationDate = items.getString("expirationdate");
            System.out.print(itemNo + " ");
            System.out.print(getItemName(itemNo) + " ");
            System.out.print(getItemQuantity(itemNo, expirationDate) + " ");
            System.out.println(expirationDate);
        }
    }

    /**
     * getPantryItem will get the items contained in the current pantry
     * @param pantryNo  The pantry whose items should be retrieved
     * @return A result set containing the pantry items
     */
    private ResultSet getPantryItems(int pantryNo) throws Exception{
        ResultSet rs;
        PreparedStatement ps = connection.prepareStatement("select itemno, expirationdate from stores where pantryno = ?");
        ps.setInt(1, pantryNo);
        rs = ps.executeQuery();
        return rs;
    }

    /**
     * getItemName will get the name of the item
     * @param itemNo The number of the item whose name should be found
     * @return String corresponding to item name
     */
    private String getItemName(int itemNo) throws Exception{
        PreparedStatement ps = connection.prepareStatement("select itemname from item where itemno = ?");
        ps.setInt(1, itemNo);
        ResultSet rs = ps.executeQuery();
        rs.next();
        return rs.getString("itemname");
    }

    /**
     * getItemQuantity will get the quantity a user has of an item
     * @param itemNo    The item number of the item whose quantity should be found
     * @param expirationDate The expiration date of the item whose quantity should be found
     * @return Integer corresponding to items quantity
     */
    private int getItemQuantity(int itemNo, String expirationDate) throws Exception{
        PreparedStatement ps = connection.prepareStatement("select quantity from stores where itemno = ? and expirationdate = ?");
        ps.setInt(1, itemNo);
        ps.setString(2, expirationDate);
        ResultSet rs = ps.executeQuery();
        rs.next();
        return rs.getInt("quantity");

    }

    /**
     * addIngredient will retrieve information about an ingredient to add recursively
     * @param pantryNumber  The pantry number corresponding to where the ingredient should be added
     */
    private void addIngredient(int pantryNumber) throws Exception{
        String input = "";
        while(!input.equalsIgnoreCase("no")) {
            int itemNumber;
            System.out.print("Enter the name of the item you would like to add: ");
            String itemName = s.nextLine().toLowerCase();
            System.out.print("Enter the expiration date (yyyy/mm/dd): ");
            String expirationDate = s.nextLine();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();

            itemNumber = checkItemExistence(itemName);
            //addToUserPantry(itemNumber, pantryNumber, dtf.format(now), expirationDate);
            addNewQuantity(pantryNumber, itemNumber, expirationDate, dtf.format(now));

            System.out.println("Item successfully stored!");

            System.out.println("Would you like to store another item? (yes/no)");
            System.out.print("> ");
            input = s.nextLine();
        }
    }

    /**
     * removeIngredient will remove an ingredient recursively from a users pantry
     * @param pantryNumber  The pantry from where the ingredient should be removed
     */
    private void removeIngredient(int pantryNumber) throws Exception{
        System.out.println("Your current storing:");
        printPantryContents(pantryNumber);
        String input = "";
        while(!input.equalsIgnoreCase("no")) {
            System.out.println("Enter the number of the item you would like to remove: ");
            System.out.print("> ");
            int itemNo = s.nextInt();
            s.nextLine();
            System.out.println("Enter the expiration date (yyyy/mm/dd): ");
            System.out.print("> ");
            String expirationDate = s.nextLine();
            System.out.println("How much would you like to remove?");
            System.out.print("> ");
            int quantity = s.nextInt();
            s.nextLine();    // remove the \n

            boolean success = removeFromPantry(itemNo, pantryNumber, expirationDate, quantity);

            if(success) {
                System.out.println("Item successfully updated!");
            }

            System.out.println("Would you like to remove another item? (yes/no)");
            System.out.print("> ");
            input = s.nextLine();
        }
    }

    /**
     * checkItemExistence will get the item number of an item based of its name, or direct
     * the user to create a new item if it does not yet exist in the database
     * @param itemName The name of the item whose item number is going to be found
     * @return int corresponding to the itemName
     */
    public int checkItemExistence(String itemName) throws Exception{
        int itemNumber = 0;
        boolean itemDNE = true;
        PreparedStatement ps;

        ResultSet resultSet = connection.createStatement().executeQuery("select itemname from item");
        while (resultSet.next()) {
            if (resultSet.getString("itemname").equals(itemName)) {
                ps = connection.prepareStatement("select itemno from item where itemname = ?");
                ps.setString(1, itemName);
                resultSet = ps.executeQuery();
                resultSet.next();
                itemNumber = resultSet.getInt("itemno");
                itemDNE = false;
            }
        }
        if (itemDNE) {
            itemNumber = createItem(itemName);
        }

        return itemNumber;
    }

//    /**
//     * addToUserPantry will add an item to the users pantry
//     * @param itemNumber    The item number corresponding to the item to add
//     * @param pantryNumber  The pantry to add to
//     * @param date          The current date
//     * @param expiration    The expiration date of the item
//     */
//    private void addToUserPantry(int itemNumber, int pantryNumber, String date, String expiration) throws Exception{
//        PreparedStatement ps;
//        ps = connection.prepareStatement("insert into item_pantry values(?, ?, ?, ?)");
//        ps.setInt(1, itemNumber);
//        ps.setInt(2, pantryNumber);
//        ps.setString(3, date);
//        ps.setString(4, expiration);
//        ps.executeUpdate();
//    }

    /**
     * removeFromPantry will remove an item from a pantry if a user has enough to remove
     * @param itemNumber    The item number corresponding to the item to add
     * @param pantryNumber  The pantry to add to
     * @param expirationDate    The expiration date of the item to remove
     * @param quantity      The quantity to remove
     * @return boolean corresponding ot a successful removal
     */
    private boolean removeFromPantry(int itemNumber, int pantryNumber, String expirationDate, int quantity )
            throws Exception{
        int currentQuntity = getCurrentQuantity(pantryNumber, itemNumber, expirationDate);
        if(currentQuntity < quantity){
            System.out.println("Cannot remove more than you have!");
            System.out.println("You currently have " + currentQuntity + " and you tried to remove " + quantity);
            return false;
        }
        int newQuantity = currentQuntity - quantity;
        PreparedStatement ps = connection.prepareStatement("update stores set quantity = ? where itemno = ?" +
                "and pantryno = ? and expirationdate = ?");
        ps.setInt(1, newQuantity);
        ps.setInt(2, itemNumber);
        ps.setInt(3, pantryNumber);
        ps.setString(4, expirationDate);
        ps.executeUpdate();

        ps = connection.prepareStatement("delete from stores where quantity = 0");
        ps.executeUpdate();

        return true;
    }

    /**
     * getCurrentQuantity will get the current quantity of an item the user has in their pantry
     * @param pantryNumber the number of a users pantry
     * @param itemNumber   The item number of the item whose quantity should be found
     * @param expirationDate The expiration date of the item to be found
     * @return integer corresponding to the quantity of the item
     */
    private int getCurrentQuantity(int pantryNumber, int itemNumber, String expirationDate) throws Exception{
        int quantity = 0;
        PreparedStatement ps = connection.prepareStatement("select quantity from stores where pantryno = ? and itemno = ? and expirationdate=?");
        ps.setInt(1, pantryNumber);
        ps.setInt(2, itemNumber);
        ps.setString(3, expirationDate);
        ResultSet rs = ps.executeQuery();
        while(rs.next()){
            quantity += rs.getInt("quantity");
        }
        return  quantity;
    }

    /**
     * addNewQuantity will store a new quantity of an item in the users pantry
     * @param pantryNumber  The users pantry number
     * @param itemNumber    The item number of the item to store
     * @param expirationDate    The expiration date of the item to store
     */
    private void addNewQuantity(int pantryNumber, int itemNumber, String expirationDate, String currentDate) throws Exception{
        PreparedStatement ps;
        System.out.print("Enter the quantity: ");
        double quantity = Double.parseDouble(s.nextLine());
        ps = connection.prepareStatement("insert into stores values(?, ?, ?, ?, ?)");
        ps.setInt(1, pantryNumber);
        ps.setInt(2, itemNumber);
        ps.setDouble(3, quantity);
        ps.setString(4,expirationDate );
        ps.setString(5, currentDate);
        ps.executeUpdate();
    }

    /**
     * findPantry will find the pantry number for the current user, or create one
     * if it does not exist
     * @return integer corresponding to the pantry number
     */
    public int findPantry() throws Exception{
        boolean pantryDNE = true;
        int pantryNo = 0;
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select username from pantry");
        PreparedStatement ps;
        while(resultSet.next()){
            if(resultSet.getString("username").equals(currentUser)){
                ps = connection.prepareStatement("select pantryno from pantry where username = ?");
                ps.setString(1, currentUser);
                resultSet = ps.executeQuery();
                resultSet.next();
                pantryNo = resultSet.getInt("pantryno");
                pantryDNE = false;
            }
        }
        if(pantryDNE){
            resultSet = statement.executeQuery("SELECT * FROM pantry order by pantryno desc limit 1");
            resultSet.next();
            pantryNo += resultSet.getInt("pantryno") + 1;
            ps = connection.prepareStatement("insert into pantry values(?, ?)");
            ps.setInt(1, pantryNo);
            ps.setString(2, currentUser);
            ps.executeUpdate();
        }
        return pantryNo;
    }

    /**
     * createItem will create a new item if it does not yet exist in our database
     * @param itemName  The name of the item to create
     * @return integer corresponding to the item number
     */
    private int createItem(String itemName) throws Exception{
        int itemNo = 0;
        System.out.println("This is a new item!");
        System.out.print("Enter the item aisle: ");
        String aisle = s.nextLine().toLowerCase();

        ResultSet resultSet = connection.createStatement().executeQuery("select * from item order by itemno desc limit 1");
        if(resultSet.next()) {
            itemNo += resultSet.getInt("itemno") + 1;
        }else{
            itemNo = 1;
        }
        PreparedStatement ps = connection.prepareStatement("insert into item values(?, ?, ?)");
        ps.setInt(1, itemNo);
        ps.setString(2, itemName);
        ps.setString(3, aisle);
        ps.executeUpdate();

        return itemNo;
    }

}
