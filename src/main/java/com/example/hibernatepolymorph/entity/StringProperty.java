package com.example.hibernatepolymorph.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.StringJoiner;

@Entity
@Table(name="string_property")
public class StringProperty implements Property<String> {

    public static final String DISCRIMINATOR = "S";

    @Id
    private Long id;

    @Column(name = "`name`")
    private String name;

    @Column(name = "`value`")
    private String value;


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
    public String getValue() {
        return value;
    }

    @Override
    public String getDiscriminator() {
        return DISCRIMINATOR;
    }

    public void setValue(String value) {
        this.value = value;
    }


    @Override
    public String toString() {
        return new StringJoiner(", ", StringProperty.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("name='" + name + "'")
                .add("value='" + value + "'")
                .toString();
    }
}