package com.example.hibernatepolymorph;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class HibernateTest {

    static final EntityManagerFactory entityManagerFactory;


    static {
        entityManagerFactory = Persistence.createEntityManagerFactory("H2DB");
//        entityManagerFactory = Persistence.createEntityManagerFactory("LOCALDB");
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
        entityManager.remove(object);
        transaction.commit();
    }
}
