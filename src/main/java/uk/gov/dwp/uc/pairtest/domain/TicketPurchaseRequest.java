package uk.gov.dwp.uc.pairtest.domain;

import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;

import static uk.gov.dwp.uc.pairtest.domain.TicketRequest.Type.ADULT;
import static uk.gov.dwp.uc.pairtest.domain.TicketRequest.Type.INFANT;
import static uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.ErrorCode.*;

/**
 * Should be an Immutable Object
 */
public final class TicketPurchaseRequest {

    private static final int MAX_TICKETS_PER_REQUEST = 20;

    private final long accountId;
    private final TicketRequest[] ticketRequests;

    public TicketPurchaseRequest(long accountId, TicketRequest[] ticketRequests) {
        this.accountId = accountId;
        this.ticketRequests = ticketRequests;
    }

    public long getAccountId() {
        return accountId;
    }

    public TicketRequest[] getTicketTypeRequests() {
        return ticketRequests;
    }

    public int getTotalTicketsRequested() {
        return getTotalTicketsRequested(null);
    }
    public int getTotalTicketsRequested(TicketRequest.Type type) {
        return Arrays.stream(ticketRequests)
                .filter(it -> type == null || it.getTicketType() == type)
                .mapToInt(TicketRequest::getNoOfTickets)
                .sum();
    }

    public int getTotalPrice() {
        int total = 0;
        for (TicketRequest ticketRequest : ticketRequests) {
            total += ticketRequest.getPrice();
        }
        return total;
    }

    public int getTotalSeatsRequired() {
        int total = 0;
        for (TicketRequest ticketRequest : ticketRequests) {
            total += ticketRequest.getSeatsRequired();
        }
        return total;
    }

    public void validateRequest() throws InvalidPurchaseException {
        //All accounts with an id greater than zero are valid
        if (accountId <= 0) {
            throw new InvalidPurchaseException(INVALID_ACCOUNT_ID, "Invalid account id: " + accountId);
        }

        //Only a maximum of 20 tickets that can be purchased at a time.
        int totalTicketsRequested = getTotalTicketsRequested();
        if (totalTicketsRequested < 1
          || totalTicketsRequested > MAX_TICKETS_PER_REQUEST) {
            throw new InvalidPurchaseException(INVALID_NUMBER_OF_TICKETS, "Invalid number of tickets requested: " + totalTicketsRequested);
        }

        //Child and Infant tickets cannot be purchased without purchasing an Adult ticket.
        int adultTickets = getTotalTicketsRequested(ADULT);
        if (adultTickets < 1) {
            throw new InvalidPurchaseException(NO_ADULT_TICKETS, "Cannot purchase child or infant tickets without purchasing an adult ticket");
        }

        //Infants do not pay for a ticket and are not allocated a seat. They will be sitting on an Adult's lap.
        // So need to make sure that there are as many adults as there are infants, so that they have a lap to sit on.
        int infantTickets = getTotalTicketsRequested(INFANT);
        if (adultTickets < infantTickets) {
            throw new InvalidPurchaseException(NOT_ENOUGH_ADULT_TICKETS, "Cannot purchase more infant tickets than adult tickets");
        }
    }
}
