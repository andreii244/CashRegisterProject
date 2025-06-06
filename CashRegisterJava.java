package com.mycompany.cashregister.java;

import java.util.*;

/**
 *
 * @author Gabriel
 */
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

public class CashRegisterJava {
    static Scanner input = new Scanner(System.in);
    static ArrayList<String[]> accounts = new ArrayList<>();
    static ArrayList<CartItem> cartItems = new ArrayList<>();
    static String currentUser = "";

    static final Pattern USER_CHECK = Pattern.compile("^[a-zA-Z0-9]{5,15}$");
    static final Pattern PASS_CHECK = Pattern.compile("^(?=.*[A-Z])(?=.*\\d).{8,20}$");
    static final String TRANSACTION_FILE = "transactions.txt";

    // CartItem class to better manage cart items
    static class CartItem {
        String name;
        double price;
        int quantity;
        double total;

        CartItem(String name, double price, int quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.total = price * quantity;
        }

        @Override
        public String toString() {
            return name + " | ₱" + String.format("%.2f", price) +
                   " x " + quantity + " = P" + String.format("%.2f", total);
        }
    }

    public static double inputAmount(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String inputStr = input.nextLine().trim();
                double amt = Double.parseDouble(inputStr);
                if (amt > 0) return amt;
                System.out.println("Amount must be greater than zero.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    public static int inputInteger(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String inputStr = input.nextLine().trim();
                int num = Integer.parseInt(inputStr);
                if (num > 0) return num;
                System.out.println("Number must be greater than zero.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    public static int inputChoice(String prompt, int min, int max) {
        while (true) {
            try {
                System.out.print(prompt);
                String inputStr = input.nextLine().trim();
                int choice = Integer.parseInt(inputStr);
                if (choice >= min && choice <= max) return choice;
                System.out.println("Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    public static void createAccount() {
        System.out.println("\n--- Create Account ---");
        String user, pass;

        while (true) {
            System.out.print("Username (5-15 alphanumeric): ");
            user = input.nextLine().trim();
            if (!USER_CHECK.matcher(user).matches()) {
                System.out.println("Username not valid. Must be 5-15 alphanumeric characters.");
                continue;
            }

            System.out.print("Password (8-20 chars, 1 uppercase, 1 number): ");
            pass = input.nextLine();
            if (!PASS_CHECK.matcher(pass).matches()) {
                System.out.println("Password not valid. Must be 8-20 characters with at least 1 uppercase and 1 number.");
                continue;
            }

            boolean taken = false;
            for (String[] acc : accounts) {
                if (acc[0].equals(user)) {
                    taken = true;
                    break;
                }
            }

            if (taken) {
                System.out.println("Username already used. Try another.");
            } else {
                accounts.add(new String[]{user, pass});
                System.out.println("Account successfully created.\n");
                break;
            }
        }
    }

    public static void accessAccount() {
        System.out.println("\n--- Login ---");
        while (true) {
            System.out.print("Username: ");
            String user = input.nextLine().trim();
            System.out.print("Password: ");
            String pass = input.nextLine();

            boolean match = false;
            for (String[] acc : accounts) {
                if (acc[0].equals(user) && acc[1].equals(pass)) {
                    match = true;
                    currentUser = user;
                    break;
                }
            }

            if (match) {
                System.out.println("Logged in successfully!\n");
                cashMenu();
                break;
            } else {
                System.out.println("Login failed. Please try again.");
            }
        }
    }

    public static void addItem() {
        System.out.print("Product name: ");
        String name = input.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Product name cannot be empty.");
            return;
        }

        double cost = inputAmount("Product price: P");
        int qty = inputInteger("Quantity: ");

        CartItem newItem = new CartItem(name, cost, qty);
        cartItems.add(newItem);
        System.out.println("Item added successfully!");
    }

    public static void showCart() {
        System.out.println("\n--- Your Cart ---");
        if (cartItems.isEmpty()) {
            System.out.println("Cart is empty.");
            return;
        }

        double sum = 0;
        for (int i = 0; i < cartItems.size(); i++) {
            System.out.println((i + 1) + ". " + cartItems.get(i));
            sum += cartItems.get(i).total;
        }
        System.out.println("Total: P" + String.format("%.2f", sum));
    }

    public static void updateQuantity() {
        if (cartItems.isEmpty()) {
            System.out.println("Cart is empty. Add items first.");
            return;
        }

        showCart();
        int itemIndex = inputChoice("Enter item number to update: ", 1, cartItems.size()) - 1;
        int newQty = inputInteger("Enter new quantity: ");

        CartItem item = cartItems.get(itemIndex);
        item.quantity = newQty;
        item.total = item.price * newQty;
        System.out.println("Quantity updated successfully!");
    }

    public static void removeItem() {
        if (cartItems.isEmpty()) {
            System.out.println("Cart is empty. Add items first.");
            return;
        }

        showCart();
        int itemIndex = inputChoice("Enter item number to remove: ", 1, cartItems.size()) - 1;
        CartItem removedItem = cartItems.remove(itemIndex);
        System.out.println("Removed: " + removedItem.name);
    }

    public static void saveTransaction(double totalDue, double paid, double change) {
        try (FileWriter writer = new FileWriter(TRANSACTION_FILE, true)) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            writer.write("=== TRANSACTION RECORD ===\n");
            writer.write("Date & Time: " + now.format(formatter) + "\n");
            writer.write("Cashier: " + currentUser + "\n");
            writer.write("Items Purchased:\n");
            
            for (CartItem item : cartItems) {
                writer.write("  - " + item.toString() + "\n");
            }
            
            writer.write("Total Amount: P" + String.format("%.2f", totalDue) + "\n");
            writer.write("Amount Paid: P" + String.format("%.2f", paid) + "\n");
            writer.write("Change: P" + String.format("%.2f", change) + "\n");
            writer.write("=============================\n\n");
            
            System.out.println("Transaction saved to " + TRANSACTION_FILE);
        } catch (IOException e) {
            System.out.println("Error saving transaction: " + e.getMessage());
        }
    }

    public static void makePayment() {
        if (cartItems.isEmpty()) {
            System.out.println("Cart is empty. Add items first.");
            return;
        }

        double totalDue = 0;
        for (CartItem item : cartItems) {
            totalDue += item.total;
        }

        System.out.println("\n--- Checkout ---");
        showCart();
        System.out.println("Total Due: P" + String.format("%.2f", totalDue));

        double paid;
        do {
            paid = inputAmount("Enter payment amount: P");
            if (paid < totalDue) {
                System.out.println("Insufficient payment. Required: P" + String.format("%.2f", totalDue));
            }
        } while (paid < totalDue);

        double change = paid - totalDue;
        System.out.println("\n=== RECEIPT ===");
        System.out.println("Total: P" + String.format("%.2f", totalDue));
        System.out.println("Paid: P" + String.format("%.2f", paid));
        System.out.println("Change: P" + String.format("%.2f", change));
        System.out.println("Payment successful!");

        // Save transaction to file
        saveTransaction(totalDue, paid, change);
        
        cartItems.clear();
    }

    public static void viewTransactionHistory() {
        System.out.println("\n--- Transaction History ---");
        try (BufferedReader reader = new BufferedReader(new FileReader(TRANSACTION_FILE))) {
            String line;
            boolean hasContent = false;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                hasContent = true;
            }
            if (!hasContent) {
                System.out.println("No transaction history found.");
            }
        } catch (FileNotFoundException e) {
            System.out.println("No transaction history file found.");
        } catch (IOException e) {
            System.out.println("Error reading transaction history: " + e.getMessage());
        }
    }

    public static void cashMenu() {
        while (true) {
            System.out.println("\n--- Cash Register ---");
            System.out.println("1. Add Item");
            System.out.println("2. View Cart");
            System.out.println("3. Update Item Quantity");
            System.out.println("4. Remove Item");
            System.out.println("5. Checkout");
            System.out.println("6. View Transaction History");
            System.out.println("7. Logout");

            int choice = inputChoice("Select option (1-7): ", 1, 7);

            switch (choice) {
                case 1: addItem(); break;
                case 2: showCart(); break;
                case 3: updateQuantity(); break;
                case 4: removeItem(); break;
                case 5: makePayment(); break;
                case 6: viewTransactionHistory(); break;
                case 7: 
                    System.out.println("Logged out successfully.");
                    currentUser = "";
                    return;
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== CASH REGISTER SYSTEM ===");
        
        while (true) {
            System.out.println("\n=== Welcome Andrei's Cash Register===");
            System.out.println("1. Sign Up");
            System.out.println("2. Login");
            System.out.println("3. Exit");

            int choice = inputChoice("Your choice (1-3): ", 1, 3);

            switch (choice) {
                case 1: createAccount(); break;
                case 2: accessAccount(); break;
                case 3: 
                    System.out.println("Thank you for using Andrei's Cash Register System. Goodbye!");
                    return;
            }
        }
    }
}