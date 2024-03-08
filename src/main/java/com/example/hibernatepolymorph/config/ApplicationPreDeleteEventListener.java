package com.example.hibernatepolymorph.config;

import com.example.hibernatepolymorph.entity.Property;
import com.example.hibernatepolymorph.entity.PropertyEventHandler;
import org.hibernate.event.spi.PreDeleteEvent;
import org.hibernate.event.spi.PreDeleteEventListener;


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
