package com.example.hibernatepolymorph;

import com.example.hibernatepolymorph.entity.IntegerProperty;
import com.example.hibernatepolymorph.entity.PropertyRepository;
import com.example.hibernatepolymorph.entity.StringProperty;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PropertyRepositoryTests extends HibernateTest {

    @Test
    @Order(1)
    void createPropertyRepository() {
        IntegerProperty ageProperty = new IntegerProperty();
        ageProperty.setId(2L);
        ageProperty.setName("age");
        ageProperty.setValue(23);

        save(ageProperty);
        System.out.printf("Created: %s%n", ageProperty);

        StringProperty nameProperty = new StringProperty();
        nameProperty.setId(2L);
        nameProperty.setName("name");
        nameProperty.setValue("John Doe");

        save(nameProperty);
        System.out.printf("Created: %s%n", nameProperty);

        PropertyRepository propertyRepository = new PropertyRepository();
        propertyRepository.setId(1L);

        save(propertyRepository);
        System.out.printf("Created: %s%n", propertyRepository);

        propertyRepository.getProperties().add(ageProperty);
        propertyRepository.getProperties().add(nameProperty);

        update(propertyRepository);
        System.out.printf("Updated: %s%n", propertyRepository);

        assertThat(propertyRepository.getId()).isNotNull();
    }

    @Test
    @Order(2)
    void verifyRepository() {
        System.out.printf("Verifying repository %d has two items %n", 1L);
        PropertyRepository propertyRepository = retrieve(PropertyRepository.class, 1L);
        assertThat(propertyRepository.getProperties()).satisfiesExactlyInAnyOrder(
                age -> {
                    assertThat(age).isInstanceOf(IntegerProperty.class);
                    assertThat(age.getId()).isEqualTo(2L);
                    assertThat(age.getName()).isEqualTo("age");
                    assertThat(age.getValue()).isEqualTo(23);
                },
                name -> {
                    assertThat(name).isInstanceOf(StringProperty.class);
                    assertThat(name.getId()).isEqualTo(2L);
                    assertThat(name.getName()).isEqualTo("name");
                    assertThat(name.getValue()).isEqualTo("John Doe");
                }
        );
    }

    @Test
    @Order(3)
    void deleteProperty() {
        System.out.printf("Deleting: IntegerProperty %d%n", 2L);
        delete(IntegerProperty.class, 2L);

        System.out.printf("Verifying repository %d has only one item %n", 1L);
        PropertyRepository propertyRepository = retrieve(PropertyRepository.class, 1L);
        assertThat(propertyRepository.getProperties()).satisfiesExactlyInAnyOrder(
                name -> {
                    assertThat(name).isInstanceOf(StringProperty.class);
                    assertThat(name.getId()).isEqualTo(2L);
                    assertThat(name.getName()).isEqualTo("name");
                    assertThat(name.getValue()).isEqualTo("John Doe");
                }
        );
    }
}
