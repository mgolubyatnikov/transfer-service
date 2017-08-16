package com.github.mgolubyatnikov.transferservice.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mgolubyatnikov.transferservice.util.BigDecimalUtil;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.junit.Assert.assertEquals;

public class AccountTest {

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    private static final String FIXTURE_ACCOUNT_JSON = "fixtures/account.json";

    private Account getFixtureAccount() {
        Account account = new Account(BigDecimalUtil.valueOf("1500.50"));
        account.setId(1L);
        return account;
    }

    @Test
    public void serializesToJson() throws Exception {
        final Account account = getFixtureAccount();

        final String expected = MAPPER.writeValueAsString(
                MAPPER.readValue(fixture(FIXTURE_ACCOUNT_JSON), Account.class));

        assertEquals(expected, MAPPER.writeValueAsString(account));
    }

    @Test
    public void deserializesFromJson() throws Exception {
        final Account account = getFixtureAccount();

        assertEquals(MAPPER.readValue(fixture(FIXTURE_ACCOUNT_JSON), Account.class), account);
    }
}