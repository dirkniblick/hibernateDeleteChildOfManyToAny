package com.example.hibernatepolymorph;

import com.example.hibernatepolymorph.entity.IntegerProperty;
import com.example.hibernatepolymorph.entity.PropertyHolder;
import com.example.hibernatepolymorph.entity.StringProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


class PropertyHolderTests extends HibernateTest {

    private static final Logger logger = LogManager.getLogger(PropertyHolderTests.class);

    private static final long AGE_PROPERTY_ID = 30L;
    private static final long NAME_PROPERTY_ID = 31L;
    private static final long PROPERTY_HOLDER_ID = 32L;


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
    void createPropertyHolder() {
        assertDoesNotThrow(() -> {
            StringProperty nameProperty = retrieve(StringProperty.class, NAME_PROPERTY_ID);

            PropertyHolder namePropertyHolder = new PropertyHolder();
            namePropertyHolder.setId(PROPERTY_HOLDER_ID);
            save(namePropertyHolder);
            logger.info("Created: {}", namePropertyHolder);

            namePropertyHolder.setProperty(nameProperty);
            update(namePropertyHolder);
            logger.info("Updated: {}", namePropertyHolder);
        });
    }

    @Test
    @Order(3)
    void verifyPropertyHolder() {
        logger.info("Verifying holder {} has item", PROPERTY_HOLDER_ID);
        PropertyHolder propertyHolder = retrieve(PropertyHolder.class, PROPERTY_HOLDER_ID);
        logger.info("Retrieved: {}", propertyHolder);

        assertThat(propertyHolder.getProperty()).satisfies(p -> {
            assertThat(p.getId()).isEqualTo(NAME_PROPERTY_ID);
            assertThat(p.getName()).isEqualTo("name");
            assertThat(p.getValue()).isEqualTo("John Doe");
        });
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
    void verifyPropertyHolderAgain() {
        logger.info("Verifying PropertyHolder {} has no child", PROPERTY_HOLDER_ID);
        PropertyHolder propertyHolder = retrieve(PropertyHolder.class, PROPERTY_HOLDER_ID);
        assertThat(propertyHolder.getProperty()).isNull();
    }
}
