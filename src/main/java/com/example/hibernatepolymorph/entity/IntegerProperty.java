package com.example.hibernatepolymorph.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.StringJoiner;

@Entity
@Table(name="integer_property")
public class IntegerProperty implements Property<Integer> {

    public static final String DISCRIMINATOR = "I";

    @Id
    private Long id;

    @Column(name = "`name`")
    private String name;

    @Column(name = "`value`")
    private Integer value;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String getDiscriminator() {
        return DISCRIMINATOR;
    }

    public void setValue(Integer value) {
        this.value = value;
    }


    @Override
    public String toString() {
        return new StringJoiner(", ", IntegerProperty.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("name='" + name + "'")
                .add("value=" + value)
                .toString();
    }
}