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
            // create a machine
        } else {
            // create a queue
        }
    }

    @PostMapping("/addProducts")
    public void addProduct(@RequestParam long number) {
        this.service.addProducts(number);
    }

//    @GetMapping("/deleteItem")
//    public String deleteItem(@RequestParam long id, @RequestParam boolean isMachine) {
//        if (isMachine) {
//            // remove the machine
//        } else {
//            // remove the queue
//        }
//        // return all the data >> konva board
//    }

    @PostMapping("/connect")
    public void connect(@RequestParam long srcID, @RequestParam long distID, @RequestParam boolean isSrcMachine) {

    }

    @GetMapping("/run")
    public String run() {
        return "";
    }

}
