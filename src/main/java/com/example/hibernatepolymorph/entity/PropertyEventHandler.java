package com.example.hibernatepolymorph.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.PreDeleteEvent;


public class PropertyEventHandler {

    private static final Logger logger = LogManager.getLogger(PropertyEventHandler.class);


    public Boolean preDelete(Property<?> property, PreDeleteEvent event) {
        removeFromPropertyHolder(property, event.getSession());
        removeFromPropertyRepository(property, event.getSession());
        return false;
    }

    protected void removeFromPropertyHolder(Property<?> property, EventSource eventSource) {
        logger.info("Remove from PropertyHolders {}", property);
        eventSource.createNativeMutationQuery("""
                           UPDATE property_holder
                              SET property_type = NULL
                                , property_id = NULL
                            WHERE property_id = :property_id
                           """)
                   .setParameter("property_id", property.getId())
                   .setHibernateFlushMode(FlushMode.MANUAL)
                   .executeUpdate();
    }

    protected void removeFromPropertyRepository(Property<?> property, EventSource event) {
        logger.info("Remove from PropertyRepository {}", property);
        event.createNativeMutationQuery("""
                     DELETE FROM repository_properties
                      WHERE property_id = :property_id
                        AND property_type = :property_type
                     """)
             .setParameter("property_id", property.getId())
             .setParameter("property_type", property.getDiscriminator())
             .setHibernateFlushMode(FlushMode.MANUAL)
             .executeUpdate();
    }
}
