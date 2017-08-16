package com.github.mgolubyatnikov.transferservice.resources;

import com.github.mgolubyatnikov.transferservice.domain.Transfer;
import com.github.mgolubyatnikov.transferservice.services.ServiceException;
import com.github.mgolubyatnikov.transferservice.services.TransferService;
import io.dropwizard.jersey.params.LongParam;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/transfers")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public class TransferResource {

    private final TransferService transferService;

    public TransferResource(TransferService transferService) {
        this.transferService = transferService;
    }

    @GET
    public List<Transfer> getTransfers() {
        return transferService.getTransfers();
    }

    @GET
    @Path("/{id}")
    public Transfer getTransfer(@PathParam("id") LongParam id) {
        Transfer transfer = transferService.getTransfer(id.get());
        if (transfer == null) {
            throw new WebApplicationException("Transfer not found", Response.Status.NOT_FOUND);
        }
        return transfer;
    }

    @POST
    public Transfer createTransfer(@NotNull @Valid Transfer transfer) {
        try {
            transfer = transferService.createTransfer(transfer);
        } catch (ServiceException e) {
            throw new WebApplicationException(e.getMessage(), Response.Status.BAD_REQUEST);
        }
        return transfer;
    }
}
