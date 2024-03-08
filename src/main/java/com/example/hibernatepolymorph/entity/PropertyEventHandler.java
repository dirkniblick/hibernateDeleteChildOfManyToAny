package com.example.hibernatepolymorph.entity;

import org.hibernate.FlushMode;
import org.hibernate.event.spi.EventSource;


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
