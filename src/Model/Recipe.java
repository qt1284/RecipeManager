package Model;

import java.util.List;

/**
 * File: Recipe
 * Description: Recipe class holds the information of a recipe.
 * Authors: Eric Barron (enb3521), Dao Tran (dat1614), Quang Tran (qt1284), Quan Do (qdd7858)
 */
public class Recipe {

    private String name;
    private String description;
    private String difficulty;
    private String cookTime;
    private int serving;
    private List<String> steps;


    public Recipe(String name, String description, String difficulty, String cookTime, int serving, List<String> steps){
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.cookTime = cookTime;
        this.serving = serving;
        this.steps = steps;
    }

    /**
     * Method that displays the recipe's information as a string.
     * @return the string of recipe's information
     */
    @Override
    public String toString(){
        return "Your created recipe's name is: " + name + "\n"
                + "Description: " + description + "\n"
                + "Difficulty: " + difficulty + "\n"
                + "Cook time: " + cookTime + "\n"
                + "Serving: " + serving + "\n"
                + "Steps to make recipe : \n" +  getStep();

    }

    /**
     * Method that gets the steps of recipe.
     * @return the string of recipe's steps
     */
    public String getStep(){
        StringBuilder str = new StringBuilder();
        int count = 0;
        for(String step: steps){
            String add = "Step " + count + ": " + step + "\n";
            str.append(add);
            count++;
        }
        return str.toString();
    }

}
