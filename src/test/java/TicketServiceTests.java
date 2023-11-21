import org.junit.jupiter.api.Test;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.TicketService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketPurchaseRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static uk.gov.dwp.uc.pairtest.domain.TicketRequest.Type.*;
import static uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.ErrorCode.*;

public class TicketServiceTests {

    private final TicketService ticketService;

    /**
     * Use Mockito to create mock versions of the services, as we don't have an implementation of SeatReservationService.
     *  We don't need these to do anything as we are assuming that the services will always do what we ask them
     *  to do without errors.
     */
    public TicketServiceTests() {
        TicketPaymentService ticketPaymentService = mock(TicketPaymentService.class);
        SeatReservationService seatReservationService = mock(SeatReservationService.class);
        ticketService = new TicketServiceImpl(ticketPaymentService, seatReservationService);
    }

    /**
     * All accounts with an id greater than zero are valid
     */
    @Test void testAccountId() {
        //Don't care what tickets are being requested for this test, just need them to be valid
        TicketRequest[] validTicketRequests = new TicketRequest[] {
            new TicketRequest(ADULT, 1)
        };

        //Invalid IDs
        assertInvalid(INVALID_ACCOUNT_ID, new TicketPurchaseRequest(0, validTicketRequests));
        assertInvalid(INVALID_ACCOUNT_ID, new TicketPurchaseRequest(-1, validTicketRequests));
        assertInvalid(INVALID_ACCOUNT_ID, new TicketPurchaseRequest(Long.MIN_VALUE, validTicketRequests));

        //Valid IDs
        assertValid(new TicketPurchaseRequest(1, validTicketRequests));
        assertValid(new TicketPurchaseRequest(Long.MAX_VALUE, validTicketRequests));
    }

    /**
     * Only a maximum of 20 tickets that can be purchased at a time.
     */
    @Test void testNumberOfTickets() {
        //No tickets
        assertInvalid(INVALID_NUMBER_OF_TICKETS, new TicketPurchaseRequest(1, new TicketRequest[0]));

        //Too many tickets
        assertInvalid(INVALID_NUMBER_OF_TICKETS, new TicketPurchaseRequest(1, new TicketRequest[] {
                new TicketRequest(ADULT, 21)
        }));

        //Too many tickets, spread over different types
        assertInvalid(INVALID_NUMBER_OF_TICKETS, new TicketPurchaseRequest(1, new TicketRequest[] {
                new TicketRequest(ADULT, 10),
                new TicketRequest(CHILD, 6),
                new TicketRequest(INFANT, 5)
        }));

        //Max number of tickets, spread over different types
        assertValid(new TicketPurchaseRequest(1, new TicketRequest[] {
                new TicketRequest(ADULT, 10),
                new TicketRequest(CHILD, 5),
                new TicketRequest(INFANT, 5)
        }));

        //Min number of tickets
        assertValid(new TicketPurchaseRequest(1, new TicketRequest[] {
                new TicketRequest(ADULT, 1)
        }));
    }

    /**
     * Child and Infant tickets cannot be purchased without purchasing an Adult ticket.
     */
    @Test void testNoAdultTickets() {
        //Child tickets with no adult tickets
        assertInvalid(NO_ADULT_TICKETS, new TicketPurchaseRequest(1, new TicketRequest[] {
                new TicketRequest(CHILD, 1)
        }));

        //Infant tickets with no adult tickets
        assertInvalid(NO_ADULT_TICKETS, new TicketPurchaseRequest(1, new TicketRequest[] {
                new TicketRequest(INFANT, 1)
        }));

        //Child tickets with adult tickets
        assertValid(new TicketPurchaseRequest(1, new TicketRequest[] {
                new TicketRequest(ADULT, 1),
                new TicketRequest(CHILD, 1)
        }));

        //Infant tickets with adult tickets
        assertValid(new TicketPurchaseRequest(1, new TicketRequest[] {
                new TicketRequest(ADULT, 1),
                new TicketRequest(INFANT, 1)
        }));
    }

    /**
     * Infants do not pay for a ticket and are not allocated a seat. They will be sitting on an Adult's lap.
     *  So need to make sure that there are as many adults as there are infants, so that they have a lap to sit on.
     */
    @Test void testEnoughAdultTickets() {
        //More infants than adults
        assertInvalid(NOT_ENOUGH_ADULT_TICKETS, new TicketPurchaseRequest(1, new TicketRequest[] {
                new TicketRequest(ADULT, 1),
                new TicketRequest(INFANT, 2)
        }));

        //Same infants and adults
        assertValid(new TicketPurchaseRequest(1, new TicketRequest[] {
                new TicketRequest(ADULT, 1),
                new TicketRequest(INFANT, 1)
        }));

        //More adults than infants
        assertValid(new TicketPurchaseRequest(1, new TicketRequest[] {
                new TicketRequest(ADULT, 2),
                new TicketRequest(INFANT, 1)
        }));
    }

    /**
     * Checks that an {@link InvalidPurchaseException} is thrown with the correct error code when purchasing
     *  tickets for the given request
     */
    private void assertInvalid(InvalidPurchaseException.ErrorCode expectedErrorCode, TicketPurchaseRequest request) {
        InvalidPurchaseException e =
                assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(request));

        assertEquals(expectedErrorCode, e.getErrorCode());
    }

    /**
     * Checks that no exceptions are thrown when purchasing tickets for the given request
     */
    private void assertValid(TicketPurchaseRequest request) {
        ticketService.purchaseTickets(request);
    }

    /**
     * Same test cases for the 3 totals methods, so might as well group them together
     */
    @Test void testTotals() {
        //Min adult tickets possible
        assertTotals(1, 20, 1, 1, 0, new TicketRequest[] {
                new TicketRequest(ADULT, 1)
        });

        //Min child tickets possible
        assertTotals(2, 30, 2, 1, 1, new TicketRequest[] {
                new TicketRequest(ADULT, 1),
                new TicketRequest(CHILD, 1)
        });

        //Min infant tickets possible
        assertTotals(2, 20, 1, 1, 0, new TicketRequest[] {
                new TicketRequest(ADULT, 1),
                new TicketRequest(INFANT, 1)
        });

        //Max adult tickets possible
        assertTotals(20, 400, 20, 20, 0, new TicketRequest[] {
                new TicketRequest(ADULT, 20)
        });

        //Max child tickets possible
        assertTotals(20, 210, 20, 1, 19, new TicketRequest[] {
                new TicketRequest(ADULT, 1),
                new TicketRequest(CHILD, 19)
        });

        //Max infant tickets possible
        assertTotals(20, 200, 10, 10, 0, new TicketRequest[] {
                new TicketRequest(ADULT, 10),
                new TicketRequest(INFANT, 10)
        });

        //Mix of all 3
        assertTotals(20, 250, 15, 10, 5, new TicketRequest[] {
                new TicketRequest(ADULT, 10),
                new TicketRequest(CHILD, 5),
                new TicketRequest(INFANT, 5),
        });

        assertTotals(12, 140, 9, 5, 4, new TicketRequest[] {
                new TicketRequest(ADULT, 5),
                new TicketRequest(CHILD, 4),
                new TicketRequest(INFANT, 3),
        });
    }
    private void assertTotals(int tickets, int price, int seats, int adult, int child, TicketRequest[] ticketRequests) {
        TicketPurchaseRequest ticketPurchaseRequest = new TicketPurchaseRequest(1, ticketRequests);

        assertEquals(tickets, ticketPurchaseRequest.getTotalTicketsRequested());
        assertEquals(price, ticketPurchaseRequest.getTotalPrice());
        assertEquals(seats, ticketPurchaseRequest.getTotalSeatsRequired());

        assertEquals(adult, ticketPurchaseRequest.getTotalTicketsRequested(ADULT));
        assertEquals(child, ticketPurchaseRequest.getTotalTicketsRequested(CHILD));

        //Might as well check that we can process the request
        ticketService.purchaseTickets(ticketPurchaseRequest);
    }
}