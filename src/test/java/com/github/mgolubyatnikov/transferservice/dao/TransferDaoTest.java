package com.github.mgolubyatnikov.transferservice.dao;

import com.github.mgolubyatnikov.transferservice.domain.Transfer;
import com.github.mgolubyatnikov.transferservice.util.BigDecimalUtil;
import io.dropwizard.testing.FixtureHelpers;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.skife.jdbi.v2.Handle;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TransferDaoTest {

    @ClassRule
    public static final DaoTestRule RULE = new DaoTestRule();

    private TransferDao transferDao;
    private Handle handle;

    @Before
    public void setUp() {
        transferDao = RULE.getDbi().onDemand(TransferDao.class);
        handle = RULE.getHandle();
        handle.begin();
        handle.createScript(FixtureHelpers.fixture("sample/account.sql")).execute();
        handle.createScript(FixtureHelpers.fixture("sample/transfer.sql")).execute();
        handle.commit();
    }

    @After
    public void tearDown() {
        handle.begin();
        handle.execute("delete from transfers");
        handle.execute("delete from accounts");
        handle.commit();
    }

    @Test
    public void findAll() throws Exception {
        int expectedTransfersNumber = 2;
        List<Transfer> transfers = transferDao.findAll();
        assertNotNull(transfers);
        assertEquals(expectedTransfersNumber, transfers.size());
    }

    @Test
    public void findById() throws Exception {
        Long transferId = 2L;
        Long expectedSourceAccountId = 2L;
        Long expectedDestinationAccountId = 1L;
        BigDecimal expectedAmount = BigDecimalUtil.valueOf("560");

        Transfer transfer = transferDao.findById(transferId);

        assertNotNull(transfer);
        assertEquals(transferId, transfer.getId());
        assertEquals(expectedSourceAccountId, transfer.getSourceAccountId());
        assertEquals(expectedDestinationAccountId, transfer.getDestinationAccountId());
        assertEquals(expectedAmount, transfer.getAmount());
    }

    @Test
    public void insert() throws Exception {
        Transfer transfer = createTransfer();
        Long transferId = transferDao.insert(transfer);

        assertNotNull(transferId);

        List<Map<String, Object>> rows = handle.select(
                "select * from transfers where id = ?", transferId);

        assertEquals(rows.size(), 1);

        Map<String, Object> insertedRow = rows.get(0);

        assertEquals(transfer.getSourceAccountId(), insertedRow.get("sourceAccountId"));
        assertEquals(transfer.getDestinationAccountId(), insertedRow.get("destinationAccountId"));
        assertEquals(transfer.getAmount(), insertedRow.get("amount"));
    }

    private Transfer createTransfer() {
        Long sourceAccountId = 1L;
        Long destinationAccountId = 2L;
        BigDecimal amount = BigDecimalUtil.valueOf("10");
        return new Transfer(sourceAccountId, destinationAccountId, amount);
    }
}