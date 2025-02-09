package net.salesianos.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import net.salesianos.server.threads.ClientHandler;
import net.salesianos.utils.Constants;

public class ServerApp {
    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(Constants.SERVER_PORT)) {
            System.out.println("Server started on port " + serverSocket.getLocalPort());

            while (true) {
                System.out.println("Waiting for incoming connections...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandler.start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
}