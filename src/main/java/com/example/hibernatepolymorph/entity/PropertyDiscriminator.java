package com.example.hibernatepolymorph.entity;

import jakarta.persistence.DiscriminatorType;
import org.hibernate.annotations.AnyDiscriminator;
import org.hibernate.annotations.AnyDiscriminatorValue;
import org.hibernate.annotations.AnyKeyJavaClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE})
@AnyKeyJavaClass(Long.class)
@AnyDiscriminator(DiscriminatorType.STRING)
@AnyDiscriminatorValue(discriminator = StringProperty.DISCRIMINATOR, entity = StringProperty.class)
@AnyDiscriminatorValue(discriminator = IntegerProperty.DISCRIMINATOR, entity = IntegerProperty.class)
public @interface PropertyDiscriminator {

}
