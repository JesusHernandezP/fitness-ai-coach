package com.fitness.fitnessaicoach.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "foods")
public class Food {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    private double calories;

    private double protein;

    private double carbs;

    private double fat;

    public Food() {
    }

    public Food(String name, double calories, double protein, double carbs, double fat) {
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getCalories() {
        return calories;
    }

    public double getProtein() {
        return protein;
    }

    public double getCarbs() {
        return carbs;
    }

    public double getFat() {
        return fat;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }
}
