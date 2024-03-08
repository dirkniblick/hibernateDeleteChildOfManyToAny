package com.example.hibernatepolymorph.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import org.hibernate.annotations.Any;

import java.util.StringJoiner;


@Entity
@Table(name = "property_holder", indexes = {@Index(name = "foo", columnList = "property_id,property_type"), })
public class PropertyHolder {

    @Id
    private Long id;

    @Any
    @PropertyDiscriminator
    @Column(name = "property_type", columnDefinition = "varchar(1) check (property_type in ('S','I'))")
    @JoinColumn(name = "property_id")
    private Property<?> property;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Property<?> getProperty() {
        return property;
    }

    public void setProperty(Property<?> property) {
        this.property = property;
    }


    @Override
    public String toString() {
        return new StringJoiner(", ", PropertyHolder.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("property=" + property)
                .toString();
    }
}

