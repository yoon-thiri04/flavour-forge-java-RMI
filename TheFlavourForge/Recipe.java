import java.io.Serializable;
import java.util.List;

public class Recipe implements Serializable {
    private static final long serialVersionUID = 1L; // Recommended for Serializable classes

    private int id;
    private String title;
    private String imageUrl;
    private String sourceUrl;

    //Fields for detailed view =====
    private String summary;
    private List<String> instructions;
    private int readyInMinutes;
    private int servings;
    private String cuisine;
    private String category;
    private List<String> ingredients;
    private List<String> equipment;
    private String nutritionalInfo;


    // Constructor for search results (basic info)
    public Recipe(int id, String title, String imageUrl) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getImageUrl() { return imageUrl; }
    public String getSummary() { return summary; }
    public List<String> getInstructions() { return instructions; }
    public int getReadyInMinutes() { return readyInMinutes; }
    public int getServings() { return servings; }
    public String getCuisine() { return cuisine; }
    public String getCategory() { return category; }
    public List<String> getIngredients() { return ingredients; }
    public List<String> getEquipment() { return equipment; }
    public String getNutritionalInfo() { return nutritionalInfo; }
    public String getSourceUrl() { return sourceUrl; }

    // ===== MODIFICATION: Added setters to populate details later =====
    public void setSummary(String summary) { this.summary = summary; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public void setInstructions(List<String> instructions) { this.instructions = instructions; }
    public void setReadyInMinutes(int readyInMinutes) { this.readyInMinutes = readyInMinutes; }
    public void setServings(int servings) { this.servings = servings; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }
    public void setCategory(String category) { this.category = category; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }
    public void setEquipment(List<String> equipment) { this.equipment = equipment; }
    public void setNutritionalInfo(String nutritionalInfo) { this.nutritionalInfo = nutritionalInfo; }


    @Override
    public String toString() {
        return String.format("Recipe: %s", title);
    }
}