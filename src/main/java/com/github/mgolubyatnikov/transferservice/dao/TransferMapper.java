package com.github.mgolubyatnikov.transferservice.dao;

import com.github.mgolubyatnikov.transferservice.domain.Transfer;
import org.skife.jdbi.v2.BeanMapper;

public class TransferMapper extends BeanMapper<Transfer> {
    public TransferMapper() {
        super(Transfer.class);
    }
}
