package com.github.mgolubyatnikov.transferservice.services;

import com.github.mgolubyatnikov.transferservice.dao.AccountDao;
import com.github.mgolubyatnikov.transferservice.domain.Account;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionCallback;
import org.skife.jdbi.v2.TransactionStatus;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.PathParam;
import java.util.List;

public class AccountService {

    private final DBI jdbi;
    private final AccountDao accountDao;

    public AccountService(DBI jdbi) {
        this.jdbi = jdbi;
        accountDao = jdbi.onDemand(AccountDao.class);
    }

    public List<Account> getAccounts() {
        return accountDao.findAll();
    }

    public Account getAccount(@PathParam("id") Long id) {
        return accountDao.findById(id);
    }

    public Account createAccount(Account account) {
        return jdbi.inTransaction(new AccountTransaction(account));
    }

    private static class AccountTransaction implements TransactionCallback<Account> {

        private final Account account;

        public AccountTransaction(Account account) {
            this.account = account;
        }

        @Override
        public Account inTransaction(Handle handle, TransactionStatus status) throws Exception {
            AccountDao accountDao = handle.attach(AccountDao.class);
            Long accountId = accountDao.insert(account);
            return accountDao.findById(accountId);
        }
    }
}
