package com.virtualcafe;

import com.virtualcafe.helpers.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Barista {
    private static final int DEFAULT_PORT = 50000;
    private final ServerSocket serverSocket;

    //Constructor to initialise the server with the specified port.
    public Barista(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);
    }

    /**
     * Starts the server and continuously listens for client connections.
     * This method runs an infinite loop to accept and handle clients.
     */
    public void start() {
        System.out.println("Waiting for client connections...");
        while (true) {
            acceptClientConnection();
        }
    }


    //Accepts a single client connection and delegates handling to a separate thread.
    private void acceptClientConnection() {
        try {
            Socket clientSocket = serverSocket.accept();
            handleClientConnection(clientSocket);
        } catch (IOException e) {
            logError(e);
        }
    }


    //Handles the connected client by creating a new thread for communication.
    private void handleClientConnection(Socket clientSocket) {
        // Use a separate thread to handle the client, ensuring non-blocking behaviour for the server.
        new Thread(new ClientHandler(clientSocket)).start();
    }


    //Logs error messages and exception stack traces for debugging purposes.
    private void logError(Exception e) {
        System.err.println("Error accepting client connection");
        e.printStackTrace();
    }

    /**
     * Main method to start the server application.
     * Initialises the server on the default port and starts listening for clients.
     */
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        try {
            Barista server = new Barista(port);
            server.start();
        } catch (IOException e) {
            System.err.println("Error starting the server on port " + port);
            e.printStackTrace();
        }
    }
}
