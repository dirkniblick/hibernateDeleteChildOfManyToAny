# hibernateDeleteChildOfManyToAny

I'm hoping to get some pointers on how to manage the relationships of entities using Hibernate's [@Any](https://docs.jboss.org/hibernate/stable/orm/userguide/html_single/Hibernate_User_Guide.html#associations-any) and [@ManyToAny](https://docs.jboss.org/hibernate/stable/orm/userguide/html_single/Hibernate_User_Guide.html#associations-many-to-any), specifically when removing related records. I am currently unable to find guidance in the Hibernate docs and have had limited success elsewhere. Simply put, when I delete "child" records, I don't know how to get Hibernate to manage (i.e., clean up after) the owners of those records.

## Examples

Let's start with [a project on Github](https://github.com/dirkniblick/hibernateDeleteChildOfManyToAny) with entities and tests quite similar to those found in the documentation referenced above. The two test classes in this project create "child" entities, create a "parent" entity for those children, and then attempt to delete the child entity.

### @Any
A [PropertyHolder](./src/main/java/com/example/hibernatepolymorph/entity/PropertyHolder.java) can hold any [Property](./src/main/java/com/example/hibernatepolymorph/entity/Property.java):

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

Let's create a [StringProperty](./src/main/java/com/example/hibernatepolymorph/entity/StringProperty.java) and a PropertyHolder related to it in [PropertyHolderTests](./src/test/java/com/example/hibernatepolymorph/PropertyHolderTests.java) (abridged here):

```java
StringProperty nameProperty = new StringProperty();
nameProperty.setId(1L);
nameProperty.setName("name");
nameProperty.setValue("John Doe");
save(nameProperty);

PropertyHolder namePropertyHolder = new PropertyHolder();
namePropertyHolder.setId(1L);
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
    entityManager.clear();
    entityManager.merge(object);
    transaction.commit();
}
```

Now, delete the property:

```java
delete(StringProperty.class, 1L);

PropertyHolder propertyHolder = retrieve(PropertyHolder.class, 1L);
assertThat(propertyHolder.getProperty()).isNull(); // FAILS

...

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

<T> T retrieve(Class<T> entityType, Object id) {
    return getEntityManager().find(entityType, id);
}
```

✅ What I expect to happen is StringProperty #1 is deleted and PropertyHolder #1 to still be viable without its property.

⚠️ What happens is StringProperty #1 *is* deleted, i.e., its row is removed from the `string_property` table. But the EntityManager returns null for PropertyHolder #1 because its row in the `property_holder` table still references the deleted property: `HHH015013: Returning null (as required by JPA spec) rather than throwing EntityNotFoundException, as the entity (type=com.example.hibernatepolymorph.entity.PropertyHolder, id=1) does not exist`

### @ManyToAny

A [PropertyRepository](./src/main/java/com/example/hibernatepolymorph/entity/PropertyRepository.java) can be associated with any number of [Properties](./src/main/java/com/example/hibernatepolymorph/entity/Property.java):

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

Let's create a [StringProperty](./src/main/java/com/example/hibernatepolymorph/entity/StringProperty.java) and an [IntegerProperty](./src/main/java/com/example/hibernatepolymorph/entity/IntegerProperty.java) followed by a PropertyRepository related to them both in [PropertyRepositoryTests](./src/test/java/com/example/hibernatepolymorph/PropertyRepositoryTests.java):

```java
IntegerProperty ageProperty = new IntegerProperty();
ageProperty.setId(2L);
ageProperty.setName("age");
ageProperty.setValue(23);
save(ageProperty);

StringProperty nameProperty = new StringProperty();
nameProperty.setId(2L);
nameProperty.setName("name");
nameProperty.setValue("John Doe");
save(nameProperty);

PropertyRepository propertyRepository = new PropertyRepository();
propertyRepository.setId(1L);
save(propertyRepository);

propertyRepository.getProperties().add(ageProperty);
propertyRepository.getProperties().add(nameProperty);
update(propertyRepository);
```

Now, delete one of the properties:

```java
delete(IntegerProperty.class, 2L);

PropertyRepository propertyRepository = retrieve(PropertyRepository.class, 1L); // FAILS
assertThat(propertyRepository.getProperties()).hasSize(1);
```

✅ What I expect to happen is IntegerProperty #2 is deleted and PropertyRepository #1 to only be related to StringProperty #2.

⚠️ What happens is IntegerProperty #2 *is* deleted, i.e., its row is removed from the `integer_property`. But the row in `repository_properties` which relates PropertyRepository #1 with IntegerProperty #2 is *not* deleted. So, the EntityManager throws an exception when trying to retrieve PropertyRepository #1: `jakarta.persistence.EntityNotFoundException: Unable to find com.example.hibernatepolymorph.entity.IntegerProperty with id 2` 

## Solutions?

As I said earlier, I've been unable to find much guidance on how to handle these situations. The Hibernate docs show how to create these relationships and entities, but I didn't find anything specific to @Any or @ManyToAny. So, **what is the intended or suggested method for having Hibernate cleanup these relationships after or during a delete?** The only other source I've found is [an article on Medium years ago](https://medium.com/@joshuajharkema/spring-boot-hibernate-and-manytoany-orphan-removal-aeb17a457b21) which suggests using a PreDeleteEventListener and an Integrator. Is this still a viable solution?
