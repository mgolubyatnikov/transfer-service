package com.github.mgolubyatnikov.transferservice;

import com.github.mgolubyatnikov.transferservice.resources.AccountResource;
import com.github.mgolubyatnikov.transferservice.resources.TransferResource;
import com.github.mgolubyatnikov.transferservice.services.AccountService;
import com.github.mgolubyatnikov.transferservice.services.TransferService;
import com.github.mgolubyatnikov.transferservice.util.ResourceUtil;
import io.dropwizard.Application;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.DBI;

public class TransferServiceApplication extends Application<TransferServiceConfigurataion> {

    private DBI jdbi;

    public static void main(String[] args) throws Exception {
        new TransferServiceApplication().run(args);
    }

    @Override
    public String getName() {
        return "transfer-service";
    }

    @Override
    public void initialize(Bootstrap<TransferServiceConfigurataion> bootstrap) {
        bootstrap.setConfigurationSourceProvider(new ResourceConfigurationSourceProvider());
    }

    @Override
    public void run(TransferServiceConfigurataion config, Environment environment) throws Exception {
        final DBIFactory factory = new DBIFactory();
        jdbi = factory.build(environment, config.getDataSourceFactory(), "postgresql");

        initSchema();
        loadSampleDatabase();

        final AccountService accountService = new AccountService(jdbi);
        final TransferService transferService = new TransferService(jdbi);

        environment.jersey().register(new AccountResource(accountService));
        environment.jersey().register(new TransferResource(transferService));
    }

    private void initSchema() {
        jdbi.useTransaction((handle, status) -> {
            handle.execute(ResourceUtil.toString("schema.sql"));
        });
    }

    public void loadSampleDatabase() {
        jdbi.useTransaction((handle, status) -> {
            handle.execute(ResourceUtil.toString("sample/account.sql"));
            handle.execute(ResourceUtil.toString("sample/transfer.sql"));
        });
    }

    public void cleanDatabase() {
        jdbi.useTransaction((handle, status) -> {
            handle.execute("delete from transfers");
            handle.execute("delete from accounts");
        });
    }
}
