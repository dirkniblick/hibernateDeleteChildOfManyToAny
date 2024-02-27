package com.example.hibernatepolymorph.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.AnyDiscriminator;
import org.hibernate.annotations.AnyDiscriminatorValue;
import org.hibernate.annotations.AnyKeyJavaClass;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ManyToAny;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;


@Entity
@Table(name = "property_repository")
public class PropertyRepository {

    @Id
    private Long id;

    @ManyToAny
    @AnyDiscriminator(DiscriminatorType.STRING)
    @Column(name = "property_type", columnDefinition = "varchar(1) not null check (property_type in ('S','I'))")
    @AnyKeyJavaClass(Long.class)
    @AnyDiscriminatorValue(discriminator = StringProperty.DISCRIMINATOR, entity = StringProperty.class)
    @AnyDiscriminatorValue(discriminator = IntegerProperty.DISCRIMINATOR, entity = IntegerProperty.class)
    @Cascade(CascadeType.ALL)
    @JoinTable(name = "repository_properties",
            joinColumns = @JoinColumn(name = "repository_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "property_id", nullable = false),
            uniqueConstraints = @UniqueConstraint(columnNames = {"repository_id", "property_id", "property_type"})
    )
    private List<Property<?>> properties = new ArrayList<>();


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Property<?>> getProperties() {
        return properties;
    }

    public void setProperties(List<Property<?>> properties) {
        this.properties = properties;
    }


    @Override
    public String toString() {
        return new StringJoiner(", ", PropertyRepository.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("properties=" + properties.stream().map(Property::getName).collect(Collectors.joining(",")))
                .toString();
    }
}