package com.github.mgolubyatnikov.transferservice.dao;

import com.github.mgolubyatnikov.transferservice.domain.Account;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

@RegisterMapper(AccountMapper.class)
public interface AccountDao {

    @SqlQuery("select * from accounts")
    List<Account> findAll();

    @SqlQuery("select * from accounts where id = :id")
    Account findById(@Bind("id") Long id);

    @GetGeneratedKeys
    @SqlUpdate("insert into accounts (balance, version) values (:balance, :version)")
    Long insert(@BindBean Account account);

    @SqlUpdate("update accounts set balance = :balance, version = version + 1 where id = :id and version = :version")
    Integer update(@BindBean Account account);
}
