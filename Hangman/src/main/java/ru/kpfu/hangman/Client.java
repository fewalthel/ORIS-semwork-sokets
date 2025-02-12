package ru.kpfu.hangman;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.Random;

public class Client extends Application {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    private PrintWriter out;
    private BufferedReader in;
    private TextFlow chatArea;
    private TextField inputField;
    private Label attemptsLabel;
    private String username;
    private int attemptsLeft = 6;

    private static final String[] COLORS = {"red", "blue", "green", "purple", "orange"};
    private static String userColor;

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(10);
        chatArea = new TextFlow();
        chatArea.setPrefHeight(250);
        chatArea.setStyle("-fx-background-color: #EEE; -fx-padding: 10px; -fx-border-color: #CCC; -fx-border-width: 1px;");

        ScrollPane scrollPane = new ScrollPane(chatArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(250);

        attemptsLabel = new Label("Оставшиеся попытки: " + attemptsLeft);
        attemptsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        attemptsLabel.setTextFill(Color.DARKRED);

        inputField = new TextField();
        inputField.setPromptText("Введите букву...");

        Button sendButton = new Button("Отправить");
        sendButton.setOnAction(e -> sendGuess());

        HBox inputBox = new HBox(10, inputField, sendButton);
        root.getChildren().addAll(scrollPane, attemptsLabel, inputBox);

        Scene scene = new Scene(root, 400, 350);
        primaryStage.setTitle("Виселица - " + username);
        primaryStage.setScene(scene);
        primaryStage.show();

        connectToServer();
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            userColor = COLORS[new Random().nextInt(COLORS.length)];

            // Запрашиваем имя пользователя
            username = requestUsername();
            out.println(username);

            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        processServerMessage(serverMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String requestUsername() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Вход");
        dialog.setHeaderText("Введите ваше имя:");
        dialog.setContentText("Имя:");

        return dialog.showAndWait().orElse("Игрок");
    }

    private void sendGuess() {
        String guess = inputField.getText().trim();
        if (!guess.isEmpty() && guess.length() == 1) {
            out.println(guess);
            displayMessage(username + " (вы): " + guess, userColor);
            inputField.clear();
        } else {
            displayMessage("❗ Введите одну букву.", "gray");
        }
    }

    private void processServerMessage(String message) {
        Platform.runLater(() -> {
            if (message.startsWith("ATTEMPTS:")) {
                String[] parts = message.split(":");
                if (parts.length == 3 && parts[1].equals(username)) {
                    attemptsLeft = Integer.parseInt(parts[2]);
                    attemptsLabel.setText("Оставшиеся попытки: " + attemptsLeft);
                }
            } else if (message.startsWith("COLOR:")) {
                String[] parts = message.split(":");
                if (parts.length == 3) {
                    Server.setClientColor(parts[1], parts[2]);
                }
            } else {
                String sender = message.split(":")[0];
                String color = Server.getClientColor(sender);
                displayMessage(message, color);
            }
        });
    }

    private void displayMessage(String message, String color) {
        Text text = new Text(message + "\n");
        text.setFill(Color.web(color));
        text.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        Platform.runLater(() -> chatArea.getChildren().add(text));
    }

    public static String getUserColor () {
        return userColor;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
