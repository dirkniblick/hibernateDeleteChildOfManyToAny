package com.example.hibernatepolymorph;

import com.example.hibernatepolymorph.entity.IntegerProperty;
import com.example.hibernatepolymorph.entity.PropertyHolder;
import com.example.hibernatepolymorph.entity.StringProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class PropertyHolderTests extends HibernateTest {

    private static final Logger logger = LogManager.getLogger(PropertyHolderTests.class);


    @Test
    @Order(1)
    void createPropertyHolder() {
        IntegerProperty ageProperty = new IntegerProperty();
        ageProperty.setId(1L);
        ageProperty.setName("age");
        ageProperty.setValue(23);
        save(ageProperty);
        logger.info("Created: {}", ageProperty);

        StringProperty nameProperty = new StringProperty();
        nameProperty.setId(1L);
        nameProperty.setName("name");
        nameProperty.setValue("John Doe");
        save(nameProperty);
        logger.info("Created: {}", nameProperty);

        PropertyHolder namePropertyHolder = new PropertyHolder();
        namePropertyHolder.setId(1L);
        save(namePropertyHolder);
        logger.info("Created: {}", namePropertyHolder);

        namePropertyHolder.setProperty(nameProperty);
        update(namePropertyHolder);
        logger.info("Updated: {}", namePropertyHolder);

        assertThat(namePropertyHolder.getId()).isNotNull();
    }

    @Test
    @Order(2)
    void verifyPropertyHolder() {
        logger.info("Verifying holder {} has item", 1L);
        PropertyHolder propertyHolder = retrieve(PropertyHolder.class, 1L);
        logger.info("Retrieved: {}", propertyHolder);

        assertThat(propertyHolder.getProperty()).satisfies(p -> {
            assertThat(p.getId()).isEqualTo(1L);
            assertThat(p.getName()).isEqualTo("name");
            assertThat(p.getValue()).isEqualTo("John Doe");
        });
    }

    @Test
    @Order(3)
    void deleteProperty() {
        logger.info("Deleting: StringProperty {}", 1L);
        delete(StringProperty.class, 1L);

        logger.info("Verifying PropertyHolder {} has no child", 1L);
        PropertyHolder propertyHolder = retrieve(PropertyHolder.class, 1L);
        assertThat(propertyHolder.getProperty()).isNull();
    }
}
