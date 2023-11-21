package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketPurchaseRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public final class TicketServiceImpl implements TicketService {

    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    public TicketServiceImpl(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    /**
     * Should only have private methods other than the one below.
     */
    @Override public void purchaseTickets(TicketPurchaseRequest ticketPurchaseRequest) throws InvalidPurchaseException {
        //Reject any invalid ticket purchase requests, throwing an InvalidPurchaseException
        ticketPurchaseRequest.validateRequest();

        //Calculate the correct amount for the requested tickets and make a payment request to the TicketPaymentService
        long accountId = ticketPurchaseRequest.getAccountId();
        int totalPrice = ticketPurchaseRequest.getTotalPrice();
        ticketPaymentService.makePayment(accountId, totalPrice);

        //Calculate the correct no of seats to reserve and make a seat reservation request to the SeatReservationService
        int totalSeats = ticketPurchaseRequest.getTotalSeatsRequired();
        seatReservationService.reserveSeat(accountId, totalSeats);
    }
}
