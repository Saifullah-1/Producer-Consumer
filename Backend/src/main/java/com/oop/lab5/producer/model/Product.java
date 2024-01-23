package com.oop.lab5.producer.model;

import com.oop.lab5.producer.service.RandomColorGenerator;

public class Product {
    private final long id;
    private final String color;
    private final long rate; // will be generated randomly in this class

    public Product(long id) {
        this.id = id;
        this.color = RandomColorGenerator.generateRandomColor(); // creating random color
        this.rate = (int) (Math.random() * 30) + 1; // creating random rate !!will be changed!!
    }

    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", color='" + color + '\'' +
                ", rate=" + rate +
                '}';
    }
}
