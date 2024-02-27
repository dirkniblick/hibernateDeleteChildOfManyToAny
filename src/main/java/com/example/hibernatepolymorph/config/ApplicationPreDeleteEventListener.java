package com.example.hibernatepolymorph.config;

import com.example.hibernatepolymorph.entity.Property;
import com.example.hibernatepolymorph.entity.PropertyEventHandler;
import org.hibernate.event.spi.PreDeleteEvent;
import org.hibernate.event.spi.PreDeleteEventListener;


public class ApplicationPreDeleteEventListener implements PreDeleteEventListener {

    public static final ApplicationPreDeleteEventListener INSTANCE = new ApplicationPreDeleteEventListener();

    private final PropertyEventHandler propertyEventHandler = new PropertyEventHandler();


    @Override
    public boolean onPreDelete(PreDeleteEvent event) {
        Object entity = event.getEntity();

        if (entity instanceof Property<?> property) {
            return propertyEventHandler.preDelete(property, event);

        } else {
            return false;
        }
    }
}
