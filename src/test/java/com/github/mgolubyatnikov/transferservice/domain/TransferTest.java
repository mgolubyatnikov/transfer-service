package com.github.mgolubyatnikov.transferservice.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mgolubyatnikov.transferservice.util.BigDecimalUtil;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.junit.Assert.assertEquals;

public class TransferTest {

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    private static final String FIXTURE_TRANSFER_JSON = "fixtures/transfer.json";

    private Transfer getFixtureTransfer() {
        Transfer transfer = new Transfer(1L, 2L, BigDecimalUtil.valueOf("35.99"));
        transfer.setId(1L);
        return transfer;
    }

    @Test
    public void serializesToJson() throws Exception {
        final Transfer transfer = getFixtureTransfer();

        final String expected = MAPPER.writeValueAsString(
                MAPPER.readValue(fixture("fixtures/transfer.json"), Transfer.class));

        assertEquals(expected, MAPPER.writeValueAsString(transfer));
    }

    @Test
    public void deserializesFromJson() throws Exception {
        final Transfer transfer = getFixtureTransfer();

        assertEquals(MAPPER.readValue(fixture(FIXTURE_TRANSFER_JSON), Transfer.class), transfer);
    }
}