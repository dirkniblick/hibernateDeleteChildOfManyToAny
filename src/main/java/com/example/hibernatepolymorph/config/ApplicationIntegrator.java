package com.example.hibernatepolymorph.config;

import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.UnknownKeyFor;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;


public class ApplicationIntegrator implements Integrator {

    @Override
    public void integrate(@UnknownKeyFor @NonNull @Initialized Metadata metadata,
                          @UnknownKeyFor @NonNull @Initialized BootstrapContext bootstrapContext,
                          @UnknownKeyFor @NonNull @Initialized SessionFactoryImplementor sessionFactory) {

        final EventListenerRegistry eventListenerRegistry =
                sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);

        eventListenerRegistry.appendListeners(EventType.PRE_DELETE, new ApplicationPreDeleteEventListener());
    }

    @Override
    public void disintegrate(@UnknownKeyFor @NonNull @Initialized SessionFactoryImplementor sessionFactoryImplementor,
                             @UnknownKeyFor @NonNull @Initialized SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {
        // We HAVE to override this...
    }
}
