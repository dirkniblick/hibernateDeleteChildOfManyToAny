package com.example.hibernatepolymorph.config;

import org.hibernate.integrator.spi.Integrator;
import org.hibernate.jpa.boot.spi.IntegratorProvider;

import java.util.List;


public class ApplicationIntegratorProvider implements IntegratorProvider {

    @Override
    public List<Integrator> getIntegrators() {
        return List.of(new ApplicationIntegrator());
    }
}
