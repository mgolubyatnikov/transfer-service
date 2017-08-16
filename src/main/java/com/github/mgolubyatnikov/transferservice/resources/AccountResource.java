package com.github.mgolubyatnikov.transferservice.resources;

import com.github.mgolubyatnikov.transferservice.domain.Account;
import com.github.mgolubyatnikov.transferservice.services.AccountService;
import io.dropwizard.jersey.params.LongParam;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/accounts")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public class AccountResource {

    private final AccountService accountService;

    public AccountResource(AccountService accountService) {
        this.accountService = accountService;
    }

    @GET
    public List<Account> listAccounts() {
        return accountService.getAccounts();
    }

    @GET
    @Path("/{id}")
    public Account getAccount(@PathParam("id") LongParam accountId) {
        Account account = accountService.getAccount(accountId.get());
        if (account == null) {
            throw new WebApplicationException("Account not found", Response.Status.NOT_FOUND);
        }
        return account;
    }

    @POST
    public Account createAccount(@NotNull @Valid Account account) {
        return accountService.createAccount(account);
    }
}
