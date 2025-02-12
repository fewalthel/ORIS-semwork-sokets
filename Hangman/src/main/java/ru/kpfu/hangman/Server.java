package ru.kpfu.hangman;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    public static HashSet<ClientHandler> clients = new HashSet<>();
    private static Set<String> clientNames = new HashSet<>();
    private static List<String> words = Arrays.asList("java", "socket", "programming", "hangman", "computer");
    private static String wordToGuess = getRandomWord();
    private static StringBuilder currentGuess = new StringBuilder("_".repeat(wordToGuess.length()));

    private static Map<String, String> clientColors = new HashMap<>();

    public static synchronized String getClientColor(String username) {
        return clientColors.getOrDefault(username, "black");
    }

    public static synchronized void setClientColor(String username, String color) {
        clientColors.put(username, color);
    }


    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized boolean addClientName(String name) {
        if (clientNames.contains(name)) {
            return false;
        }
        clientNames.add(name);
        return true;
    }

    public static synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
        clientNames.remove(client.getUsername());
        broadcast("Server: " + client.getUsername() + " покинул игру.");
    }

    public static synchronized void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public static synchronized boolean updateGuess(char guess) {
        boolean found = false;
        for (int i = 0; i < wordToGuess.length(); i++) {
            if (wordToGuess.charAt(i) == guess) {
                currentGuess.setCharAt(i, guess);
                found = true;
            }
        }
        if (found) {
            broadcast("Слово сейчас: " + currentGuess);
        }
        return found;
    }

    public static synchronized String getCurrentGuess() {
        return currentGuess.toString();
    }

    private static String getRandomWord() {
        Random random = new Random();
        return words.get(random.nextInt(words.size()));
    }

    public static synchronized void addClient(ClientHandler client) {
        clients.add(client);
        broadcast("Server: Сейчас играют: " + getClientNames());
    }

    private static String getClientNames() {
        return String.join(", ", clientNames);
    }

    public static synchronized boolean isWordGuessed() {
        return wordToGuess.equals(currentGuess.toString());
    }
}
