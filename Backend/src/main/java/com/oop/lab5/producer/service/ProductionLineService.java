package com.oop.lab5.producer.service;

import com.oop.lab5.producer.model.Machine;
import com.oop.lab5.producer.model.Product;
import com.oop.lab5.producer.model.ProductQueue;
import com.oop.lab5.producer.snapshot.Memento;
import com.oop.lab5.producer.snapshot.Originator;
import com.oop.lab5.producer.snapshot.SnapshotDP;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProductionLineService {
    private int productID = 1;
    private int machineID = 1;
    private int queueID = 1;
    private HashMap<Integer, Machine> machines = new HashMap<>();
    private HashMap<Integer, ProductQueue> queues = new HashMap<>();
    private Queue<Product> products =  new LinkedList<>();
    private final List<Thread> threads = new ArrayList<>();
    private final SnapshotDP snapshotDP = new SnapshotDP();
    private static ProductionLineService instance;

    private ProductionLineService() {}

    public static ProductionLineService getInstance() {
        if(instance == null){
            instance = new ProductionLineService();
        }
        return instance;
    }

    public int productsNo() {
        return (this.productID - 1);
    }

    public void run() {
        this.snapshotDP.clear();
        autoSave();
        queues.get(1).setProducts(new LinkedList<>(products));
        machines.forEach((key,value) -> threads.add(value.process()));
    }

    public synchronized String sendStep() {
        JSONObject singleStep = new JSONObject();
        JSONArray colors = new JSONArray();
        JSONArray products = new JSONArray();
        machines.forEach((key, value) ->
                colors.put(new JSONObject().put("id", key).put("color", value.getColor()))
        );

        queues.forEach((key, value) ->
                products.put(new JSONObject().put("id", key).put("products", value.getProducts().size()))
        );

        singleStep.put("colors", colors);
        singleStep.put("products", products);
        singleStep.put("isFinished", isFinished());

        if (isFinished()) { // stopping the threads
            for (Machine m : this.machines.values())
                m.stop();
        }

        System.out.println(singleStep.toString());
        return singleStep.toString();
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
        System.out.println("mID " + this.machineID);
    }

    public void addQueue() {
        ProductQueue q = new ProductQueue(this.queueID);
        this.queues.put(this.queueID++, q);
        System.out.println("qID " + this.queueID);
    }

    public void connect(int srcID, int destID, boolean isSrcMachine) {
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

    public boolean isFinished() {
        ProductQueue output = outputStream();
        return output != null && output.getProducts().size() == productsNo();
    }

    public void autoSave() { // Make snapshot << this method should be used during simulation
        this.snapshotDP.clear();

        Originator new_originator = new Originator();
        new_originator.addQueues(new HashMap<>(this.queues));
        new_originator.addMachines(new HashMap<>(this.machines));
        new_originator.addProducts(new LinkedList<>(this.products));

        Memento memento = new_originator.saveStateToMemento();
        this.snapshotDP.getCareTaker().addMemento(memento);
    }

    public boolean replay() {
        if (this.snapshotDP.getCareTaker().getCurrentMemento() == null)
            return false;

        System.out.println("condition 1 passed");

        if (this.snapshotDP.getCareTaker().getCurrentMemento().getProducts().isEmpty())
            return false;

        System.out.println("condition 2 passed");

        clear();
        Originator originator = new Originator();
        originator.getStateFromMemento(this.snapshotDP.getCareTaker().getCurrentMemento());
        this.queues = new HashMap<>(originator.getQueues());
        this.queueID = this.queues.size() + 1;
        this.machines = new HashMap<>(originator.getMachines());
        this.machineID = this.machines.size() + 1;
        this.products = new LinkedList<>(originator.getProducts());
        this.productID = this.products.size() + 1;
        this.queues.forEach((key, value) ->
                value.setProducts(new LinkedList<>())
        );

        System.out.println("Start Replay");
        run();
        return true;
    }

    public void clear() {
        this.queues.clear();
        this.machines.clear();
        this.products.clear();
        this.threads.clear();
        this.productID = 1;
        this.queueID = 1;
        this.machineID = 1;
    }
}