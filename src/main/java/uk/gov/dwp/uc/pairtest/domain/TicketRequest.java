package uk.gov.dwp.uc.pairtest.domain;

/**
 * Should be an Immutable Object
 */
public final class TicketRequest {

    private final int noOfTickets;
    private final Type type;

    public TicketRequest(Type type, int noOfTickets) {
        this.type = type;
        this.noOfTickets = noOfTickets;
    }

    public int getNoOfTickets() {
        return noOfTickets;
    }

    public Type getTicketType() {
        return type;
    }

    public int getPrice() {
        return noOfTickets * type.price;
    }

    public int getSeatsRequired() {
        return noOfTickets * type.seatsRequired;
    }

    /*
    |   Ticket Type    |     Price   |
    | ---------------- | ----------- |
    |    INFANT        |    £0       |
    |    CHILD         |    £10      |
    |    ADULT         |    £20      |
     */
    public enum Type {
        ADULT(20, 1),
        CHILD(10, 1),
        INFANT(0, 0);

        public final int price;
        public final int seatsRequired;

        Type(int price, int seatsRequired) {
            this.price = price;
            this.seatsRequired = seatsRequired;
        }
    }

}
