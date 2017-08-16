package com.github.mgolubyatnikov.transferservice.dao;

import com.github.mgolubyatnikov.transferservice.domain.Account;
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

public class AccountDaoTest {

    @ClassRule
    public static final DaoTestRule RULE = new DaoTestRule();

    private AccountDao accountDao;
    private Handle handle;

    @Before
    public void setUp() {
        accountDao = RULE.getDbi().onDemand(AccountDao.class);
        handle = RULE.getHandle();
        handle.begin();
        handle.createScript(FixtureHelpers.fixture("sample/account.sql")).execute();
        handle.commit();
    }

    @After
    public void tearDown() {
        handle.begin();
        handle.execute("delete from accounts");
        handle.commit();
    }

    @Test
    public void findAll() throws Exception {
        int expectedAccountsNumber = 2;
        List<Account> accounts = accountDao.findAll();
        assertNotNull(accounts);
        assertEquals(expectedAccountsNumber, accounts.size());
    }

    @Test
    public void findByNumber() throws Exception {
        Long accountId = 2L;
        BigDecimal expectedBalance = BigDecimalUtil.valueOf("7000195.13");
        int expectedVersion = 1;

        Account account = accountDao.findById(accountId);

        assertNotNull(account);
        assertEquals(accountId, account.getId());
        assertEquals(expectedBalance, account.getBalance());
        assertEquals(expectedVersion, account.getVersion());
    }

    @Test
    public void insert() throws Exception {
        Account account = new Account(BigDecimalUtil.valueOf("1045.99"));
        long accountId = accountDao.insert(account);

        List<Map<String, Object>> rows = handle.select(
                "select * from accounts where id = ?", accountId);

        assertEquals(rows.size(), 1);

        Map<String, Object> insertedRow = rows.get(0);

        assertEquals(account.getBalance(), insertedRow.get("balance"));
        assertEquals(account.getVersion(), insertedRow.get("version"));
    }

    @Test
    public void update() throws Exception {
        Long accountId = 2L;
        BigDecimal newBalance = BigDecimalUtil.valueOf("1000.99");

        Account account = accountDao.findById(accountId);
        account.setBalance(newBalance);
        int count = accountDao.update(account);

        assertEquals(1, count);

        Account updated = accountDao.findById(accountId);

        assertNotNull(updated);
        assertEquals(newBalance, updated.getBalance());
        assertEquals(account.getVersion() + 1, updated.getVersion());
    }

    @Test
    public void updateWithWrongVersion() throws Exception {
        Long accountId = 2L;

        Account account = accountDao.findById(accountId);
        account.setVersion(account.getVersion() + 1);

        int count = accountDao.update(account);

        assertEquals(0, count);
    }
}
