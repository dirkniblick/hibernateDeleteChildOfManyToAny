package com.example.hibernatepolymorph;

import com.example.hibernatepolymorph.entity.IntegerProperty;
import com.example.hibernatepolymorph.entity.PropertyRepository;
import com.example.hibernatepolymorph.entity.StringProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


class PropertyRepositoryTests extends HibernateTest {

    private static final Logger logger = LogManager.getLogger(PropertyHolderTests.class);

    private static final long AGE_PROPERTY_ID = 20L;
    private static final long NAME_PROPERTY_ID = 21L;
    private static final long PROPERTY_REPOSITORY_ID = 23L;


    @Test
    @Order(1)
    void createProperties() {
        assertDoesNotThrow(() -> {
            IntegerProperty ageProperty = new IntegerProperty();
            ageProperty.setId(AGE_PROPERTY_ID);
            ageProperty.setName("age");
            ageProperty.setValue(23);
            save(ageProperty);
            logger.info("Created: {}", ageProperty);

            StringProperty nameProperty = new StringProperty();
            nameProperty.setId(NAME_PROPERTY_ID);
            nameProperty.setName("name");
            nameProperty.setValue("John Doe");
            save(nameProperty);
            logger.info("Created: {}", nameProperty);
        });
    }

    @Test
    @Order(2)
    void createPropertyRepository() {
        assertDoesNotThrow(() -> {
            IntegerProperty ageProperty = retrieve(IntegerProperty.class, AGE_PROPERTY_ID);
            StringProperty nameProperty = retrieve(StringProperty.class, NAME_PROPERTY_ID);

            PropertyRepository propertyRepository = new PropertyRepository();
            propertyRepository.setId(PROPERTY_REPOSITORY_ID);
            save(propertyRepository);
            logger.info("Created: {}", propertyRepository);

            propertyRepository.getProperties().add(ageProperty);
            propertyRepository.getProperties().add(nameProperty);
            update(propertyRepository);
            logger.info("Updated: {}", propertyRepository);
        });
    }

    @Test
    @Order(3)
    void verifyRepository() {
        logger.info("Verifying repository {} has two items", PROPERTY_REPOSITORY_ID);
        PropertyRepository propertyRepository = retrieve(PropertyRepository.class, PROPERTY_REPOSITORY_ID);
        assertThat(propertyRepository.getProperties()).satisfiesExactlyInAnyOrder(
                age -> {
                    assertThat(age).isInstanceOf(IntegerProperty.class);
                    assertThat(age.getId()).isEqualTo(AGE_PROPERTY_ID);
                    assertThat(age.getName()).isEqualTo("age");
                    assertThat(age.getValue()).isEqualTo(23);
                },
                name -> {
                    assertThat(name).isInstanceOf(StringProperty.class);
                    assertThat(name.getId()).isEqualTo(NAME_PROPERTY_ID);
                    assertThat(name.getName()).isEqualTo("name");
                    assertThat(name.getValue()).isEqualTo("John Doe");
                }
        );
    }

    @Test
    @Order(4)
    void deleteProperty() {
        assertDoesNotThrow(() -> {
            logger.info("Deleting: StringProperty {}", NAME_PROPERTY_ID);
            delete(StringProperty.class, NAME_PROPERTY_ID);
        });
    }

    @Test
    @Order(5)
    void verifyPropertyDeleted() {
        StringProperty deletedProperty = retrieve(StringProperty.class, NAME_PROPERTY_ID);
        assertThat(deletedProperty).isNull();
    }

    @Test
    @Order(6)
    void verifyPropertyRepositoryAgain() {
        logger.info("Verifying repository {} has only one item", 1L);
        PropertyRepository propertyRepository = retrieve(PropertyRepository.class, PROPERTY_REPOSITORY_ID);
        assertThat(propertyRepository.getProperties()).satisfiesExactlyInAnyOrder(
                age -> {
                    assertThat(age).isInstanceOf(IntegerProperty.class);
                    assertThat(age.getId()).isEqualTo(AGE_PROPERTY_ID);
                    assertThat(age.getName()).isEqualTo("age");
                    assertThat(age.getValue()).isEqualTo(23);
                }
        );
    }
}
