package ru.kpfu.hangman;


import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private int attemptsLeft = 6;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);


            while (true) {
                out.println("Введите ваше имя: ");
                username = in.readLine();
                if (username == null || username.isEmpty() || !Server.addClientName(username)) {
                    out.println("Это имя уже занято. Попробуйте другое.");
                } else {
                    break;
                }
            }

            Server.addClient(this);
            out.println("Добро пожаловать, " + username + "! Слово для отгадывания: " + Server.getCurrentGuess());


            // Оповещаем всех клиентов
            Server.broadcast("Server: " + username + " подключился!");

            String input;
            while ((input = in.readLine()) != null) {
                if (input.length() == 1) {
                    char guess = input.charAt(0);
                    Server.broadcast(username + " отгадывает букву: " + guess); //

                    if (Server.updateGuess(guess)) {
                        Server.broadcast("Server: " + username + " угадал букву: " + guess);
                        if (Server.isWordGuessed()) {
                            Server.broadcast("Слово отгадано! Игра окончена.");
                            break;
                        }
                    } else {
                        attemptsLeft--;
                        Server.broadcast("ATTEMPTS:" + username + ":" + attemptsLeft);
                        Server.broadcast(username + " ошибся! Осталось " + attemptsLeft + " попыток.");

                        if (attemptsLeft == 0) {
                            out.println("У вас закончились попытки. Вы проиграли.");
                            break;
                        }
                    }
                } else {
                    out.println("❗ Введите одну букву.");
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка связи с клиентом " + username);
        } finally {
            disconnect();
        }
    }

    private void disconnect() {
        try {
            if (username != null) {
                Server.clients.remove(username);
                Server.broadcast("❌ " + username + " покинул игру.");
            }
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public String getUsername() {
        return this.username;
    }
}
