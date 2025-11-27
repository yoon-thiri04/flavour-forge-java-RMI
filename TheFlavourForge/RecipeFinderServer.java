import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup; // Make sure this import is here

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.net.URI;

public class RecipeFinderServer implements RecipeFinder {

    private static final String API_KEY = "e5cc6dfd1d0f4e989651237a2b2927af"; // Replace with your key

    @Override
    public List<Recipe> findRecipes(Map<String, String> criteria) {
        System.out.println("Received findRecipes request with criteria: " + criteria);
        String urlString = buildApiUrl("https://api.spoonacular.com/recipes/complexSearch", criteria);
        return parseRecipeList(urlString, "results");
    }
    
    @Override
    public Recipe getRecipeDetails(int recipeId) {
        System.out.println("Received getRecipeDetails request for ID: " + recipeId);
        String urlString = String.format(
                "https://api.spoonacular.com/recipes/%d/information?apiKey=%s",
                recipeId, API_KEY
        );

        try {
            String responseBody = makeApiCall(urlString);
            System.out.println("Requesting details for ID: " + recipeId + " URL: " + urlString);
            System.out.println("Similar recipes API Response: " + responseBody);
            JSONObject recipeJson = new JSONObject(responseBody);

            Recipe recipe = new Recipe(
                    recipeJson.getInt("id"),
                    recipeJson.getString("title"),
                    recipeJson.optString("image", "images/placeholder.png")
            );
            
            // FIX: Set the sourceUrl here.
            recipe.setSourceUrl(recipeJson.optString("sourceUrl", "https://spoonacular.com/recipes"));

            // Jsoup (class name) to parse HTML summary
            recipe.setSummary(Jsoup.parse(recipeJson.optString("summary", "No summary available.")).text());

            recipe.setReadyInMinutes(recipeJson.optInt("readyInMinutes", 0));
            recipe.setServings(recipeJson.optInt("servings", 0));

            List<String> instructionsList = new ArrayList<>();
            if (recipeJson.has("analyzedInstructions") && !recipeJson.getJSONArray("analyzedInstructions").isEmpty()) {
                JSONArray steps = recipeJson.getJSONArray("analyzedInstructions").getJSONObject(0).getJSONArray("steps");
                for (int i = 0; i < steps.length(); i++) {
                    instructionsList.add((i + 1) + ". " + steps.getJSONObject(i).getString("step"));
                }
            } else {
                instructionsList.add("No instructions provided.");
            }
            recipe.setInstructions(instructionsList);

            List<String> ingredientsList = new ArrayList<>();
            if (recipeJson.has("extendedIngredients")) {
                JSONArray ingredients = recipeJson.getJSONArray("extendedIngredients");
                for (int i = 0; i < ingredients.length(); i++) {
                    ingredientsList.add("- " + ingredients.getJSONObject(i).getString("original"));
                }
            }
            recipe.setIngredients(ingredientsList);

            return recipe;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Recipe> findSimilarRecipes(int recipeId) {
        System.out.println("Received findSimilarRecipes request for ID: " + recipeId);
        String urlString = String.format("https://api.spoonacular.com/recipes/%d/similar?apiKey=%s&number=4", recipeId, API_KEY);
        return parseRecipeList(urlString, null);
    }

    private List<Recipe> parseRecipeList(String urlString, String resultsKey) {
        List<Recipe> recipes = new ArrayList<>();
        try {
            String responseBody = makeApiCall(urlString);
            JSONArray jsonArray;
            if (resultsKey != null) {
                JSONObject root = new JSONObject(responseBody);
                jsonArray = root.getJSONArray(resultsKey);
            } else {
                jsonArray = new JSONArray(responseBody);
            }

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject recipeJson = jsonArray.getJSONObject(i);

                String imageUrl;
                if (recipeJson.has("image") && !recipeJson.isNull("image")) {
                    imageUrl = recipeJson.getString("image");
                } else if (recipeJson.has("imageType")) {
                    imageUrl = "https://spoonacular.com/recipeImages/" + recipeJson.getInt("id") + "-556x370." + recipeJson.getString("imageType");
                } else {
                    imageUrl = "images/placeholder.png"; // local fallback
                }
                
                // When parsing, only the basic info is needed
                Recipe recipe = new Recipe(
                        recipeJson.getInt("id"),
                        recipeJson.getString("title"),
                        imageUrl
                );
                recipes.add(recipe);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return recipes;
    }


     private String makeApiCall(String urlString) throws Exception {
        URL url = new URI(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            System.err.println("API Error Response Code: " + responseCode + " for URL: " + urlString);
            throw new RuntimeException("HttpResponseCode: " + responseCode);
        }

        try (Scanner scanner = new Scanner(conn.getInputStream())) {
            return scanner.useDelimiter("\\A").next();
        }
    }

    private String buildApiUrl(String baseUrl, Map<String, String> criteria) {
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        urlBuilder.append("?apiKey=").append(API_KEY);

        try {
            for (Map.Entry<String, String> entry : criteria.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    urlBuilder.append("&").append(entry.getKey()).append("=")
                            .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Requesting URL: " + urlBuilder.toString());
        return urlBuilder.toString();
    }

    public static void main(String[] args) {
        try {
            RecipeFinderServer server = new RecipeFinderServer();
            RecipeFinder stub = (RecipeFinder) UnicastRemoteObject.exportObject(server, 0);
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.bind("RecipeFinderService", stub);
            System.out.println("✅ Server is ready.");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}