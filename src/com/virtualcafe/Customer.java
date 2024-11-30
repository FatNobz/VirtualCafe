package com.virtualcafe;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Customer {
    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;

    // Constructor to initialise the customer with server address and port
    public Customer(String serverAddress, int port) throws IOException {
        socket = new Socket(serverAddress, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    // Start the customer interaction with the server
    public void start() {
        // Ctrl-C shutdown
        addShutdownHook();

        try (Scanner scanner = new Scanner(System.in)) {
            displayInstructions();  // Display how the system works for the customer

            // Prompt the customer for name and send it to the server
            System.out.print("Enter your name: ");
            String customerName = scanner.nextLine();
            out.println(customerName);  // Send the customer name to server

            // Start a thread to listen for server messages asynchronously
            new Thread(this::listenForServerMessages).start();

            // Command loop for sending commands to the server
            while (true) {
                String command = scanner.nextLine().trim().toLowerCase();
                if (command.equals("exit")) {
                    Exit();
                    break;
                } else if (command.equals("collect")) {
                    Collect();
                } else {
                    out.println(command);  // Send the command to server
                }
            }
        } finally {
            closeConnection();
        }
    }

    // Display the instructions for the customer on how to interact
    private void displayInstructions() {
        System.out.println("Welcome to the Virtual Café!");
        System.out.println("Here’s how to use this program:");
        System.out.println("--------------------------------");
        System.out.println("1. Ordering drinks: Type `order <quantity> <drink>` to place an order.");
        System.out.println("   Examples: `order 2 teas`, `order 1 coffee`, `order 2 teas and 3 coffees`");
        System.out.println("2. Checking order status: Type `order status` to check your order status.");
        System.out.println("3. Collect your order: Type `collect` to collect your order.");
        System.out.println("4. To exit the café: Type `exit` to exit the café. Or press Ctrl-C to alternatively exit.");
        System.out.println("--------------------------------");
        System.out.println("Please enter your commands below. Enjoy your experience!");
        System.out.println();
    }

    // Listen for messages from the server and print them
    private void listenForServerMessages() {
        try {
            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                System.out.println("Server: " + serverMessage);
            }
        } catch (IOException e) {
            System.out.println("Connection to server lost.");
        }
    }

    // Handle the "collect" command when the customer wants to collect their order
    private void Collect() {
        System.out.println("I am now about to collect my order.");
        out.println("collect");
    }

    // Handle the "exit" command to terminate the connection
    private void Exit() {
        System.out.println("I am now exiting the café.");
        out.println("exit");
    }

    // Add a shutdown hook to ensure the client exits on Ctrl-C
    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Received termination signal (Ctrl-C). Exiting the café gracefully...");
            out.println("exit");  // Notify the server that the client is disconnecting
        }));
    }

    // Close the socket and input/output streams
    private void closeConnection() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.out.println("Error closing client connection: " + e.getMessage());
        }
    }

    // Main method to run the Customer client
    public static void main(String[] args) {
        try {
            Customer customer = new Customer("localhost", 50000);
            customer.start();
        } catch (IOException e) {
            System.out.println("Unable to connect to the server: " + e.getMessage());
        }
    }
}
