package com.example.hibernatepolymorph;

import com.example.hibernatepolymorph.entity.IntegerProperty;
import com.example.hibernatepolymorph.entity.PropertyHolder;
import com.example.hibernatepolymorph.entity.StringProperty;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class PropertyHolderTests extends HibernateTest {

    @Test
    @Order(1)
    void createPropertyHolder() {
        IntegerProperty ageProperty = new IntegerProperty();
        ageProperty.setId(1L);
        ageProperty.setName("age");
        ageProperty.setValue(23);

        save(ageProperty);
        System.out.printf("Created: %s%n", ageProperty);

        StringProperty nameProperty = new StringProperty();
        nameProperty.setId(1L);
        nameProperty.setName("name");
        nameProperty.setValue("John Doe");

        save(nameProperty);
        System.out.printf("Created: %s%n", nameProperty);

        PropertyHolder namePropertyHolder = new PropertyHolder();
        namePropertyHolder.setId(1L);
        save(namePropertyHolder);
        System.out.printf("Created: %s%n", namePropertyHolder);

        namePropertyHolder.setProperty(nameProperty);
        update(namePropertyHolder);
        System.out.printf("Updated: %s%n", namePropertyHolder);

        assertThat(namePropertyHolder.getId()).isNotNull();
    }

    @Test
    @Order(2)
    void verifyPropertyHolder() {
        System.out.printf("Verifying holder %d has item%n", 1L);
        PropertyHolder propertyHolder = retrieve(PropertyHolder.class, 1L);
        System.out.printf("Retrieved: %s%n", propertyHolder);

        assertThat(propertyHolder.getProperty()).satisfies(p -> {
            assertThat(p.getId()).isEqualTo(1L);
            assertThat(p.getName()).isEqualTo("name");
            assertThat(p.getValue()).isEqualTo("John Doe");
        });
    }

    @Test
    @Order(3)
    void deleteProperty() {
        System.out.printf("Deleting: StringProperty %d%n", 1L);
        delete(StringProperty.class, 1L);

        System.out.printf("Verifying PropertyHolder %d has no child%n", 1L);
        PropertyHolder propertyHolder = retrieve(PropertyHolder.class, 1L);
        assertThat(propertyHolder.getProperty()).isNull();
    }
}
