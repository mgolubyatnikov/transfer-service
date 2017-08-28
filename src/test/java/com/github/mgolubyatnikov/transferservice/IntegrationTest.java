package com.github.mgolubyatnikov.transferservice;

import com.github.mgolubyatnikov.transferservice.domain.Account;
import com.github.mgolubyatnikov.transferservice.domain.Transfer;
import com.github.mgolubyatnikov.transferservice.util.BigDecimalUtil;
import io.dropwizard.jersey.errors.ErrorMessage;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class IntegrationTest {

    @ClassRule
    public static final DropwizardAppRule<TransferServiceConfigurataion> RULE =
            new DropwizardAppRule<>(TransferServiceApplication.class, "config.yml");

    private static final String ACCOUNTS_PATH = "/accounts";
    private static final String TRANSFERS_PATH = "/transfers";
    private static final String ID_1 = "1";
    private static final String ID_2 = "2";

    private static TransferServiceApplication app;
    private static Client client;

    private static WebTarget accountsWebTarget;
    private static WebTarget transfersWebTarget;

    @BeforeClass
    public static void setUpClass() {
        app = RULE.getApplication();
        client = RULE.client();
        String targetUri = "http://localhost:" + RULE.getLocalPort();
        accountsWebTarget = client.target(targetUri + ACCOUNTS_PATH);
        transfersWebTarget = client.target(targetUri + TRANSFERS_PATH);
    }

    @AfterClass
    public static void tearDownClass() {
        client.close();
    }

    @Before
    public void setUp() {
        app.cleanDatabase();
        app.loadSampleDatabase();
    }

    @Test
    public void getTransfers() {
        final List<Transfer> transfers = transfersWebTarget.request()
                .get(new GenericType<List<Transfer>>() {
                });

        assertNotNull(transfers);
        assertFalse(transfers.isEmpty());
        assertEquals(2, transfers.size());
    }

    @Test
    public void getTransfer() {
        final Transfer transfer = transfersWebTarget.path(ID_1).request().get(Transfer.class);

        assertNotNull(transfer);
        assertEquals(Long.valueOf(1L), transfer.getId());
        assertEquals(Long.valueOf(1L), transfer.getSourceAccountId());
        assertEquals(Long.valueOf(2L), transfer.getDestinationAccountId());
        assertEquals(BigDecimalUtil.valueOf("1350.40"), transfer.getAmount());
    }

    @Test
    public void getTransferNotFound() {
        Response response = transfersWebTarget.path("35").request().get();

        assertNotNull(response);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void postTransfer() {
        final Transfer transfer = new Transfer(1L, 2L, BigDecimalUtil.valueOf("5360.99"));

        Transfer newTransfer = transfersWebTarget.request()
                .post(Entity.entity(transfer, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(Transfer.class);

        assertNotNull(newTransfer);
        assertNotNull(newTransfer.getId());
        assertEquals(transfer.getSourceAccountId(), newTransfer.getSourceAccountId());
        assertEquals(transfer.getDestinationAccountId(), newTransfer.getDestinationAccountId());
        assertEquals(transfer.getAmount(), newTransfer.getAmount());
    }

    @Test
    public void postTransfer_AccountBalancesUpdated() {
        final BigDecimal transferAmount = BigDecimalUtil.valueOf("99.99");
        final Transfer transfer = new Transfer(1L, 2L, transferAmount);

        Account account1 = accountsWebTarget.path(ID_1).request().get(Account.class);
        Account account2 = accountsWebTarget.path(ID_2).request().get(Account.class);

        transfersWebTarget.request().post(Entity.entity(transfer, MediaType.APPLICATION_JSON_TYPE));

        Account updatedAccount1 = accountsWebTarget.path(ID_1).request().get(Account.class);
        Account updatedAccount2 = accountsWebTarget.path(ID_2).request().get(Account.class);

        assertEquals(account1.getBalance().subtract(transferAmount), updatedAccount1.getBalance());
        assertEquals(account2.getBalance().add(transferAmount), updatedAccount2.getBalance());
    }

    @Test
    public void postTransfer_AccountNotFound() {
        final BigDecimal transferAmount = BigDecimalUtil.valueOf("99.99");
        final Transfer transfer = new Transfer(1L, 99L, transferAmount);

        Response response = transfersWebTarget.request()
                .post(Entity.entity(transfer, MediaType.APPLICATION_JSON_TYPE));

        assertNotNull(response);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ErrorMessage errorMessage = response.readEntity(ErrorMessage.class);
        assertTrue(errorMessage.getMessage().contains("Account does not exist"));
    }

    @Test
    public void postTransfer_InsufficientFunds() {
        final BigDecimal transferAmount = BigDecimalUtil.valueOf("9999999999999999.99");
        final Transfer transfer = new Transfer(1L, 2L, transferAmount);

        Response response = transfersWebTarget.request()
                .post(Entity.entity(transfer, MediaType.APPLICATION_JSON_TYPE));

        assertNotNull(response);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ErrorMessage errorMessage = response.readEntity(ErrorMessage.class);
        assertTrue(errorMessage.getMessage().contains("Account has insufficient funds"));
    }

    @Test
    public void postTransfer_AccountBalancesUpdated_Concurrent() throws InterruptedException {
        int accountsNumber = 10;

        List<Account> accounts = new ArrayList<>();
        for (int i = 0; i < accountsNumber; i++) {
            BigDecimal randomBalance = BigDecimal.valueOf(Math.random() * 10000)
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
            final Account account = new Account(randomBalance);

            Account newAccount = accountsWebTarget.request()
                    .post(Entity.entity(account, MediaType.APPLICATION_JSON_TYPE))
                    .readEntity(Account.class);
            accounts.add(newAccount);
        }

        BigDecimal balancesSum = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal::add).get();

        Random random = new Random();

        ExecutorService executorService = Executors.newFixedThreadPool(30);
        for (int i = 0; i < 500; i++) {
            executorService.submit(() -> {
                BigDecimal randomAmount = BigDecimal.valueOf(Math.random() * 100)
                        .setScale(2, BigDecimal.ROUND_HALF_UP);

                int sourceAccountId = random.nextInt(accountsNumber);
                int destinationAccountId = random.nextInt(accountsNumber);
                if (sourceAccountId == destinationAccountId) {
                    return;
                }

                Account sourceAccount = accounts.get(sourceAccountId);
                Account destinationAccount = accounts.get(destinationAccountId);

                final Transfer transfer = new Transfer(sourceAccount.getId(), destinationAccount.getId(), randomAmount);
                transfersWebTarget.request()
                        .post(Entity.entity(transfer, MediaType.APPLICATION_JSON_TYPE));
            });
        }
        executorService.shutdown();
        boolean terminated = executorService.awaitTermination(120, TimeUnit.SECONDS);
        assertTrue("awaitTermination timeout", terminated);

        List<Account> updatedAccounts = new ArrayList<>();
        for (Account account : accounts) {
            updatedAccounts.add(accountsWebTarget.path(String.valueOf(account.getId())).request().get(Account.class));
        }

        BigDecimal newBalancesSum = updatedAccounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal::add).get();

        assertEquals(newBalancesSum, balancesSum);
    }

    @Test
    public void getAccounts() {
        final List<Account> accounts = accountsWebTarget.request()
                .get(new GenericType<List<Account>>() {
                });

        assertNotNull(accounts);
        assertFalse(accounts.isEmpty());
        assertEquals(2, accounts.size());
    }

    @Test
    public void getAccount() {
        Account account = accountsWebTarget.path(ID_2).request().get(Account.class);

        assertNotNull(account);
        assertEquals(Long.valueOf(2L), account.getId());
        assertEquals(BigDecimalUtil.valueOf("7000195.13"), account.getBalance());
    }

    @Test
    public void getAccountNotFound() {
        Response response = accountsWebTarget.path("35").request().get();

        assertNotNull(response);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void postAccount() {
        final Account account = new Account(BigDecimalUtil.valueOf("100.00"));

        Account newAccount = accountsWebTarget.request()
                .post(Entity.entity(account, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(Account.class);

        assertNotNull(newAccount);
        assertNotNull(newAccount.getId());
        assertEquals(account.getBalance(), newAccount.getBalance());
    }
}
