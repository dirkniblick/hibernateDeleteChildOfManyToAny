package com.example.hibernatepolymorph.entity;

public interface Property<T> {

    Long getId();

    String getName();

    T getValue();

    String getDiscriminator();
}