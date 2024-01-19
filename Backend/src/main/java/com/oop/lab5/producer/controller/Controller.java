package com.oop.lab5.producer.controller;

import com.oop.lab5.producer.service.Service;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("")
@RestController
@RequestMapping("/")
public class Controller {
    private Service service;

    public Controller(Service service) {
        this.service = service;
    }

    // We will start with adding a machine and a queue
    @PostMapping("/addItem")
    public void addItem(@RequestParam boolean isMachine) {
        if (isMachine) {
            service.addMachine();
        } else {
            service.addQueue();
        }
    }

    @PostMapping("/addProducts")
    public void addProduct(@RequestParam long number) {
        this.service.addProducts(number);
    }

    @PostMapping("/connect")
    public void connect(@RequestParam long srcID, @RequestParam long distID, @RequestParam boolean isSrcMachine) {
        service.connect(srcID, distID, isSrcMachine);
    }

    @GetMapping("/run")
    public void run() {
        service.board();
    }

    @GetMapping("/getStatus")
    public String status() {
        return service.getStatus();
    }

    @GetMapping("/finished")
    public boolean finished() {
        return service.isFinished();
    }

}
