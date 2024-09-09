import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.JSONObject;  // Import the JSON library
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ChatBotUI extends Application {

    private TextArea conversationArea;
    private TextField userInputField;
    private Button sendButton;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Setup the layout
        primaryStage.setTitle("Java Programming ChatBot");

        // Conversation history area
        conversationArea = new TextArea();
        conversationArea.setEditable(false);  // Make it read-only
        conversationArea.setWrapText(true);   // Wrap text in the area

        // Input field for user's message
        userInputField = new TextField();
        userInputField.setPromptText("Ask me something about Java...");

        // Send button
        sendButton = new Button("Send");
        sendButton.setOnAction(e -> handleUserInput());  // Event handler when send button is clicked

        // Layout
        VBox layout = new VBox(10);  // Vertical box with spacing of 10
        layout.getChildren().addAll(conversationArea, userInputField, sendButton);

        Scene scene = new Scene(layout, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // This method handles sending user input and displaying bot response
    private void handleUserInput() {
        String userInput = userInputField.getText();
        if (userInput.trim().isEmpty()) {
            return;  // Do nothing if input is empty
        }

        // Display user's message
        conversationArea.appendText("You: " + userInput + "\n");

        // Clear the input field
        userInputField.clear();

        // Get the bot's response (using the same backend scraping mechanism)
        String botResponse = getWebResponse(userInput);

        // Display the bot's parsed response (title and answer)
        conversationArea.appendText("Bot: " + botResponse + "\n");
    }

    // This method sends the user's input to the backend (Python Flask server) and gets the response
    private String getWebResponse(String userInput) {
        try {
            // URL encode the user input to handle spaces, special characters, etc.
            String encodedQuery = URLEncoder.encode(userInput, StandardCharsets.UTF_8.toString());

            // Backend URL (this should point to your Python server or any other scraping server)
            URL url = new URL("http://localhost:5000/scrape?query=" + encodedQuery);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Reading the response
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parse the JSON response
            JSONObject jsonResponse = new JSONObject(response.toString());
            String title = jsonResponse.optString("title", "No title found");
            String answer = jsonResponse.optString("answer", "No answer found");

            // Return the parsed response
            return "Question: " + title + "\nAnswer: " + answer;

        } catch (Exception e) {
            e.printStackTrace();
            return "Sorry, I couldn't retrieve the data.";
        }
    }
}
