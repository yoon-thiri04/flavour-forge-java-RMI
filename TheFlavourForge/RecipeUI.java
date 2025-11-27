import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeUI extends Application {

    private static final int CARD_WIDTH = 300;
    private static final int CARD_HEIGHT = 350;
    private static final int IMAGE_HEIGHT = 180;

    private final List<Recipe> initialRecipes;
    private RecipeFinder recipeService;
    private GridPane recipeGrid;
    private Label pageTitle;
    private Label pageSubtitle;
    private final List<Recipe> recipes;

    public RecipeUI(List<Recipe> recipes) {
        this.initialRecipes = new ArrayList<>(recipes); // Store a copy
        this.recipes = new ArrayList<>(recipes);
        connectToServer();
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f2eb;");

        // ========== HEADER ==========
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(15, 30, 15, 30));
        topBar.setAlignment(Pos.CENTER_LEFT);

        ImageView logo = new ImageView(new Image("images/logo.png", 40, 40, true, true));
        Label logoText = new Label("Flavor Forge");
        logoText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        HBox logoBox = new HBox(10, logo, logoText);
        logoBox.setAlignment(Pos.CENTER_LEFT);

        logo.setOnMouseClicked(event -> {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            // Assuming RecipeSearchUI has a no-arg constructor
            RecipeSearchUI mainPage = new RecipeSearchUI();
            try {
                mainPage.start(stage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

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
                showError("Invalid Input", "Please enter a recipe title or ingredients.");
                return;
            }
            performSearch(query);
        });

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
        root.setTop(topBar);

        // ========== MAIN CONTENT ==========
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setAlignment(Pos.TOP_CENTER);

        Label categoryLabel = new Label("RECIPES");
        categoryLabel.setStyle("-fx-background-color: #e34d2f; -fx-text-fill: white; "
                + "-fx-padding: 5 10; -fx-background-radius: 10;");

        pageTitle = new Label("YOUR RECIPE RESULTS");
        pageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 28));

        pageSubtitle = new Label("We found " + recipes.size() + " delicious ideas for you.");
        pageSubtitle.setFont(Font.font("Arial", 14));
        pageSubtitle.setTextFill(Color.GRAY);

        mainContent.getChildren().addAll(categoryLabel, pageTitle, pageSubtitle);

        recipeGrid = new GridPane();
        recipeGrid.setHgap(20);
        recipeGrid.setVgap(20);
        recipeGrid.setAlignment(Pos.CENTER);
        mainContent.getChildren().add(recipeGrid);

        // ===== FIX: Removed the redundant loop and just call updateUI =====
        updateUI(this.initialRecipes);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f5f2eb; -fx-border-color: transparent;");

        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setTitle("Flavor Forge - Results");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void performSearch(String query) {
        try {
            if (recipeService == null) {
                showError("Connection Error", "Could not connect to the recipe server.");
                return;
            }
            Map<String, String> criteria = new HashMap<>();
            criteria.put("query", query);
            criteria.put("number", "10"); // You can change the number of results

            List<Recipe> newRecipes = recipeService.findRecipes(criteria);
            updateUI(newRecipes);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Server Error", "An error occurred while searching.");
        }
    }

    private void updateUI(List<Recipe> recipes) {
        // Clear the current recipes and grid
        this.recipes.clear();
        recipeGrid.getChildren().clear();

        if (recipes == null || recipes.isEmpty()) {
            pageTitle.setText("NO RECIPES FOUND");
            pageSubtitle.setText("Try searching for something else.");
        } else {
            this.recipes.addAll(recipes); // Update the main list
            pageTitle.setText("YOUR RECIPE RESULTS");
            pageSubtitle.setText("We found " + this.recipes.size() + " delicious ideas for you.");

            int column = 0;
            int row = 0;
            for (Recipe recipe : this.recipes) {
                VBox card = createRecipeCard(recipe);
                recipeGrid.add(card, column, row);
                column++;
                if (column == 3) {
                    column = 0;
                    row++;
                }
            }
        }
    }

     private VBox createRecipeCard(Recipe recipe) {
        VBox card = new VBox(10);
        card.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        card.setMaxSize(CARD_WIDTH, CARD_HEIGHT);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, gray, 5,0,2,2);");

        ImageView img = new ImageView();
        try {
            img.setImage(new Image(recipe.getImageUrl(), true));
        } catch (Exception e) {
            System.err.println("Could not load image: " + recipe.getImageUrl());
            img.setImage(new Image("images/placeholder.png"));
        }

        img.setFitWidth(CARD_WIDTH - 20);
        img.setFitHeight(IMAGE_HEIGHT);
        img.setPreserveRatio(false);
        img.setSmooth(true);
        img.setClip(new Rectangle(CARD_WIDTH - 20, IMAGE_HEIGHT));

        Label titleLabel = new Label(recipe.getTitle());
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titleLabel.setWrapText(true);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label idLabel = new Label("ID: " + recipe.getId());
        idLabel.setFont(Font.font("Arial", 11));
        idLabel.setTextFill(Color.GRAY);

        Button viewBtn = new Button("VIEW RECIPE");
        viewBtn.setStyle(
                "-fx-background-color: white; -fx-border-color: black; " +
                "-fx-padding: 5 15; -fx-background-radius: 20; -fx-border-radius: 20;"
        );

        viewBtn.setOnAction(event -> {
            try {
                // Manually construct the Spoonacular URL
                String titleSlug = recipe.getTitle().toLowerCase()
                                        .replaceAll("[^a-z0-9\\s-]", "")
                                        .replaceAll("\\s+", "-")
                                        .replaceAll("-+", "-");
                String url = "https://spoonacular.com/" + titleSlug + "-" + recipe.getId();
                
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));

            } catch (Exception e) {
                e.printStackTrace();
                showError("Error", "Could not open recipe page in browser.");
            }
        });

        HBox bottomRow = new HBox();
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        Region rowSpacer = new Region();
        HBox.setHgrow(rowSpacer, Priority.ALWAYS);
        bottomRow.getChildren().addAll(idLabel, rowSpacer, viewBtn);

        card.getChildren().addAll(img, titleLabel, spacer, bottomRow);
        return card;
    }

    private void connectToServer() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            this.recipeService = (RecipeFinder) registry.lookup("RecipeFinderService");
            System.out.println("✅ Results Page UI Connected to RMI Server!");
        } catch (Exception e) {
            System.err.println("Results Page UI Connection error: " + e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}