package com.example.hibernatepolymorph.entity;


import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import org.hibernate.annotations.Any;
import org.hibernate.annotations.AnyDiscriminator;
import org.hibernate.annotations.AnyDiscriminatorValue;
import org.hibernate.annotations.AnyKeyJavaClass;

import java.util.StringJoiner;

@Entity
@Table(name = "property_holder")
public class PropertyHolder {

    @Id
    private Long id;

    @Any
    @AnyDiscriminator(DiscriminatorType.STRING)
    @AnyDiscriminatorValue(discriminator = "S", entity = StringProperty.class)
    @AnyDiscriminatorValue(discriminator = "I", entity = IntegerProperty.class)
    @AnyKeyJavaClass(Long.class)
    @Column(name = "property_type")
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

