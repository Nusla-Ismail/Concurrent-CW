package CoffeeShop;

import java.util.ArrayList;
import java.util.Random;

public class CoffeeShopSimulation {
    public static void main(String[] args) {
        CoffeeShop coffeeShop = new CoffeeShop(10);

        // Create customer threads
        Thread[] customers = new Thread[10];
        for (int i = 0; i < 10; i++) {
            customers[i] = new Customer(coffeeShop, i + 1);
        }

        // Create barista threads
        Thread barista1 = new Barista(coffeeShop, 1);
        Thread barista2 = new Barista(coffeeShop, 2);

        // Start all customer threads
        for (Thread customer : customers) {
            customer.start();
        }

        // Start barista threads
        barista1.start();
        barista2.start();

        // Wait for all customers to finish before stopping baristas
        for (Thread customer : customers) {
            try {
                customer.join(); // Wait for each customer to finish placing orders
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // Signal baristas to stop
        coffeeShop.stopBaristas();
    }
}

class CoffeeShop {
    private final int maxQueue;
    private final ArrayList<String> orderQueue = new ArrayList<>();
    private boolean stopBaristas = false;

    public CoffeeShop(int maxQueue) {
        this.maxQueue = maxQueue;
    }

    public synchronized void placeOrder(String order) {
        while (orderQueue.size() == maxQueue) {
            try {
                wait(); // Wait if the queue is full
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        orderQueue.add(order);
        System.out.println(Thread.currentThread().getName() + " has placed the coffee order: " + order);
        notify(); // Notify baristas that an order is available
    }

    public synchronized String receiveOrder() {
        while (orderQueue.isEmpty()) {
            try {
                if (stopBaristas) {
                    return null; // Return null to stop baristas if there's nothing more to do
                }
                wait(); // Wait if no orders are in the queue
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        String order = orderQueue.removeFirst(); // Remove the first order (correct method)
        System.out.println(Thread.currentThread().getName() + " has received the order: " + order);
        notify(); // Notify customers that there's space in the queue
        return order;
    }

    public synchronized void stopBaristas() {
        stopBaristas = true;
        notifyAll(); // Wake up any waiting barista threads to let them stop
    }
}

class Customer extends Thread {
    private final CoffeeShop coffeeShop;

    public Customer(CoffeeShop coffeeShop, int id) {
        super("Customer No." + id);
        this.coffeeShop = coffeeShop;
    }

    @Override
    public void run() {
        Random random = new Random();
        String[] customerOrders = {
                "Latte", "Cappuccino", "Espresso", "Mocha",
                "Americano", "Caramel Macchiato", "Flat White",
                "Iced Coffee", "Frappuccino", "Cold Brew"
        };

        for (int i = 0; i < 3; i++) { // Each customer places 3 orders
            int randomIndex = random.nextInt(customerOrders.length);
            coffeeShop.placeOrder(customerOrders[randomIndex]);

            try {
                sleep(1000); // Simulate time between placing orders
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

class Barista extends Thread {
    private final CoffeeShop coffeeShop;

    public Barista(CoffeeShop coffeeShop, int id) {
        super("Barista No." + id);
        this.coffeeShop = coffeeShop;
    }

    @Override
    public void run() {
        while (true) { // Continuously check for orders
            String order = coffeeShop.receiveOrder();
            if (order == null) {
                break; // Stop if no more orders are available
            }
            System.out.println(getName() + " is preparing the order: " + order);

            try {
                sleep(1000); // Simulate time to prepare the coffee
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println(getName() + " is stopping as there are no more orders.");
    }
}
