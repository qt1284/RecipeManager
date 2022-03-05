package Controller;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * File: CookManager
 * Description: CookManager handles the functionality cooking a recipe
 * Authors: Eric Barron (enb3521), Dao Tran (dat1614), Quang Tran (qt1284), Quan Do (qdd7858)
 */

public class CookManager {

    private final Connection connection;

    /**
     * Constructor initializes CookManager
     */
    public CookManager(Connection connection) {
        this.connection = connection;
    }

    /**
     * possibleCook determines if it is possible to make a recipe
     * from the users current ingredients
     * @param recipeId      The id of the recipe to try to cook
     * @param currentUser   The current username of the database user
     * @param pantryNo      The pantry number assigned to the current user
     * @param scale         The amount to which to scale the ingredients
     */
    public void possibleCook(int recipeId, String currentUser, int pantryNo, double scale) throws Exception {
        ResultSet userIngredients = getUserIngredients(pantryNo);
        ResultSet recipeIngredients = getRecipeIngredients(recipeId);
        boolean ingredientFound = false;
        boolean canMake = true;
        while (recipeIngredients.next() && canMake) {
            int recipeIngredientNo = recipeIngredients.getInt("itemno");
            String ingredientName = findIngredient(recipeIngredientNo);
            while (userIngredients.next()) {
                int userIngredientNo = userIngredients.getInt("itemno");
                if (recipeIngredientNo == userIngredientNo) {
                    ingredientFound = true;
                    double recipeQuantity = recipeIngredients.getInt("quantity") * scale;
                    int userQuantity = retrieveQuantity(pantryNo, userIngredientNo);
                    if (userQuantity < recipeQuantity) {
                        System.out.println("Insufficient amount of " + ingredientName);
                        System.out.println("Recipe requires: " + recipeQuantity + " You have: " + userQuantity);
                        canMake = false;
                    }
                    break;
                }
            }
            if (!ingredientFound) {
                System.out.println("Missing ingredient " + ingredientName);
                canMake = false;
                break;
            }
            ingredientFound = false;
        }
        if (canMake) {
            System.out.println("Recipe Successfully made!");
            setAsCooked(recipeId, currentUser);
            consumeIngredients(pantryNo, recipeId, scale);
        }
    }

    /**
     * getRecipeIngredients returns a result set containing all the ingredients
     * that a recipe requires in order to cook
     * @param recipeId  The recipe identification number of the recipe that is trying
     *                  to be cooked
     * @return A result set containing all the ingredients needed for the recipe
     */
    private ResultSet getRecipeIngredients(int recipeId) throws Exception {
        ResultSet rs;
        PreparedStatement ps = connection.prepareStatement("select * from recipe_ingredient where recipeno = ? order by itemno asc");
        ps.setInt(1, recipeId);
        rs = ps.executeQuery();
        return rs;
    }

    /**
     * getUserIngredients will return a result set of the ingredients that a user has
     * @param pantryNo  The users pantry number
     * @return A result set containing the ingredients a user has
     */
    private ResultSet getUserIngredients(int pantryNo) throws Exception {
        ResultSet rs;
        PreparedStatement ps = connection.prepareStatement("select itemno from item_pantry where pantryno = ? order by itemno asc");
        ps.setInt(1, pantryNo);
        rs = ps.executeQuery();
        return rs;
    }

    /**
     * retrieveQuantity will get the quantity for an ingredient a user has stored in their pantry
     * @param pantryNo  The users pantry number
     * @param itemNo    The item whose quantity will be retrieved
     */
    private int retrieveQuantity(int pantryNo, int itemNo) throws Exception {
        int quantity = 0;
        PreparedStatement ps = connection.prepareStatement("select quantity from stores where pantryno = ? and itemno = ?");
        ps.setInt(1, pantryNo);
        ps.setInt(2, itemNo);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            quantity += rs.getInt("quantity");
        }
        return quantity;
    }

    /**
     * findIngredient will determine an ingredients name
     * @param itemNo    The ingredient whose name should be found
     * @return The name of the ingredient
     */
    private String findIngredient(int itemNo) throws Exception {
        String ingredientName;
        PreparedStatement ps = connection.prepareStatement("select itemname from item where itemno = ?");
        ps.setInt(1, itemNo);
        ResultSet rs = ps.executeQuery();
        rs.next();
        ingredientName = rs.getString("itemname");
        return ingredientName;
    }

    /**
     * setAsCooked will mark a recipe as made
     * @param recipeId      The id of the recipe to be made
     * @param currentUser   The username of the user who made the recipe
     */
    private void setAsCooked(int recipeId, String currentUser) throws Exception {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime accessDate = LocalDateTime.now();
        PreparedStatement ps = connection.prepareStatement("update marks set mark=true, cook_date = ?, username = ? where recipeno = ?");
        ps.setString(1, dtf.format(accessDate));
        ps.setString(2, currentUser);
        ps.setInt(3, recipeId);
        ps.executeUpdate();
    }

    /**
     * consumeIngredient will update a users pantry after they successfully  make a recipe
     * @param pantryNo          The pantry to be updated
     * @param recipeId          The id of the recipe that was made
     * @param scale             The scale applied to making the recipe
     */
    private void consumeIngredients(int pantryNo, int recipeId, double scale)
            throws Exception{
        ResultSet userIngredients = getUserIngredients(pantryNo);
        ResultSet recipeIngredients = getRecipeIngredients(recipeId);
        while (recipeIngredients.next() && userIngredients.next()) {
            int userIngredientNo = userIngredients.getInt("itemno");
            int itemNo = recipeIngredients.getInt("itemno");
            double required = recipeIngredients.getInt("quantity") * scale;
            int userQuantity = retrieveQuantity(pantryNo, userIngredientNo);
            int newQuantity = (int) (userQuantity - required);
            updatePantry(itemNo, newQuantity, pantryNo);
        }
    }

    /**
     * updatePantry will update the quantity in a users pantry
     * @param itemNo    The item number to update
     * @param quantity  The to update the item by
     * @param pantryNo  The pantry to update
     */
    private void updatePantry(int itemNo, int quantity, int pantryNo) throws Exception{
        PreparedStatement ps = connection.prepareStatement("" +
                "update stores set quantity = ? where pantryno = ? and itemno = ?");
        ps.setInt(1, quantity);
        ps.setInt(2, pantryNo);
        ps.setInt(3, itemNo);
        ps.executeUpdate();
    }
}
