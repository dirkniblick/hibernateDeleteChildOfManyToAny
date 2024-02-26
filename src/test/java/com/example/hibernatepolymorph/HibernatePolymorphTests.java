package com.example.hibernatepolymorph;

import com.example.hibernatepolymorph.entity.IntegerProperty;
import com.example.hibernatepolymorph.entity.PropertyHolder;
import com.example.hibernatepolymorph.entity.PropertyRepository;
import com.example.hibernatepolymorph.entity.StringProperty;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HibernatePolymorphTests {

    static EntityManagerFactory entityManagerFactory;
    static EntityManager entityManager;

    @BeforeAll
    static void beforeAll() {
        entityManagerFactory = Persistence.createEntityManagerFactory("LOCALDB");
        entityManager = entityManagerFactory.createEntityManager();
    }

    @AfterAll
    static void afterAll() {
        entityManager.close();
        entityManagerFactory.close();
    }

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
    void deletePropertyOwnedByHolder() {
        System.out.printf("Deleting: StringProperty %d%n", 2L);
        delete(StringProperty.class, 1L);

        System.out.printf("Verifying PropertyHolder %d has no child%n", 1L);
        PropertyHolder propertyHolder = retrieve(PropertyHolder.class, 1L);
        assertThat(propertyHolder.getProperty()).isNull();
    }

    @Test
    @Order(4)
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
    @Order(5)
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
    @Order(6)
    void deletePropertyFromRepository() {
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


    <T> T retrieve(Class<T> entityType, Object id) {
        EntityManager newEntityManager = entityManagerFactory.createEntityManager();
        return newEntityManager.find(entityType, id);
    }

    void save(Object object) {
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        entityManager.persist(object);
        transaction.commit();
    }

    void update(Object object) {
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        entityManager.clear();
        entityManager.merge(object);
        transaction.commit();
    }

    <T> void delete(Class<T> entityType, Object id) {
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        T object = entityManager.find(entityType, id);
        entityManager.remove(object);
        transaction.commit();
    }
}
