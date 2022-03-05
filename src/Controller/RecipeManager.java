package Controller;


import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * File: RecipeManager
 * Description: RecipeManager handles the functionality for a recipe
 * such as creating a recipe, editing recipe, categorizing recipe,...
 * Authors: Eric Barron (enb3521), Dao Tran (dat1614), Quang Tran (qt1284), Quan Do (qdd7858)
 */
public class RecipeManager {

    /**
     * Scanner for getting input
     */
    private final Scanner s = new Scanner(System.in);
    private final Connection connection;
    private final PantryManager pantryManager;
    private final CookManager cookManager;
    private final String currentUser;

    /**
     * Constructor initializes RecipeManager.
     */
    public RecipeManager(Connection connection, String currentUser,
                         PantryManager pantryManager){
        this.connection = connection;
        this.pantryManager = pantryManager;
        this.cookManager = new CookManager(connection);
        this.currentUser = currentUser;
    }

    /**
     * Method that creates a recipe.
     */
    public void createRecipe(){
        System.out.print("Enter recipe name: ");
        String name = s.nextLine();
        System.out.print("Enter recipe description: ");
        String description = s.nextLine();
        System.out.print("Enter recipe difficulty (1-Novice to 5-Expert): ");
        int difficulty = Integer.parseInt(s.nextLine());
        System.out.print("Enter recipe cook time in minutes: ");
        int cookTime = Integer.parseInt(s.nextLine());
        System.out.print("Enter recipe servings: ");
        int servings = Integer.parseInt(s.nextLine());

        System.out.print("Enter recipe steps, type 'Done' when you are finished:\n> ");
        String step = s.nextLine();
        List<String> recipeSteps = new ArrayList<>();
        while(!step.equalsIgnoreCase("done")){
            System.out.print("> ");
            recipeSteps.add(step);
            step = s.nextLine();
        }
        try{

            int recipeNo = recipeBasics(difficulty, servings, cookTime, name, description);
            recipeIngredients(recipeNo);
            recipeSteps(recipeNo, recipeSteps);
            createdBy(recipeNo);
            setAsUncooked(recipeNo);

        }catch(Exception e){
            e.printStackTrace();
        }
        System.out.println("Recipe Created!");
    }

    /**
     * Method that inserts recipe's info to the database.
     * @param difficulty The difficulty of recipe
     * @param servings The servings of recipe
     * @param cookTime The cooking time of recipe
     * @param name The name of recipe
     * @param description The short description of recipe
     * @return The recipe number which is a key number
     */
    private int recipeBasics(int difficulty, int servings, int cookTime,
                             String name, String description) throws  Exception {
        int recipeNo;
        PreparedStatement ps = connection.prepareStatement("insert into recipe values(?, ?, ?, ?, ?, ?, ?)");
        ResultSet rs = connection.createStatement()
                .executeQuery("SELECT * FROM recipe order by recipeno desc limit 1");
        rs.next();
        recipeNo = rs.getInt("recipeno") + 1;
        ps.setInt(1, recipeNo);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime accessDate = LocalDateTime.now();
        ps.setString(2, dtf.format(accessDate));
        ps.setInt(3, difficulty);
        ps.setInt(4, servings);
        ps.setInt(5, cookTime);
        ps.setString(6, name.toLowerCase());
        ps.setString(7, description.toLowerCase());
        ps.executeUpdate();
        return recipeNo;
    }

    /**
     * Method that getting the input of ingredients of recipe.
     * @param recipeNo the recipe number that the ingredients insert to
     */
    private void recipeIngredients(int recipeNo) throws Exception{

        String name = "";
        int quantity;
        int itemNo;
        String[] input ;
        while(!name.equalsIgnoreCase("done")) {
            System.out.print("Enter the recipe ingredient followed by " +
                    "the quantity (i.e. Onion 1), type 'Done' when you are finished:\n> ");
            input = s.nextLine().split(" ");
            if(input[0].equalsIgnoreCase("done")){
                break;
            }
            else if(input.length != 2){
                System.out.println("Invalid input, please try again.");
                continue;
            }
            name = input[0].toLowerCase();
            quantity = Integer.parseInt(input[1]);
            itemNo = pantryManager.checkItemExistence(name);
            addItem(itemNo, recipeNo, quantity);
        }
    }

    private void createdBy(int recipeNo) throws Exception{
        PreparedStatement ps = connection.prepareStatement("insert into creates values(?, ?, ?)");
        ps.setString(1, currentUser);
        ps.setInt(2, recipeNo);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime accessDate = LocalDateTime.now();
        ps.setString(3, dtf.format(accessDate));
        ps.executeUpdate();
    }

    /**
     * Method that inserts the ingredients to the table in database.
     * @param itemNo The ingredient number
     * @param recipeNo The recipe number
     * @param quantity The quantity of ingredient
     */
    private void addItem(int itemNo, int recipeNo, int quantity) throws Exception{
        PreparedStatement ps = connection.prepareStatement("insert into recipe_ingredient values(?, ?, ?)");
        ps.setInt(1, itemNo);
        ps.setInt(2, recipeNo);
        ps.setInt(3, quantity);
        ps.executeUpdate();
    }

    /**
     * Method that insert the recipe's steps into the table in database.
     * @param recipeNo The recipe number
     * @param recipeSteps The recipe's step
     */
    private void recipeSteps(int recipeNo, List<String> recipeSteps ) throws Exception{
        PreparedStatement ps ;
        ps = connection.prepareStatement("insert into recipe_step values(?, ?)");
        ps.setInt(1, recipeNo);
        StringBuilder sb = new StringBuilder();
        String steps;
        while (!recipeSteps.isEmpty()){
            sb.append(recipeSteps.remove(0)).append("\n");
        }
        steps = sb.toString();
        ps.setString(2, steps);
        ps.executeUpdate();
    }
    public void recommend()throws Exception{
        System.out.print("Top rate/Most recent/Producible Recipes/Similar Tastes \n> ");
        String option = s.nextLine();
        if (option.equalsIgnoreCase("top rate")){
            printTopRate();
        } else if (option.equalsIgnoreCase("most recent")){
            printMostRecent();
        } else if(option.equalsIgnoreCase("producible recipes")){
            printProducible();
        } else if(option.equalsIgnoreCase("similar tastes")){
            printSimilar();
        } else {
            System.out.println("That option is not available. Exit to menu...");
        }
    }

    private void printTopRate() throws Exception{
        String sql = "select * from recipe ORDER BY rate_avg DESC LIMIT 50";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        int count = 0;
        while (rs.next()) {
            if (count == 0) {
                System.out.println("Here is the result:");
                for (int i = 1; i <= columnsNumber; i++) {
                    System.out.print(centerString(30,rsmd.getColumnName(i)));
                }
                System.out.println();
            }
            count++;
            for (int i = 1; i <= columnsNumber; i++) {
                String columnValue = rs.getString(i);
                System.out.print(centerString(30,columnValue));
            }
            System.out.println();

        }
    }

    private void printMostRecent() throws Exception{
        String sql = "select * from recipe where recipeno in (" +
                            " select recipeno from creates ORDER BY creation_date DESC LIMIT 50)";

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        int count = 0;
        while (rs.next()) {
            if (count == 0) {
                System.out.println("Here is the result:");
                for (int i = 1; i <= columnsNumber; i++) {
                    System.out.print(centerString(30,rsmd.getColumnName(i)));
                }
                System.out.println();
            }
            count++;
            for (int i = 1; i <= columnsNumber; i++) {
                String columnValue = rs.getString(i);
                System.out.print(centerString(30,columnValue));
            }
            System.out.println();

        }
    }

    private void printProducible() throws Exception{
        int pantryNo = pantryManager.findPantry();
        PreparedStatement ps = connection.prepareStatement("select * from recipe where" +
                "recipeno in (select recipeno from recipe_ingredient " +
                "where itemno in (select itemno from stores where pantryno = ? " +
                "and stores.quantity >= recipe_ingredient.quantity)) order by rate_avg DESC");
        ps.setInt(1, pantryNo);
        ResultSet rs = ps.executeQuery();
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        int count = 0;
        while (rs.next()) {
            if (count == 0) {
                System.out.println("Here is the result:");
                for (int i = 1; i <= columnsNumber; i++) {
                    System.out.print(centerString(30,rsmd.getColumnName(i)));
                }
                System.out.println();
            }
            count++;
            for (int i = 1; i <= columnsNumber; i++) {
                String columnValue = rs.getString(i);
                System.out.print(centerString(30,columnValue));
            }
            System.out.println();
        }
    }

    private void printSimilar() throws Exception{
        String t = "select recipeno from marks where mark = true and username in (" +
                "select username from marks where mark = true and recipeno in (select" +
                "recipeno from marks where username = ? and mark = true)";
        PreparedStatement ps = connection.prepareStatement(t);
        ps.setString(1, currentUser);
        ResultSet rs = ps.executeQuery();
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        int count = 0;
        while (rs.next()) {
            if (count == 0) {
                System.out.println("Here is the result:");
                for (int i = 1; i <= columnsNumber; i++) {
                    System.out.print(centerString(30,rsmd.getColumnName(i)));
                }
                System.out.println();
            }
            count++;
            for (int i = 1; i <= columnsNumber; i++) {
                String columnValue = rs.getString(i);
                System.out.print(centerString(30,columnValue));
            }
            System.out.println();
        }
    }

    /**
     * Method that edits the recipe.
     */
    public void editRecipe() throws Exception{
        System.out.println("Here is your recipe list: ");
        String sql = "SELECT *" +
                " FROM recipe WHERE recipeno in (select recipeno from creates where username = '" + currentUser + "') ";
        printList(sql);
        int recipeID = selectingIDFromRecipe("edit", true);
        if (recipeID == -1){
            System.out.println("You choose to exit");
            return;
        }
        boolean finish = false;
        while (!finish) {
            System.out.print("Which part do you want to edit? (Enter a column name)\n> ");
            DatabaseMetaData md = connection.getMetaData();
            String part = s.nextLine();
            ResultSet rs = md.getColumns(null, null, "recipe", part);
            if (rs.next()) {
                //Column in table exist
                System.out.print("What do you want to change " + part + " into:\n> ");
                String change = s.nextLine();
                if (part.equals("name") || (part.equals("description")))
                    sql = "update recipe set " + part + "= '" + change + "' WHERE recipeno = " + recipeID;
                else
                    sql = "update recipe set " + part + "= " + change + " WHERE recipeno = " + recipeID;
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.executeUpdate();
                System.out.println("You successfully edited the recipe.");
                finish = true;
            } else {
                System.out.print("Sorry, that recipe part does not exist. Please try again.\n> ");
            }
        }
    }

    /**
     * Method that creates category for a recipe.
     */
    public void createCategory() throws Exception{
        int categoryNo;
        System.out.print("Enter the name of category that you want to create (Enter 'exit' if you finish):\n> ");
        String category = s.nextLine();
        ResultSet rs;
        while (!category.equals("exit")){
            if (connection.createStatement().executeQuery("SELECT * FROM category WHERE categoryname = '"
                    + category +"'").next()){
                System.out.print("This category is already exist. Please try another one\n> ");
            }
            else{
                rs = connection.createStatement().executeQuery("SELECT max(categoryno) as max FROM category");
                rs.next();
                categoryNo = rs.getInt("max") + 1;
                PreparedStatement ps = connection.prepareStatement("insert into category values(?, ?)");
                ps.setInt(1, categoryNo);
                ps.setString(2, category);
                ps.executeUpdate();
                System.out.println(category + " has been added as a category.");
            }
            System.out.print("Enter the name of category that you want to create (Enter 'exit' if you finish):\n> ");
            category = s.nextLine();
        }
    }

    /**
     * Method that categorizes a recipe.
     */
    public void categorize() throws Exception{
        Statement st = connection.createStatement();
        System.out.println("Here is your recipe list");
        String sql = "SELECT *" +
                "  FROM recipe WHERE recipeno in (select recipeno from creates where username = '" + currentUser + "' )";
        printList(sql);
        int selectID = selectingIDFromRecipe("categorize", true);
        if (selectID == -1){
            System.out.println("You choose to exit");
            return;
        }
        System.out.print("Which category does this recipe belongs to?\n> ");
        String category = s.nextLine();
        while(!isStringInTable("category","categoryname",category)){
            System.out.print("This category is not in the table. Do you want to create a new category? (yes/no)\n> ");
            String option = s.nextLine();
            if (option.equalsIgnoreCase("yes"))
                createCategory();
             else {
                 System.out.print("Which category does this recipe belongs to?\n> ");
                 category = s.nextLine();
            }
        }
        ResultSet rs = st.executeQuery("SELECT categoryno FROM category WHERE categoryname = '" + category + "'");
        rs.next();
        int categoryNo = rs.getInt("categoryno");
        rs = connection.createStatement().executeQuery("SELECT * FROM belongs_to WHERE "
                                    + "recipeno = " + selectID  + " AND categoryno = " + categoryNo);

        if (rs.next()){
            System.out.println("Sorry this recipe has this category already. Exit to menu...");
            return;
        }
        PreparedStatement ps = connection.prepareStatement("insert into belongs_to values(?, ?)");
        ps.setInt(1, selectID);
        ps.setInt(2, categoryNo);
        ps.executeUpdate();
        System.out.println("You successfully categorized the recipe.");
    }

    /**
     * Method that show the recipe list to choose a recipe to delete.
     */
    public void deleteRecipe() throws Exception{
        System.out.println("Here is your recipe list");
        String sql = "SELECT * FROM recipe WHERE recipeno in (select recipeno from creates where username = '" + currentUser + "') ";
        printList(sql);
        delete();

    }

    /**
     * Method that deletes a recipe from the table in database.
     */
    public void delete() throws Exception{
        Statement st = connection.createStatement();
        int selectID = selectingIDFromRecipe("delete", true);
        if (selectID == -1){
            System.out.println("You choose to exit");
            return;
        }
        String sql = "SELECT mark FROM marks WHERE recipeno = " + selectID;
        ResultSet rs = st.executeQuery(sql);
        if(rs.next()) {
            if (rs.getBoolean("mark")) {
                System.out.println("This recipe has been made, you can't delete it. Exit to menu...");
                return;
            }
        }
        printList("SELECT * FROM recipe WHERE recipeno = " + selectID);
        System.out.print("Do you want to delete this item? ('yes' or 'no')\n> ");
        String confirm = s.nextLine();

        if (confirm.equals("yes")) {
            System.out.println("You successfully deleted your recipe.");
            PreparedStatement ps = connection.prepareStatement("delete FROM recipe WHERE recipeno = " + selectID);
            ps.executeUpdate();
        } else {
            System.out.println("You choose to not delete. Go back to menu.");
        }
    }

    /**
     * Method to print out item list based on sql
     * @param sql the passed in sql
     */
    public void printList(String sql) throws Exception{
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        int count = 0;
        String input;
        boolean empty = true;
        while (rs.next()) {
            if (count == 0) {
                System.out.println("Here is the result:");
                for (int i = 1; i <= columnsNumber; i++) {
                    System.out.print(centerString(30,rsmd.getColumnName(i)));
                }
                System.out.println();
            }
            count++;

            for (int i = 1; i <= columnsNumber; i++) {
                String columnValue = rs.getString(i);
                System.out.print(centerString(30,columnValue));
            }
            System.out.println();
            if (count == 20){
                System.out.print("Enter yes to see the next 20 results. Enter anything else will skip.\n> ");
                input = s.nextLine();
                if (!input.equalsIgnoreCase("yes")){
                    break;
                }
                for (int i = 1; i <= columnsNumber; i++) {
                    System.out.print(centerString(30,rsmd.getColumnName(i)));
                }
                System.out.println();
                count = 0;
                empty = false;
            }

        }
        if (count == 0 && empty){
            System.out.println("The result is empty. There is no recipe with that criteria yet.");
        }
    }

    /**
     * Method that margins the output string.
     * @param width The width of string
     * @param s The string need to be margined
     * @return The string after formatting
     */
    public static String centerString (int width, String s) {
        return String.format("%-" + width  + "s|",
                String.format("%" + (s.length() + (width - s.length()) / 2) + "s", s));
    }


    /**
     * Method that asks for the type of searching recipe.
     */
    public void search() throws Exception{
        String searchType;
        String searchTerm;
        boolean loop = true;
        while(loop) {
            System.out.print("Enter search type: (Enter 'ingredient', 'name' , 'category', or 'back' to the menu)\n> ");
            searchType = s.nextLine();
            switch (searchType.toLowerCase()) {
                case "ingredient" -> {
                    System.out.print("Enter ingredient to search by:\n> ");
                    searchTerm = s.nextLine();
                    searchByIngredient(searchTerm);
                }
                case "name" -> {
                    System.out.print("Enter recipe name to search for:\n> ");
                    searchTerm = s.nextLine();
                    searchByName(searchTerm);
                }
                case "category" -> {
                    System.out.print("Enter category to search by:\n> ");
                    searchTerm = s.nextLine();
                    searchByCategory(searchTerm);
                }
                case "back" -> loop = false;
                default -> System.out.print("Invalid search type must search by" +
                        " 'ingredient', 'name' , or 'category'. Type 'back' to choose a different" +
                        " operation.\n");
            }
        }
    }

    /**
     * Method that searches the recipe by category.
     * @param category The recipe's category
     */
    private void searchByCategory(String category) throws Exception{
        printList("SELECT * FROM recipe WHERE recipeno IN " +
                "(SELECT recipeno FROM belongs_to WHERE categoryno IN " +
                "(SELECT categoryno FROM category WHERE categoryname = '"+category+"')) ORDER BY name ASC");
    }

    /**
     * Method that searches recipe by name.
     * @param recipeName The recipe's name
     */
    private void searchByName(String recipeName) throws Exception{
        printList("SELECT * FROM recipe WHERE name like '%"+recipeName+"%' ORDER BY name ASC");
    }

    /**
     * Method that asks for the type of sorting recipe.
     */
    public void sort() throws Exception{
        String sortType;
        String sortDirection;
        boolean loop = true;
        while(loop) {
            System.out.print("Enter sort type: (Enter 'name', 'rating', 'most recent' or 'back' to the menu)\n> ");
            sortType = s.nextLine();

            switch (sortType.toLowerCase()) {
                case "name" -> {
                    System.out.print("Enter sort direction (asc/desc):\n> ");
                    sortDirection = s.nextLine();
                    sortByName(sortDirection);
                }
                case "rating" -> {
                    System.out.print("Enter sort direction (asc/desc):\n> ");
                    sortDirection = s.nextLine();
                    sortByRating(sortDirection);
                }
                case "most recent" -> {
                    sortByRecent();
                }
                case "back" -> loop = false;
                default -> System.out.println("Invalid search type must search by " +
                        "'name', 'rating' or 'most recent'. Type 'back' to chose a different" +
                        " operation.");
            }
        }
    }

    /**
     * Method that sorts recipe by name.
     * @param sortDirection The sort direction
     */
    private void sortByName(String sortDirection) throws Exception{
        printList("SELECT * FROM recipe order by name " + sortDirection);
    }

    /**
     * Method that sorts the recipe by rating.
     * @param sortDirection The sort direction
     */
    private void sortByRating(String sortDirection) throws Exception{
        printList("SELECT * FROM recipe WHERE recipeno IN ("+
                "SELECT recipeno FROM rates order by star_rate " + sortDirection + ")");
    }

    /**
     * Method that sorts the recipe by the most recent.
     */
    private void sortByRecent() throws Exception{
        printList("SELECT * FROM recipe order by accessdate desc");
    }

    /**
     * Method that marks recipe as cooked.
     */
    public void markRecipe() throws Exception{
        printList("SELECT * FROM recipe");
        int recipeId = selectingIDFromRecipe("mark", false);
        if (recipeId == -1){
            System.out.println("You chose to exit.");
            return;
        }
        double scale = 1;
        System.out.print("Would you like to scale the recipe? (yes/no)\n> ");
        String input = s.nextLine();
        while(!input.equalsIgnoreCase("yes") && !input.equalsIgnoreCase("no")){
            System.out.println("Please enter yes or no.");
            System.out.print("Would you like to scale the recipe? (yes/no)\n> ");
            input = s.nextLine();
        }
        if(input.equalsIgnoreCase("yes")){
            System.out.print("Enter the scale factor:\n> ");
            scale = s.nextDouble();
            s.nextLine();
        }
        int pantryNo = pantryManager.findPantry();
        cookManager.possibleCook(recipeId, currentUser, pantryNo, scale);
    }

    /**
     * Method that sets recipe as uncooked.
     * @param recipeId The recipe number
     */
    private void setAsUncooked(int recipeId) throws Exception{
        PreparedStatement ps = connection.prepareStatement("insert into marks values(?, ?, ?)");
        ps.setString(1, currentUser);
        ps.setInt(2, recipeId );
        ps.setBoolean(3, false);
        ps.executeUpdate();
    }

    /**
     * Method that searches recipe by ingredient.
     * @param ingredient The recipe's ingredient
     */
    private void searchByIngredient(String ingredient) throws Exception{
        String sql = "SELECT * FROM recipe WHERE recipeno IN (" +
                "SELECT recipeno FROM recipe_ingredient WHERE itemno IN (" +
                "SELECT itemno FROM item WHERE itemname = '"+ ingredient + "'))";
        System.out.println("This is a list of every recipe using " + ingredient);
        printList(sql);

    }

    /**
     * Method that rates a recipe.
     */
    public void rateRecipe() throws Exception{
        System.out.println("Here is the list of all recipe: ");
        printList("SELECT * FROM recipe");
        System.out.print("Enter the id of recipe you want to rate:\n> ");
        int id = selectingIDFromRecipe("rate", false);
        if (id == -1){
            System.out.println("You chose to exit.");
            return;
        }
        if (isUserAlreadyRated(currentUser,id)) {
            System.out.println("You have already rated this recipe. Exit to menu...");
            return;
        }
        System.out.print("Please rate FROM 1 to 5 the recipe(Enter -1 to exit)\n> ");
        int rate = s.nextInt();
        while (rate <1 || rate > 5){
            System.out.println("You should enter a a number between 1 and 5");
            System.out.print("Please rate FROM 1 to 5 the recipe(Enter -1 to exit)\n> ");
            rate = s.nextInt();
        }
        s.nextLine();    // remove the \n
        PreparedStatement ps;
        ps = connection.prepareStatement("insert into rates values(?, ?, ?)");
        ps.setString(1, currentUser);
        ps.setInt(2, id);
        ps.setInt(3, rate);
        ps.executeUpdate();
        double avg = findAverage(id,rate);
        ps = connection.prepareStatement("update recipe set rate_avg = " + avg + " where recipeno = "+ id);
        ps.executeUpdate();
    }
    public double findAverage(int id, int rate) throws Exception{
        ResultSet rs = connection.createStatement().executeQuery("select count(recipeno) as id from rates where recipeno = " + id);
        rs.next();
        int prevTotal = rs.getInt("id") - 1;
        rs.close();
        rs = connection.createStatement().executeQuery("select rate_avg from recipe where recipeno = " + id);
        rs.next();
        double average = rs.getInt("rate_avg");
        return (average * prevTotal + rate) / (prevTotal+1);

    }
    /**
     * Method that checks if the user already rated that recipe.
     * @param username The user's username
     * @param recipeno The recipe's number
     * @return true if the user rated that recipe; otherwise, false
     */
    public boolean isUserAlreadyRated(String username, int recipeno) throws Exception{
        ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM rates WHERE username = '"
                + username + "' AND recipeno = " + recipeno);

        return rs.next();
    }

    /**
     * Method that selects the recipe's numbers that the user has authority on.
     * @param option The recipe's number
     * @param needAuthorize The user's authorize
     * @return the recipe's number that user has authority on
     */
    public int selectingIDFromRecipe(String option, boolean needAuthorize) throws Exception{
        int selectID;
        PreparedStatement sql;
        if (needAuthorize){
            sql = connection.prepareStatement("SELECT recipeno FROM creates WHERE recipeno = ? AND username = ?");
//            sql = connection.prepareStatement("SELECT recipeno from recipe WHERE recipeno IN (SELECT" +
//                    "recipeno FROM creates WHERE recipeno = ? AND username = ?");
            sql.setString(2, currentUser);
        }
        else {
            sql = connection.prepareStatement("SELECT recipeno FROM recipe WHERE recipeno = ?");
        }
        System.out.print("Enter the recipe number that you want to " + option + " (Enter -1 to finish):\n> ");
        selectID = s.nextInt();
        sql.setInt(1, selectID);
        ResultSet rs = sql.executeQuery();
        while (!rs.next() && selectID != -1) {
            System.out.println("The id you entered does not exist or you are not the creator. Please try again.");
            System.out.print("Enter the recipe number that you want to " + option + " (Enter -1 to finish):\n> ");
            selectID = s.nextInt();
            sql.setInt(1, selectID);
            rs = sql.executeQuery();
        }
        s.nextLine();    // remove the \n
        return selectID;
    }

    /**
     * Method that check if a string in a given table in database.
     * @param table The table in database
     * @param data The condition
     * @param value The value
     * @return true if a string exists in table; otherwise, false
     */
    public boolean isStringInTable(String table,String data,String value) throws Exception{
        ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM " + table + " WHERE "
                + data + " = '" + value + "'");
        return rs.next();
    }

}
