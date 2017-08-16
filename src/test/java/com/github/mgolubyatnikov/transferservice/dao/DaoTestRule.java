package com.github.mgolubyatnikov.transferservice.dao;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.FixtureHelpers;
import org.junit.rules.ExternalResource;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DaoTestRule extends ExternalResource {

    private DBI dbi;
    private Handle handle;

    public DBI getDbi() {
        return dbi;
    }

    public Handle getHandle() {
        return handle;
    }

    @Override
    protected void before() throws Throwable {
        dbi = new DBIFactory().build(environmentMock(), dataSourceFactory(), "h2");
        handle = dbi.open();
        initSchema();
    }

    @Override
    protected void after() {
        handle.close();
    }

    private Environment environmentMock() {
        final Environment environment = mock(Environment.class);
        final LifecycleEnvironment lifecycleEnvironment = mock(LifecycleEnvironment.class);
        final HealthCheckRegistry healthChecks = mock(HealthCheckRegistry.class);

        when(environment.lifecycle()).thenReturn(lifecycleEnvironment);
        when(environment.healthChecks()).thenReturn(healthChecks);
        when(environment.metrics()).thenReturn(new MetricRegistry());

        return environment;
    }

    private DataSourceFactory dataSourceFactory() {
        DataSourceFactory factory = new DataSourceFactory();
        factory.setDriverClass("org.h2.Driver");
        factory.setUrl("jdbc:h2:mem:testdb" + System.currentTimeMillis() + ";DB_CLOSE_DELAY=-1");
        factory.setUser("sa");
        factory.setPassword("");
        return factory;
    }

    private void initSchema() {
        handle.begin();
        handle.createScript(FixtureHelpers.fixture("schema.sql")).execute();
        handle.commit();
    }
}
