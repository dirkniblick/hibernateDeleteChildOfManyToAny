# Deleting Children @Any or @ManyToAny Relationships in Hibernate 6

👇 See [update](#update-2024-02-29) for solution.

I'm hoping to get some pointers on how to manage the relationships of entities using Hibernate's [`@Any`](https://docs.jboss.org/hibernate/stable/orm/userguide/html_single/Hibernate_User_Guide.html#associations-any) and [`@ManyToAny`](https://docs.jboss.org/hibernate/stable/orm/userguide/html_single/Hibernate_User_Guide.html#associations-many-to-any), specifically when removing related records. I am currently unable to find guidance in the Hibernate docs and have had limited success elsewhere. Simply put, when I delete "child" records, I don't know how to get Hibernate to manage (i.e., clean up after) the owners of those records.

## Examples

Let's start with [a project on Github](https://github.com/dirkniblick/hibernateDeleteChildOfManyToAny) with entities and tests quite similar to those found in the [documentation](https://docs.jboss.org/hibernate/stable/orm/userguide/html_single/Hibernate_User_Guide.html#associations-any). The two test classes in this project create "child" entities, create a "parent" entity for those children, and then attempt to delete the child entity.

### @Any
A [`PropertyHolder`](./src/main/java/com/example/hibernatepolymorph/entity/PropertyHolder.java) can hold any [`Property`](./src/main/java/com/example/hibernatepolymorph/entity/Property.java):

```java
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

    ...
}

```

Let's create a [`StringProperty`](./src/main/java/com/example/hibernatepolymorph/entity/StringProperty.java) and a `PropertyHolder` related to it in [`PropertyHolderTests`](./src/test/java/com/example/hibernatepolymorph/PropertyHolderTests.java) (abridged here):

```java
StringProperty nameProperty = new StringProperty();
nameProperty.setId(NAME_PROPERTY_ID);
nameProperty.setName("name");
nameProperty.setValue("John Doe");
save(nameProperty);

PropertyHolder namePropertyHolder = new PropertyHolder();
namePropertyHolder.setId(PROPERTY_HOLDER_ID);
save(namePropertyHolder);

namePropertyHolder.setProperty(nameProperty);
update(namePropertyHolder);

...

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
    entityManager.merge(object);
    transaction.commit();
}
```

Now, delete the property:

```java
delete(StringProperty.class, NAME_PROPERTY_ID);
```

And verify the entities involved:

```java

StringProperty deletedProperty = retrieve(StringProperty.class, NAME_PROPERTY_ID);
assertThat(deletedProperty).isNull();

PropertyHolder propertyHolder = retrieve(PropertyHolder.class, PROPERTY_HOLDER_ID);
assertThat(propertyHolder.getProperty()).isNull(); // FAILS

...

<T> void delete(Class<T> entityType, Object id) {
    EntityManager entityManager = getEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();
    transaction.begin();
    T object = entityManager.find(entityType, id);
    entityManager.remove(object);
    transaction.commit();
}

<T> T retrieve(Class<T> entityType, Object id) {
    return getEntityManager().find(entityType, id);
}
```

✅ What I expect to happen is `StringProperty` #1 is deleted and `PropertyHolder` #1 to still be viable without its property.

⚠️ What happens is `StringProperty` #1 *is* deleted (i.e., its row is removed from the `string_property` table). However, since Hibernate doesn't update the row for `PropertyHolder` #1 in the `property_holder` table, the `EntityManager` returns `null` because `PropertyHolder` still references the deleted `StringProperty`:

> `HHH015013: Returning null (as required by JPA spec) rather than throwing EntityNotFoundException, as the entity (type=com.example.hibernatepolymorph.entity.PropertyHolder, id=1) does not exist`

### @ManyToAny

A [`PropertyRepository`](./src/main/java/com/example/hibernatepolymorph/entity/PropertyRepository.java) can be associated with any number of [`Property`](./src/main/java/com/example/hibernatepolymorph/entity/Property.java):

```java
@Entity
@Table(name = "property_repository")
public class PropertyRepository {

    @Id
    private Long id;

    @ManyToAny
    @AnyDiscriminator(DiscriminatorType.STRING)
    @Column(name = "property_type")
    @AnyKeyJavaClass(Long.class)
    @AnyDiscriminatorValue(discriminator = "S", entity = StringProperty.class)
    @AnyDiscriminatorValue(discriminator = "I", entity = IntegerProperty.class)
    @Cascade(CascadeType.ALL)
    @JoinTable(name = "repository_properties",
            joinColumns = @JoinColumn(name = "repository_id"),
            inverseJoinColumns = @JoinColumn(name = "property_id")
    )
    private List<Property<?>> properties = new ArrayList<>();

    ...
}
```

Let's create a [`StringProperty`](./src/main/java/com/example/hibernatepolymorph/entity/StringProperty.java) and an [`IntegerProperty`](./src/main/java/com/example/hibernatepolymorph/entity/IntegerProperty.java) followed by a `PropertyRepository` related to them both in [`PropertyRepositoryTests`](./src/test/java/com/example/hibernatepolymorph/PropertyRepositoryTests.java):

```java
IntegerProperty ageProperty = new IntegerProperty();
ageProperty.setId(AGE_PROPERTY_ID);
ageProperty.setName("age");
ageProperty.setValue(23);
save(ageProperty);

StringProperty nameProperty = new StringProperty();
nameProperty.setId(NAME_PROPERTY_ID);
nameProperty.setName("name");
nameProperty.setValue("John Doe");
save(nameProperty);

PropertyRepository propertyRepository = new PropertyRepository();
propertyRepository.setId(PROPERTY_REPOSITORY_ID);
save(propertyRepository);

propertyRepository.getProperties().add(ageProperty);
propertyRepository.getProperties().add(nameProperty);
update(propertyRepository);
```

Now, delete one of the properties:

```java
delete(StringProperty.class, NAME_PROPERTY_ID);
```

And verify the entities involved:

```java
StringProperty deletedProperty = retrieve(StringProperty.class, NAME_PROPERTY_ID);
assertThat(deletedProperty).isNull();

PropertyRepository propertyRepository = retrieve(PropertyRepository.class, 1L); // FAILS
assertThat(propertyRepository.getProperties()).satisfiesExactlyInAnyOrder(
        age -> {
            assertThat(age).isInstanceOf(IntegerProperty.class);
            assertThat(age.getId()).isEqualTo(AGE_PROPERTY_ID);
            assertThat(age.getName()).isEqualTo("age");
            assertThat(age.getValue()).isEqualTo(23);
        }
);
```

✅ What I expect to happen is `IntegerProperty` #2 is deleted and `PropertyRepository` #1 to only be related to `StringProperty` #2.

⚠️ What happens is `IntegerProperty` #2 *is* deleted (i.e., its row is removed from the `integer_property` table). However, the row in `repository_properties` which relates `PropertyRepository` #1 with `IntegerProperty` #2 is *not* deleted. So, the `EntityManager` throws an exception when trying to retrieve `PropertyRepository` #1:

> `jakarta.persistence.EntityNotFoundException: Unable to find com.example.hibernatepolymorph.entity.IntegerProperty with id 2` 

## Solutions?

As I said earlier, I've been unable to find much guidance on how to handle these situations. The Hibernate docs show how to _create_ these relationships and entities, but I didn't find anything about _removing them_ where `@Any` or `@ManyToAny` are used. So, **what is the intended or recommended method for having Hibernate cleanup these relationships after or during a delete?** The only other source I've found is [an article from 2019 on Medium](https://medium.com/@joshuajharkema/spring-boot-hibernate-and-manytoany-orphan-removal-aeb17a457b21) which suggests using a PreDeleteEventListener and an Integrator. Is this still a viable solution?

## Update: 2024-02-29

I was able to piece together a solution for Hibernate to execute some code in the event an entity is deleted.
This was thanks to [the Medium article mentioned above, by Josh Harkema](https://medium.com/@joshuajharkema/spring-boot-hibernate-and-manytoany-orphan-removal-aeb17a457b21), and [a post on Vlad Mihalcea's website](https://vladmihalcea.com/hibernate-event-listeners/):
Create a [`PreDeleteEventListener`](https://docs.jboss.org/hibernate/orm/6.4/javadocs/org/hibernate/event/spi/PreDeleteEventListener.html) whose [`onPreDelete()`](https://docs.jboss.org/hibernate/orm/6.4/javadocs/org/hibernate/event/spi/PreDeleteEventListener.html#onPreDelete(org.hibernate.event.spi.PreDeleteEvent)) method will handle the [`PreDeleteEvent`](https://docs.jboss.org/hibernate/orm/6.4/javadocs/org/hibernate/event/spi/PreDeleteEvent.html) Hibernate issues before deleting an entity.

```java
public class ApplicationPreDeleteEventListener implements PreDeleteEventListener {

    private final PropertyEventHandler propertyEventHandler = new PropertyEventHandler();


    @Override
    public boolean onPreDelete(PreDeleteEvent event) {
        Object entity = event.getEntity();
        boolean veto = entity == null;

        if (! veto && entity instanceof Property<?> property) {
            veto = propertyEventHandler.preDelete(property, event.getSession());
        }

        return veto;
    }
}
```

The `onPreDelete()` method returns a boolean that tells Hibernate whether to veto the original delete that triggered the event.
In this implementation, the [`PropertyEventHandler`](./src/main/java/com/example/hibernatepolymorph/entity/PropertyEventHandler.java) uses the provided `EventSource` to remove relationships from the given `Property` prior to its deletion:

```java
public class PropertyEventHandler {

    public boolean preDelete(Property<?> property, EventSource eventSource) {
        return removeFromPropertyHolders(property, eventSource) ||
                removeFromPropertyRepositories(property, eventSource);
    }

    protected boolean removeFromPropertyHolders(Property<?> property, EventSource eventSource) {
        eventSource.getSession()
                   .createNativeMutationQuery("""
                           UPDATE property_holder
                              SET property_type = NULL
                                , property_id = NULL
                            WHERE property_id = :property_id
                           """)
                   .setParameter("property_id", property.getId())
                   .setHibernateFlushMode(FlushMode.MANUAL)
                   .executeUpdate();
        return false;
    }

    protected boolean removeFromPropertyRepositories(Property<?> property, EventSource eventSource) {
        eventSource.getSession()
                   .createNativeMutationQuery("""
                           DELETE FROM repository_properties
                            WHERE property_id = :property_id
                              AND property_type = :property_type
                           """)
                   .setParameter("property_id", property.getId())
                   .setParameter("property_type", property.getDiscriminator())
                   .setHibernateFlushMode(FlushMode.MANUAL)
                   .executeUpdate();
        return false;
    }
}
```

In order to enable this mechanism in the project, the `ApplicationPreDeleteEventListener` needs to be "[integrated](https://docs.jboss.org/hibernate/stable/orm/userguide/html_single/Hibernate_User_Guide.html#bootstrap-event-listener-registration)" into Hibernate.

ℹ️ There may be [other ways of "integrating" event listeners into Hibernate](https://docs.jboss.org/hibernate/stable/orm/userguide/html_single/Hibernate_User_Guide.html#bootstrap-bootstrap-native-registry-BootstrapServiceRegistry-example), but this one works.

Create an implementation of [`Integrator`](https://docs.jboss.org/hibernate/orm/6.4/javadocs/org/hibernate/integrator/spi/Integrator.html):

```java
public class ApplicationIntegrator implements Integrator {

    @Override
    public void integrate(@UnknownKeyFor @NonNull @Initialized Metadata metadata,
                          @UnknownKeyFor @NonNull @Initialized BootstrapContext bootstrapContext,
                          @UnknownKeyFor @NonNull @Initialized SessionFactoryImplementor sessionFactory) {

        final EventListenerRegistry eventListenerRegistry =
                sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);

        eventListenerRegistry.appendListeners(EventType.PRE_DELETE, ApplicationPreDeleteEventListener.class);
    }

    @Override
    public void disintegrate(@UnknownKeyFor @NonNull @Initialized SessionFactoryImplementor sessionFactoryImplementor,
                             @UnknownKeyFor @NonNull @Initialized SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {
        // We HAVE to override this...
    }
}
```

ℹ️ The `@UnknownKeyFor`, `@NonNull` and `@Initialized` annotations on the `integrate()` method are from the `org.checkerframework:checker-qual` package, which is necessary to create an `Integrator`.

Next, create an implementation of [`IntegratorProvider`](https://docs.jboss.org/hibernate/orm/6.4/javadocs/org/hibernate/jpa/boot/spi/IntegratorProvider.html):

```java
public class ApplicationIntegratorProvider implements IntegratorProvider {

    @Override
    public List<Integrator> getIntegrators() {
        return List.of(new ApplicationIntegrator());
    }
}
```

Finally, tell Hibernate to use the `ApplicationIntegratorProvider`. If using Hibernate alone, append this to the project's `persistence.xml`:

```xml
<persistence ...>
    <persistence-unit ...>
        ...
        <properties>
            ...
            <property name="hibernate.integrator_provider" value="com.example.hibernatepolymorph.config.ApplicationIntegratorProvider"/>
        </properties>
    </persistence-unit>
    ...
</persistence>
```

If using Spring Boot, append this to the `application.properties`:

```properties
spring.jpa.properties.hiberrnate.integrator_provider=package.name.to.ApplicationIntegratorProvider
```

Now, all of the project's tests perform as expected.

### Final notes

This project was built using Hibernate 6.4 and should also contain examples for everything needed to effectively utilize Hibernate's @Any and @ManyToAny relationships. Theoretically, it should work back to 6.0, but I would advise using 6.2.8Final or higher to avoid bugs with [@Any and @ManyToAny associations](https://discourse.hibernate.org/t/any-and-manytoany-associations-fail-in-spring-boot-3/7576/18) and [using meta-annotations](https://discourse.hibernate.org/t/meta-annotation-anydiscriminatorvalue-does-not-work/7242/5).

⚠️ However, if using Hibernate 5, [quite a lot of things need to change](./README_HIBERNATE_5.md).