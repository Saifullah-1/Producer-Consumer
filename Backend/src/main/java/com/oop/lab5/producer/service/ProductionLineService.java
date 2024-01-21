package com.oop.lab5.producer.service;

import com.oop.lab5.producer.model.Machine;
import com.oop.lab5.producer.model.Product;
import com.oop.lab5.producer.model.ProductQueue;
import com.oop.lab5.producer.snapshot.CareTaker;
import com.oop.lab5.producer.snapshot.Originator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

@org.springframework.stereotype.Service
public class ProductionLineService {
    private long productID = 1;
    private long machineID = 1;
    private long queueID = 1;
    private HashMap<Long, Machine> machines = new HashMap<>();
    private HashMap<Long, ProductQueue> queues = new HashMap<>();
    private Queue<Product> products =  new LinkedList<>();
    public List<Thread> threads = new ArrayList<>();
    public Originator originator = new Originator();// apply singleton dp for best practice
    public CareTaker careTaker = new CareTaker(); // apply singleton dp for best practice

    private static ProductionLineService instance;
    private ProductionLineService() {}

    public static ProductionLineService getInstance() {
        if(instance == null){
            return new ProductionLineService();
        }
        return instance;
    }

    public void run(){
        originator.clear();
        careTaker.clear();
        //write run logic
        //handle if there is no products in q0
        machines.forEach((key,value) ->
                threads.add(value.process())
        );
    }

    public void addProducts(long number) {
        while (number > 0) {
            Product p = new Product(this.productID++);
            this.products.add(p);
            number--;
        }
    }

    public void addMachine() {
        Machine m = new Machine(this.machineID);
        this.machines.put(this.machineID++, m);
//        System.out.println("mID " + this.machineID);
    }

    public void addQueue() {
        ProductQueue q = new ProductQueue(this.queueID);
        if (this.queues.isEmpty())
            q.setProducts(this.products);
        this.queues.put(this.queueID++, q);
//        System.out.println("qID " + this.queueID);
    }

    public void connect(long srcID, long destID, boolean isSrcMachine) {
        if (isSrcMachine) {
            Machine srcM = this.machines.get(srcID);
            ProductQueue destQ = this.queues.get(destID);
            srcM.setDestQueue(destQ);
        }else {
            Machine destM = this.machines.get(destID);
            ProductQueue srcQ = this.queues.get(srcID);
            destM.attachQueue(srcQ);
        }
    }

    public ProductQueue outputStream() {
        for (ProductQueue q : this.queues.values()) {
            if (q.getConnectedMachines().isEmpty())
                return q;
        }
        return null;
    }

    public void autoSave() { // Make snapshot << this method should be used during simulation
//        System.out.println("id " + this.queueID);
        for (ProductQueue q : this.queues.values())
            originator.addQueue(q);

        for (Machine m : this.machines.values())
            originator.addMachine(m);

        careTaker.add(originator.saveStateToMemento());
        System.out.println("State data:");
        System.out.println(originator.toString());
    }

    public String replay() {
        int step = 0;
        JSONObject stepStatusJson = new JSONObject();
        while (step < careTaker.size()) {
            originator.getStateFromMemento(careTaker.get(step++));
            JSONArray colors = new JSONArray();
            originator.getColors().forEach((key, value) ->
                    colors.put(new JSONObject().put("id", key).put("color", value))
            );

            JSONArray qProducts = new JSONArray();
            originator.getQueues().forEach((key, value) ->
                    qProducts.put(new JSONObject().put("id", key).put("products", value))
            );

            stepStatusJson.put("colors", colors);
            stepStatusJson.put("products", qProducts);
        }
        return stepStatusJson.toString();
    }

    public void board() {
        System.out.println("Queues");
        for (long i = 1; i < this.queues.size() + 1; ++i) {
            System.out.println(queues.get(i).toString());
        }

        System.out.println("Machines");
        for (long i = 1; i < this.machines.size() + 1; ++i) {
            System.out.println(machines.get(i).toString());
        }
    }

    public static void main(String[] args) {
        ProductionLineService service = ProductionLineService.getInstance();
        service.addMachine();
        service.addMachine();
        service.addQueue();
        service.addQueue();
        service.addProducts(10);

        service.connect(1,1,false);
        service.connect(1,2,false);
        service.connect(1,2,true);
        service.connect(2,2,true);

        service.run();
    }
}

// saving steps in order to sending them to frontend
// adding products after adding queues
/*
while(true) {
    djhnvfsksdfc





    autosave();
}
 */