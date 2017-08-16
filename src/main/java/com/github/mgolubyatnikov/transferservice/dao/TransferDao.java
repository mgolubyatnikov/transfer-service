package com.github.mgolubyatnikov.transferservice.dao;

import com.github.mgolubyatnikov.transferservice.domain.Transfer;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

@RegisterMapper(TransferMapper.class)
public interface TransferDao {

    @SqlQuery("select * from transfers")
    List<Transfer> findAll();

    @SqlQuery("select * from transfers where id = :id")
    Transfer findById(@Bind("id") Long id);

    @GetGeneratedKeys
    @SqlUpdate("insert into transfers (sourceAccountId, destinationAccountId, amount)" +
            " values (:sourceAccountId, :destinationAccountId, :amount)")
    Long insert(@BindBean Transfer transfer);
}
