package uk.gov.dwp.uc.pairtest.common;

public enum TicketPriceSeats {
	ADULT(20, 1), CHILD(10, 1), INFANT(0, 0);

	public final int price;
	public final int seats;

	private TicketPriceSeats(int price, int seats) {
		this.price = price;
		this.seats = seats;
	}
}
