# @Any or @ManyToAny Relationships in Hibernate *5*

Here is what changes would need to be made to this project's code for it work with Hibernate 5. (Reverse this when upgrading to Hibernate 6! 😉)

Replace the `@PropertyDiscriminator` meta-annotation with [an `@AnyMetaDef` defined in a `package-info.java`](https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#associations-any-meta-def-example):

```java
@AnyMetaDef(name = "PropertyMetaDef", idType = "long", metaType = "string",
        metaValues = {
                @MetaValue(value = StringProperty.DISCRIMINATOR, targetEntity = StringProperty.class),
                @MetaValue(value = IntegerProperty.DISCRIMINATOR, targetEntity = IntegerProperty.class)
        }
)
package com.example.hibernatepolymorph.entity;

import org.hibernate.annotations.AnyMetaDef;
import org.hibernate.annotations.MetaValue;
```

The declaration of `PropertyHolder#property` would [use the named `@AnyMetaDef`](https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#associations-any-example) instead of `@PropertyDiscriminator`:

```java
@Any(
        metaDef = "PropertyMetaDef",
        metaColumn = @Column(name = "property_type", columnDefinition = "varchar(1) check (property_type in ('S','I'))")
)
@JoinColumn(name = "property_id")
private Property<?> property;
```

[Likewise](https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#associations-many-to-any-example) for the declaration of `PropertyRepository#properties`:

```java
@ManyToAny(
        metaDef = "PropertyMetaDef",
        metaColumn = @Column(name = "property_type", columnDefinition = "varchar(1) not null check (property_type in ('S','I'))")
)
@Cascade(CascadeType.ALL)
@JoinTable(name = "repository_properties",
        joinColumns = @JoinColumn(name = "repository_id"),
        inverseJoinColumns = @JoinColumn(name = "property_id"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"repository_id", "property_id", "property_type"})
)
private List<Property<?>> properties = new ArrayList<>();
```