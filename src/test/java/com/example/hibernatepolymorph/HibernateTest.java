package com.example.hibernatepolymorph;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class HibernateTest {

    static EntityManagerFactory entityManagerFactory;


    @BeforeAll
    static void beforeAll() {
        entityManagerFactory = Persistence.createEntityManagerFactory("H2DB");
//        entityManagerFactory = Persistence.createEntityManagerFactory("LOCALDB");
    }

    @AfterAll
    static void afterAll() {
        entityManagerFactory.close();
    }


    EntityManager getEntityManager() {
        return entityManagerFactory.createEntityManager();
    }

    <T> T retrieve(Class<T> entityType, Object id) {
        return getEntityManager().find(entityType, id);
    }

    void save(Object object) {
        EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        entityManager.persist(object);
        transaction.commit();
    }

    void update(Object object) {
        EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        entityManager.clear();
        entityManager.merge(object);
        transaction.commit();
    }

    <T> void delete(Class<T> entityType, Object id) {
        EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        T object = entityManager.find(entityType, id);
        System.out.printf("Removing %s %s%n", entityType.getSimpleName(), id);
        entityManager.remove(object);
        System.out.printf("Committing %s %s%n", entityType.getSimpleName(), id);
        transaction.commit();
    }
}
