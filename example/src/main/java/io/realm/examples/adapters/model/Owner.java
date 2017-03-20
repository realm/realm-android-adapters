package io.realm.examples.adapters.model;

import java.util.concurrent.atomic.AtomicInteger;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Owner extends RealmObject {
    @PrimaryKey
    private int id;
    private String name;
    private Dog dog;

    private static AtomicInteger counter = new AtomicInteger(0);

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Dog getDog() {
        return dog;
    }

    public void setDog(Dog dog) {
        this.dog = dog;
    }

    public static Owner create() {
        int id = counter.getAndIncrement();
        Dog dog = new Dog();
        dog.setName("Dog-" + id);
        Owner owner = new Owner();
        owner.id = id;
        owner.setName("Owner-" + id);
        owner.setDog(dog);
        return owner;
    }
}
