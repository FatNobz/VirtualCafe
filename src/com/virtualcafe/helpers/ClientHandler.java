package com.virtualcafe.helpers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private PrintWriter out;
    private String clientName;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();


    // Data structure to store client orders
    private static final ConcurrentHashMap<String, Order> clientOrders = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService brewingExecutor = Executors.newScheduledThreadPool(4);


    // Track all connected clients
    private static final ConcurrentHashMap<String, ClientHandler> connectedClients = new ConcurrentHashMap<>();


    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket; // Initialise client connection
    }

    // Logs messages and saves them to a JSON file
    private void logCurrentState(String message) {
        System.out.println("Barista log:" + message);

        try (FileWriter file = new FileWriter("barista_log.json", true)) {
            file.write(GSON.toJson(new LogEntry(DATE_FORMAT.format(LocalDateTime.now()), message)) + "\n");
        } catch (IOException e) {
            System.out.println("Error logging to JSON file: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        // Sets up communication with the client and processes commands
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            clientName = in.readLine().trim();
            connectedClients.put(clientName, this); // Register client
            out.println("Welcome, " + clientName + ", May I have your order, please?");
            String command;
            while ((command = in.readLine()) != null) {
                Command(command);
            }
        } catch (IOException e) {
            handleUnexpectedDisconnection();
        } finally {
            close();
        }
    }

    // Handles client commands based on their input
    private void Command(String command) {
        command = command.trim().toLowerCase();
        if (command.equals("order status")) {
            OrderStatus();
        } else if (command.startsWith("order")) {
            Order(command);
        } else if (command.equals("collect")) {
            Collect();
        } else if (command.equals("exit")) {
            Exit();
        } else {
            out.println("Error - Unknown command");
        }
    }

    // My attempted implementation of the resource reallocation
    private void handleUnexpectedDisconnection() {
        System.out.println("Client disconnected: " + clientName);
        Order abandonedOrder = clientOrders.remove(clientName);
        connectedClients.remove(clientName);

        if (abandonedOrder != null) {
            logCurrentState("Reassigning order from disconnected client " + clientName);
            if (!connectedClients.isEmpty()) {
                String newOwner = connectedClients.keys().nextElement();
                clientOrders.putIfAbsent(newOwner, new Order());
                Order newOwnerOrder = clientOrders.get(newOwner);
                newOwnerOrder.addToWaitingArea(abandonedOrder.getTeaInWaitingArea(), abandonedOrder.getCoffeeInWaitingArea());
                logCurrentState("Order reassigned from " + clientName + " to " + newOwner);
                connectedClients.get(newOwner).out.println("You have been assigned additional items from " + clientName + "'s order.");
            } else {
                logCurrentState("No clients available to reassign the order from " + clientName);
            }
        }
    }



    // Displays the current status of the client's order
    private void OrderStatus() {
        Order currentOrder = clientOrders.get(clientName);
        if (currentOrder != null) {
            out.println("Order status for " + clientName + ":\n" +
                    "- Waiting area: " + currentOrder.getWaitingDetails() + "\n" +
                    "- Brewing area: " + currentOrder.getBrewingDetails() + "\n" +
                    "- Tray area: " + currentOrder.getTrayDetails());
        } else {
            out.println("No order found for " + clientName);
        }
    }




    // Processes a new client order and adds it to the waiting area
    private void Order(String command) {
        int teaQty = 0, coffeeQty = 0;
        String[] parts = command.split("and");
        for (String part : parts) {
            if (part.contains("tea")) teaQty += extractQuantity(part, "tea");
            if (part.contains("coffee")) coffeeQty += extractQuantity(part, "coffee");
        }
        if (teaQty == 0 && coffeeQty == 0) {
            out.println("Invalid order: No valid items found in the command.");
            return;
        }
        clientOrders.putIfAbsent(clientName, new Order());
        Order clientOrder = clientOrders.get(clientName);
        clientOrder.addToWaitingArea(teaQty, coffeeQty);
        out.println("Order received for " + clientName + ": " + teaQty + " tea(s) and " + coffeeQty + " coffee(s).");
        logCurrentState("New order from " + clientName + ": " + teaQty + " tea(s) and " + coffeeQty + " coffee(s).");
        processBrewing(clientOrder);
    }




    // Starts brewing teas and coffees in batches
    private void processBrewing(Order clientOrder) {
        int teaToBrew = Math.min(2, clientOrder.getTeaInWaitingArea());
        if (teaToBrew > 0) {
            clientOrder.moveTeaToBrewing(teaToBrew);
            brewingExecutor.schedule(() -> {
                clientOrder.moveTeaToTray(teaToBrew);
                out.println("Tea ready for " + clientName + " (" + teaToBrew + " tea(s) moved to tray)");
                checkOrderCompletion(clientOrder);
                processBrewing(clientOrder); // Continue brewing next batch
            }, 30, TimeUnit.SECONDS);
        }
        int coffeeToBrew = Math.min(2, clientOrder.getCoffeeInWaitingArea());
        if (coffeeToBrew > 0) {
            clientOrder.moveCoffeeToBrewing(coffeeToBrew);
            brewingExecutor.schedule(() -> {
                clientOrder.moveCoffeeToTray(coffeeToBrew);
                out.println("Coffee ready for " + clientName + " (" + coffeeToBrew + " coffee(s) moved to tray)");
                checkOrderCompletion(clientOrder);
                processBrewing(clientOrder); // Continue brewing next batch
            }, 45, TimeUnit.SECONDS);
        }
    }



    // Checks if the client's order is fully completed
    private void checkOrderCompletion(Order clientOrder) {
        if (clientOrder.isReadyForCollection()) {
            out.println("Your entire order is now ready for collection!");
        }
    }



    // Handles the client's request to collect their order
    private void Collect() {
        Order clientOrder = clientOrders.get(clientName);
        if (clientOrder != null && clientOrder.isReadyForCollection()) {
            out.println("Enjoy your drinks!");
            clientOrders.remove(clientName);
            logCurrentState("Order collected by " + clientName + ". All items removed from tray.");
        } else {
            out.println("Your order is not yet ready for collection. Please wait a little longer.");
        }
    }



    // Handles client exit and cleans up their resources
    private void Exit() {
        clientOrders.remove(clientName);
        out.println("Goodbye, " + clientName + "!");
        close();
    }



    // Closes client socket and cleans up resources
    private void close() {
        try {
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    // Extracts quantity of an item from the client's command
    private int extractQuantity(String input, String item) {
        input = input.trim();
        String[] words = input.split(" ");
        for (int i = 0; i < words.length; i++) {
            if (words[i].contains(item)) {
                try {
                    return Integer.parseInt(words[i - 1]);
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    return 1;
                }
            }
        }
        return 0;
    }


    // LogEntry class for logging to JSON
    private record LogEntry(String timestamp, String message) {
    }
}
