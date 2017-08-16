package com.github.mgolubyatnikov.transferservice.services;

import com.github.mgolubyatnikov.transferservice.dao.AccountDao;
import com.github.mgolubyatnikov.transferservice.dao.TransferDao;
import com.github.mgolubyatnikov.transferservice.domain.Account;
import com.github.mgolubyatnikov.transferservice.domain.Transfer;
import com.github.mgolubyatnikov.transferservice.util.PairLocks;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionCallback;
import org.skife.jdbi.v2.TransactionStatus;
import org.skife.jdbi.v2.exceptions.CallbackFailedException;

import java.util.List;

public class TransferService {

    private final DBI jdbi;
    private final TransferDao transferDao;

    private final PairLocks<Long> pairLocks = new PairLocks<>();

    public TransferService(DBI jdbi) {
        this.jdbi = jdbi;
        transferDao = jdbi.onDemand(TransferDao.class);
    }

    public List<Transfer> getTransfers() {
        return transferDao.findAll();
    }

    public Transfer getTransfer(Long id) {
        return transferDao.findById(id);
    }

    public Transfer createTransfer(Transfer transfer) {
        PairLocks.PairLock accountsLock = pairLocks.get(transfer.getSourceAccountId(),
                transfer.getDestinationAccountId());

        accountsLock.lock();
        try {
            transfer = jdbi.inTransaction(new TransferTransaction(transfer));
        } catch (CallbackFailedException e) {
            throw unwrapCallbackFailedException(e);
        } finally {
            accountsLock.unlock();
        }

        return transfer;
    }

    private static class TransferTransaction implements TransactionCallback<Transfer> {

        private final Transfer transfer;

        public TransferTransaction(Transfer transfer) {
            this.transfer = transfer;
        }

        @Override
        public Transfer inTransaction(Handle handle, TransactionStatus status) throws Exception {
            TransferDao transferDao = handle.attach(TransferDao.class);
            AccountDao accountDao = handle.attach(AccountDao.class);

            Account sourceAccount = accountDao.findById(transfer.getSourceAccountId());
            if (sourceAccount == null) {
                throw new ServiceException("Account does not exist: " + transfer.getSourceAccountId());
            }

            Account destinationAccount = accountDao.findById(transfer.getDestinationAccountId());
            if (destinationAccount == null) {
                throw new ServiceException("Account does not exist: " + transfer.getDestinationAccountId());
            }

            if (sourceAccount.getBalance().compareTo(transfer.getAmount()) < 0) {
                throw new ServiceException("Account has insufficient funds: " + transfer.getSourceAccountId());
            }
            sourceAccount.withdraw(transfer.getAmount());
            destinationAccount.deposit(transfer.getAmount());

            accountDao.update(sourceAccount);
            accountDao.update(destinationAccount);

            Long transferId = transferDao.insert(transfer);

            return transferDao.findById(transferId);
        }
    }

    private static RuntimeException unwrapCallbackFailedException(CallbackFailedException e) {
        Throwable cause = e.getCause();
        if (cause instanceof ServiceException) {
            return (ServiceException) cause;
        } else {
            return new RuntimeException(cause);
        }
    }
}
