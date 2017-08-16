package com.github.mgolubyatnikov.transferservice.dao;

import com.github.mgolubyatnikov.transferservice.domain.Account;
import org.skife.jdbi.v2.BeanMapper;

public class AccountMapper extends BeanMapper<Account> {
    public AccountMapper() {
        super(Account.class);
    }
}
