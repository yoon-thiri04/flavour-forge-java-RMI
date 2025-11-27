import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class RecipeSearchUI extends Application {
    private RecipeFinder recipeService;

      

    public void start(Stage primaryStage) {
        // ===== Top bar =====
        connectToServer();
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(15, 30, 15, 30));
        topBar.setAlignment(Pos.CENTER_LEFT);

        // Logo
        ImageView logo = new ImageView(new Image("images/logo.png", 40, 40, true, true));
        Label logoText = new Label("Flavor Forge");
        logoText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        HBox logoBox = new HBox(10, logo, logoText);
        logoBox.setAlignment(Pos.CENTER_LEFT);

        // Search bar
        TextField searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.setPrefWidth(400);
        searchField.setStyle("-fx-background-radius: 20; -fx-border-radius: 20; -fx-padding: 5 10;");
        Button searchBtn = new Button("🔍");
        searchBtn.setStyle("-fx-background-radius: 20; -fx-padding: 5 15;");
        HBox searchBox = new HBox(searchField, searchBtn);
        searchBox.setAlignment(Pos.CENTER);
        searchBox.setSpacing(5);

searchBtn.setOnAction(event -> {
    String query = searchField.getText();
    if (query == null || query.trim().isEmpty()) {
        showError("Invalid Input", "Please enter a recipe title or ingredients in the top search bar.");
        return;
    }

    // 1. Create a simpler criteria map for the top search
    Map<String, String> criteria = new HashMap<>();
    criteria.put("query", query); // Use the versatile 'query' parameter
    criteria.put("number", "10");  // Limit results to a max of 10

    // 2. Call the RMI server 
    try {
        if (recipeService == null) {
            showError("Connection Error", "Could not connect to the recipe server.");
            return;
        }
        
        System.out.println("Sending top-bar search to server: " + criteria);
        List<Recipe> foundRecipes = recipeService.findRecipes(criteria);

        // 3. Open the results window or show a message
        if (foundRecipes != null && !foundRecipes.isEmpty()) {
            RecipeUI recipeResultsPage = new RecipeUI(foundRecipes);
            Stage resultsStage = new Stage();
            recipeResultsPage.start(resultsStage);
            primaryStage.hide();
        } else {
            showError("No Results", "Sorry, no recipes were found for '" + query + "'.");
        }

    } catch (Exception e) {
        e.printStackTrace();
        showError("Server Error", "An error occurred while searching for recipes.");
    }
});

        // Subscribe button
        Button subscribeBtn = new Button("SUBSCRIBE");
        subscribeBtn.setStyle(
                "-fx-background-color: black; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 20;"
        );

        subscribeBtn.setOnAction(e -> {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Subscription");
    alert.setHeaderText(null);
    alert.setContentText("Thanks for subscribing!");
    alert.showAndWait();
});

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topBar.getChildren().addAll(logoBox, spacer, searchBox, subscribeBtn);

        // ===== Main Section =====
        BorderPane mainPane = new BorderPane();
        mainPane.setPadding(new Insets(30));

        // Left: Chef image
        ImageView chefImage = new ImageView(new Image("images/chef.png", 300, 350, true, true));
        VBox leftBox = new VBox(chefImage);
        leftBox.setAlignment(Pos.CENTER);
        mainPane.setLeft(leftBox);

        // Center: Form
        VBox centerBox = new VBox(30);
        centerBox.setAlignment(Pos.TOP_CENTER);

        // === Header slogan ===
        Label sloganOrange = new Label("COOK WHAT YOU LOVE");
        sloganOrange.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        sloganOrange.setStyle("-fx-text-fill: #ff8c00;");

        Label sloganBlack = new Label(", WITH WHAT YOU HAVE");
        sloganBlack.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        sloganBlack.setStyle("-fx-text-fill: black;");

        HBox sloganBox = new HBox(sloganOrange, sloganBlack);
        sloganBox.setAlignment(Pos.CENTER);
        sloganBox.setSpacing(5);

        // === Form fields ===
        TextField ingredientsField = new TextField();
        ingredientsField.setPromptText("Enter your Ingredients...");
        styleInput(ingredientsField);

        ComboBox<String> cuisineBox = new ComboBox<>();
        cuisineBox.setPromptText("Select cuisine");
        cuisineBox.getItems().addAll("Italian", "Chinese", "Indian", "Mexican", "American");
        styleInput(cuisineBox);

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.setPromptText("Select recipe type");
        typeBox.getItems().addAll("Main Course", "Side Dish", "Dessert", "Snack", "Soup");
        styleInput(typeBox);

        ComboBox<String> dietBox = new ComboBox<>();
        dietBox.setPromptText("Select Diet");
        dietBox.getItems().addAll("Vegetarian", "Vegan", "Gluten Free");
        styleInput(dietBox);

        ComboBox<String> sortBox = new ComboBox<>();
        sortBox.setPromptText("Select sorting type");
        sortBox.getItems().addAll("Popularity", "Healthiness", "Calories");
        styleInput(sortBox);

        // === Intolerances grid (unchanged) ===
        GridPane intoleranceGrid = new GridPane();
        intoleranceGrid.setHgap(10);
        intoleranceGrid.setVgap(10);
        intoleranceGrid.setPadding(new Insets(10));
        intoleranceGrid.setStyle("-fx-background-color: white; -fx-border-color: lightgray;");

        String[] intolerances = {
                "Dairy", "Egg", "Gluten", "Peanut",
                "Seafood", "Sesame", "Shellfish", "Soy",
                "Sulfite", "Tree Nut", "Wheat"
        };

        for (int i = 0; i < intolerances.length; i++) {
            int row, col;
            if (i < 4) { row = 0; col = i; }
            else if (i < 8) { row = 1; col = i - 4; }
            else { row = 2; col = i - 8; }
            intoleranceGrid.add(new CheckBox(intolerances[i]), col, row);
        }

        // === Number of Recipes ===
        TextField numRecipesField = new TextField();
        numRecipesField.setPromptText("Enter the number of recipes");
        styleInput(numRecipesField);

        Label note = new Label("Only from 1 to 20 recipes");
        note.setStyle("-fx-text-fill: red; -fx-font-size: 11;");

        VBox recipeBox = new VBox(5, createStyledLabel("NUMBER OF RECIPES"), numRecipesField, note);
        recipeBox.setAlignment(Pos.TOP_LEFT);

        // Validation
        numRecipesField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                numRecipesField.setText(newValue.replaceAll("[^\\d]", ""));
            } else if (!newValue.isEmpty()) {
                try {
                    int value = Integer.parseInt(newValue);
                    if (value < 1) {
                        numRecipesField.setText("1");
                    } else if (value > 20) {
                        numRecipesField.setText("20");
                    }
                } catch (NumberFormatException e) {
                    numRecipesField.setText("1");
                }
            }
        });

        // === Form grid ===
        GridPane formGrid = new GridPane();
        formGrid.setHgap(40);
        formGrid.setVgap(20);
        formGrid.setAlignment(Pos.CENTER);

        formGrid.add(createFieldBox("INGREDIENTS", ingredientsField), 0, 0, 2, 1);
        formGrid.add(createFieldBox("CUISINE", cuisineBox), 0, 1);
        formGrid.add(createFieldBox("TYPE", typeBox), 1, 1);
        formGrid.add(createFieldBox("DIET", dietBox), 0, 2);
        formGrid.add(createFieldBox("INTOLERANCES", intoleranceGrid), 1, 2);
        formGrid.add(createFieldBox("SORTING WITH", sortBox), 0, 3);
        formGrid.add(recipeBox, 1, 3);

        // Search button
        Button searchButton = new Button("Search");
        searchButton.setStyle(
                "-fx-background-color: orange; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 12 0; -fx-background-radius: 10; -fx-font-size: 14;"
        );
        searchButton.setMaxWidth(200);

        VBox buttonBox = new VBox(searchButton);
        buttonBox.setAlignment(Pos.CENTER);

        searchButton.setOnAction(event -> {
            // 1. Gather all inputs from the form
            Map<String, String> criteria = new HashMap<>();
            
            // Ingredients (query parameter for complexSearch is 'query')
            criteria.put("query", ingredientsField.getText());
            
            // Cuisine
            if (cuisineBox.getValue() != null) criteria.put("cuisine", cuisineBox.getValue());
            
            // Diet
            if (dietBox.getValue() != null) criteria.put("diet", dietBox.getValue());
            
            // Type
            if (typeBox.getValue() != null) criteria.put("type", typeBox.getValue());

            // Intolerances
            String intolerancesString = intoleranceGrid.getChildren().stream()
                .filter(node -> node instanceof CheckBox && ((CheckBox) node).isSelected())
                .map(node -> ((CheckBox) node).getText())
                .collect(Collectors.joining(","));
            if (!intolerancesString.isEmpty()) {
                criteria.put("intolerances", intolerancesString);
            }

            // Number of recipes
            criteria.put("number", numRecipesField.getText().isEmpty() ? "10" : numRecipesField.getText());


            // 2. Call the RMI server
            try {
                if (recipeService == null) {
                    showError("Connection Error", "Could not connect to the recipe server.");
                    return;
                }
                
                System.out.println("Sending criteria to server: " + criteria);
                List<Recipe> foundRecipes = recipeService.findRecipes(criteria);

                // 3. Open the results window
                if (foundRecipes != null && !foundRecipes.isEmpty()) {
                    // Create and show the results stage
                    RecipeUI recipeResultsPage = new RecipeUI(foundRecipes);
                    Stage resultsStage = new Stage();
                    recipeResultsPage.start(resultsStage);

                    // Hide the current search stage
                    primaryStage.hide();
                } else {
                    showError("No Results", "Sorry, no recipes were found for your criteria.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                showError("Server Error", "An error occurred while searching for recipes.");
            }
        });

        // Assemble center
        centerBox.getChildren().addAll(sloganBox, formGrid, buttonBox);
        mainPane.setCenter(centerBox);

        // ===== Root =====
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(mainPane);
        root.setStyle("-fx-background-color: #f5f5f5;");

        Scene scene = new Scene(root, 1200, 700);
        primaryStage.setTitle("Cooking Blog CMS");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

     private void connectToServer() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            this.recipeService = (RecipeFinder) registry.lookup("RecipeFinderService");
            System.out.println("✅ UI Connected to RMI Server!");
        } catch (Exception e) {
            System.err.println("UI Connection error: " + e.getMessage());
            // An error alert will be shown if the user tries to search
        }
    }

    // ===== NEW: Helper to show error dialogs =====
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Helpers
    private static VBox createFieldBox(String labelText, Control field) {
        return new VBox(5, createStyledLabel(labelText), field);
    }

    private static VBox createFieldBox(String labelText, GridPane field) {
        return new VBox(5, createStyledLabel(labelText), field);
    }

    private static Label createStyledLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 11; -fx-text-fill: gray;");
        return label;
    }

    private static void styleInput(Control control) {
        control.setStyle(
                "-fx-background-radius: 10; -fx-border-radius: 10; " +
                "-fx-border-color: lightgray; -fx-padding: 8 12; -fx-background-color: white;"
        );
        control.setMaxWidth(Double.MAX_VALUE);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
