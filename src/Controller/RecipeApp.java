package Controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.util.Scanner;


/**
 * File: RecipeApp
 * Description: RecipeApp is the recipe's application
 * that displays the menu options and get input from users.
 * Authors: Eric Barron (enb3521), Dao Tran (dat1614), Quang Tran (qt1284), Quan Do (qdd7858)
 */
public class RecipeApp {

    private final Properties props;
    private final String url;

    /**
     * Constructor initializes RecipeApp
     */
    public RecipeApp(Properties props, String url ){
        this.props = props;
        this.url = url;
    }

    /**
     * Method that gets the input from users and displays the output.
     * @param currentUser the current user's username
     */
    public void run(String currentUser) throws Exception{
        String input;
        Scanner s = new Scanner(System.in);
        Connection connection = DriverManager.getConnection(url, props);
        PantryManager pantryManager = new PantryManager(connection, currentUser);
        RecipeManager recipeManager = new RecipeManager(connection, currentUser, pantryManager);


        boolean loop = true;

        while(loop) {
            System.out.println("What you you like to do? (Type 'help' for option) ");
            System.out.print("> ");
            input = s.nextLine();
            try {
                switch (input.toLowerCase()) {
                    case "create recipe" -> recipeManager.createRecipe();
                    case "edit recipe" -> recipeManager.editRecipe();
                    case "delete recipe" -> recipeManager.deleteRecipe();
                    case "create category" -> recipeManager.createCategory();
                    case "categorize recipe" -> recipeManager.categorize();
                    case "search recipe" -> recipeManager.search();
                    case "sort recipe" -> recipeManager.sort();
                    case "make recipe" -> recipeManager.markRecipe();
                    case "rate recipe" -> recipeManager.rateRecipe();
                    case "manage ingredient" -> pantryManager.manageIngredient();
                    case "recommendation" -> recipeManager.recommend();
                    case "help" -> printHelp();
                    case "exit" -> {
                        connection.close();
                        System.out.println("You are successfully exit. Thank you!");
                        loop = false;
                    }
                    default -> System.out.println("Invalid Command type help for options");
                }
            }
            catch (InterruptedException ie){
                connection.close();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        if (!connection.isClosed())
            connection.close();
    }

    /**
     * Method that prints the menu options.
     */
    private void printHelp(){
        System.out.println("Valid Operations:");
        System.out.println("""
                 Create Recipe
                 Edit Recipe
                 Delete Recipe
                 Create Category
                 Categorize Recipe
                 Search Recipe
                 Sort Recipe
                 Make Recipe
                 Rate recipe
                 Manage Ingredient
                 Recommendation
                 Help
                 Exit
                """);
    }

}
