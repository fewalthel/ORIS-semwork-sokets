package ru.kpfu.hangman;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    public static HashSet<ClientHandler> clients = new HashSet<>();
    private static Set<String> clientNames = new HashSet<>();
    static String filePath = "C:\\Users\\Home\\IdeaProjects\\ORIS-semwork-sokets\\Hangman\\src\\main\\java\\ru\\kpfu\\hangman\\words.txt"; // Путь к вашему текстовому файлу
    static List<String> words = readLinesFromFile(filePath);
    private static String wordToGuess = getRandomWord();
    private static StringBuilder currentGuess = new StringBuilder("_".repeat(wordToGuess.length()));

    private static Map<String, String> clientColors = new HashMap<>();


    private static List<String> readLinesFromFile(String filePath) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line.trim());
            }
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        }
        return lines;
    }

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
