import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface RecipeFinder extends Remote {
    /**
     * Finds recipes based on a map of search criteria.
     * @param criteria A map containing search parameters like "query", "diet", etc.
     * @return A list of Recipe objects with basic info.
     * @throws RemoteException If a communication-related error occurs.
     */
    List<Recipe> findRecipes(Map<String, String> criteria) throws RemoteException;

    // ===== MODIFICATION: Added new methods for the detail page =====

    /**
     * Fetches detailed information for a specific recipe by its ID.
     * @param recipeId The ID of the recipe to look up.
     * @return A single Recipe object populated with detailed information.
     * @throws RemoteException If a communication-related error occurs.
     */
    Recipe getRecipeDetails(int recipeId) throws RemoteException;

    /**
     * Finds recipes that are similar to a given recipe ID.
     * @param recipeId The ID of the recipe to find similar ones for.
     * @return A list of similar Recipe objects with basic info.
     * @throws RemoteException If a communication-related error occurs.
     */
    List<Recipe> findSimilarRecipes(int recipeId) throws RemoteException;
}