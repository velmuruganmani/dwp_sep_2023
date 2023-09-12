package uk.gov.dwp.uc.pairtest;

import java.util.HashMap;
import java.util.Map;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationService;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.common.TicketConstants;
import uk.gov.dwp.uc.pairtest.common.TicketPriceSeats;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import org.apache.log4j.Logger;

public class TicketServiceImpl implements TicketService {
	/**
	 * Should only have private methods other than the one below.
	 */
	
	static final Logger logger = Logger.getLogger(TicketServiceImpl.class);

	private TicketPaymentService paymentService = new TicketPaymentServiceImpl();
	private SeatReservationService seatReservationService = new SeatReservationServiceImpl();

	@Override
	public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests)
			throws InvalidPurchaseException {

		/*
		 * - There are 3 types of tickets i.e. Infant, Child, and Adult. - The ticket
		 * prices are based on the type of ticket (see table below). - The ticket
		 * purchaser declares how many and what type of tickets they want to buy. -
		 * Multiple tickets can be purchased at any given time. - Only a maximum of 20
		 * tickets that can be purchased at a time. - Infants do not pay for a ticket
		 * and are not allocated a seat. They will be sitting on an Adult's lap. - Child
		 * and Infant tickets cannot be purchased without purchasing an Adult ticket.
		 */

		if (accountId <= 0) {
			logger.error("Invalid account id provided");
			throw new InvalidPurchaseException("Invalid account id");
		}

		if (ticketTypeRequests.length <= 0) {
			logger.error("Atleast one ticket should be bought");
			throw new InvalidPurchaseException("Atleast one ticket should be bought");
		}

		final Map<String, Integer> ticketTypeMap = calculateTotalTicketsAndAmountAndSeats(ticketTypeRequests);

		if (ticketTypeMap.get(TicketConstants.TOTAL_NUM_OF_SEATS)+ticketTypeMap.get(TicketConstants.INFANT_COUNT) > TicketConstants.MAX_ALLOCATED_TICKETS) {
			logger.error("Total Number of tickets exceeded the purchase limit");
			throw new InvalidPurchaseException("Total Number of tickets exceeded the purchase limit");
		}

		paymentService.makePayment(accountId, ticketTypeMap.get(TicketConstants.TOTAL_AMOUNT_TO_PAY));
		logger.error("payment completed for the account id "+accountId);
		seatReservationService.reserveSeat(accountId, ticketTypeMap.get(TicketConstants.TOTAL_NUM_OF_SEATS));
		logger.error("seats reserved for the account id "+accountId);
	}

	// Calculate total numbers of tickets, seats & amount
	private Map<String, Integer> calculateTotalTicketsAndAmountAndSeats(final TicketTypeRequest... ticketTypeRequests) {
		Map<String, Integer> ticketTypeMap = new HashMap<>();
		boolean isAdultTicketExists = false;
		for (TicketTypeRequest ticketTypeRequest : ticketTypeRequests)
		{
			final String ticketType = ticketTypeRequest.getTicketType().name();
			if (ticketType.equalsIgnoreCase(TicketTypeRequest.Type.INFANT.name()))
				ticketTypeMap.put(TicketConstants.INFANT_COUNT,ticketTypeRequest.getNoOfTickets());
			if (ticketType.equalsIgnoreCase(TicketTypeRequest.Type.ADULT.name()))
				isAdultTicketExists = true;
			prepareAmountAndSeats(ticketTypeMap, ticketTypeRequest, TicketPriceSeats.valueOf(ticketType).price,
					TicketPriceSeats.valueOf(ticketType).seats);
		}

		if (!isAdultTicketExists) {
			logger.error("Atleast one Adult ticket should be purchased");
			throw new InvalidPurchaseException("Atleast one Adult ticket should be purchased");
		}
		return ticketTypeMap;
	}

	private Map<String, Integer> prepareAmountAndSeats(final Map<String, Integer> ticketTypeMap,
			final TicketTypeRequest ticketTypeRequest, final int price, final int seats) {
		if(ticketTypeRequest.getNoOfTickets()<0) {
			logger.error("No of tickets can not be negative");
			throw new InvalidPurchaseException("NoOfTickets can not be negative");
		}
		ticketTypeMap.put(TicketConstants.TOTAL_AMOUNT_TO_PAY,
				ticketTypeMap.containsKey(TicketConstants.TOTAL_AMOUNT_TO_PAY)
						? ticketTypeMap.get(TicketConstants.TOTAL_AMOUNT_TO_PAY)
								+ ticketTypeRequest.getNoOfTickets() * price
						: ticketTypeRequest.getNoOfTickets() * price);
		ticketTypeMap.put(TicketConstants.TOTAL_NUM_OF_SEATS,
				ticketTypeMap.containsKey(TicketConstants.TOTAL_NUM_OF_SEATS)
						? ticketTypeMap.get(TicketConstants.TOTAL_NUM_OF_SEATS)
								+ ticketTypeRequest.getNoOfTickets() * seats
						: ticketTypeRequest.getNoOfTickets() * seats);
		return ticketTypeMap;
	}

}
