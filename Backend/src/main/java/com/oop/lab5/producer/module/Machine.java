package com.oop.lab5.producer.module;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Machine implements IObservable, Runnable {
    private Thread thread;
    private long id;
    private String color = ""; // Machine color will be the same of current product color
    private boolean state = true; // indicates whether the machine is busy or not
    private Product currentProduct;
    private long serviceTime;  // will be generated randomly in this class
    private List<ProductQueue> connectedQueues = new ArrayList<>(); // Queues which supply the machine with products <which are observers>
    private ProductQueue distQueue;

    public Machine(long id) {
        this.id = id;
        this.serviceTime = (long) (Math.random() * 10) + 1; // creating random rate !!will be changed!!
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public Product getCurrentProduct() {
        return currentProduct;
    }

    public void addProduct(Product product) throws InterruptedException {
        this.currentProduct = product;
        this.color = currentProduct.getColor();
        Thread.sleep(this.serviceTime * 1000);
        this.distQueue.addProduct(this.currentProduct);
        this.currentProduct = null;
        this.color = "";
        this.run();
    }

    public long getServiceTime() {
        return serviceTime;
    }

    public List<ProductQueue> getConnectedQueues() {
        return connectedQueues;
    }

    public void setConnectedQueues(List<ProductQueue> connectedQueues) {
        this.connectedQueues = connectedQueues;
    }

    public ProductQueue getDistQueue() {
        return distQueue;
    }

    public void setDistQueue(ProductQueue distQueue) {
        this.distQueue = distQueue;
    }

    @Override
    public void attachQueue(ProductQueue queue) { // Queue is source
        this.connectedQueues.add(queue);
        queue.connectToMachine(this);
    }

    @Override
    public void notifyQueues() {
        //inform the connected queues that the machine is ready and get the product from them
        for (ProductQueue queue : this.connectedQueues)
            queue.updateState(this);
    }

    public Thread process() {
        Thread thread = new Thread(this);
        thread.start(); // call run
        return thread;
    }

    @Override
    public void run() {
        while (true) {
            this.state = true;
            this.notifyQueues();
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
        json.put("distQueue", distQueue);

        return json.toString();
    }
}
