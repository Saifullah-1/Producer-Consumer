package com.oop.lab5.producer.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.oop.lab5.producer.observer.IObservable;
import com.oop.lab5.producer.service.ProductionLineService;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Machine implements IObservable, Runnable {
    private final int id;
    private String color = "#12cc7c"; // Machine color will be the same of current product color
    private boolean state = true; // indicates whether the machine is busy or not
    private Product currentProduct;
    private final int serviceTime;  // will be generated randomly in this class
    private final List<ProductQueue> connectedQueues = new ArrayList<>(); // Queues which supply the machine with products <which are observers>
    private ProductQueue destQueue;
    private volatile boolean isRunning = true;

    public Machine(int id) {
        this.id = id;
        this.serviceTime = (int) (Math.random() * 30) + 1; // creating random rate !!will be changed!!
        System.out.println("Service time " + serviceTime);
    }

    public int getId() {
        return id;
    }

    public String getColor() {
        return color;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public void addProduct(Product product) throws InterruptedException, JsonProcessingException {
        this.currentProduct = product;
        this.color = currentProduct.getColor();
        Thread.sleep(this.serviceTime * 1000L);
        this.destQueue.addProduct(this.currentProduct);
        this.currentProduct = null;
        this.color = "#12cc7c";
    }

    public void setDestQueue(ProductQueue destQueue) {
        this.destQueue = destQueue;
    }

    @Override
    public void attachQueue(ProductQueue queue) { // Queue is source
        this.connectedQueues.add(queue);
        queue.connectToMachine(this);
    }

    @Override
    public void notifyQueues() throws InterruptedException,JsonProcessingException {
        //inform the connected queues that the machine is ready and get the product from them
        for (ProductQueue queue : this.connectedQueues)
            queue.updateState(this);
    }

    public Thread process() {
        this.isRunning = true;
        Thread thread = new Thread(this);
        thread.start(); // call run
        return thread;
    }

    public void stop() {
        this.isRunning = false;
    }

    @Override
    public void run() {
        while (isRunning) {
            this.state = true;
            try {
                this.notifyQueues();
            } catch (InterruptedException  | JsonProcessingException e ) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("state", state);
        json.put("color", color);
        json.put("currentProduct", currentProduct);
        json.put("serviceTime", serviceTime);
        json.put("connectedQueues", connectedQueues);
        json.put("destQueue", destQueue);

        return json.toString();
    }
}