package uk.gov.dwp.uc.pairtest;

import static org.mockito.Mockito.times;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {
	
	private long ACCOUNT_ID = 1234L;

    @InjectMocks
    private TicketService ticketServiceImpl = new TicketServiceImpl();

    @Mock
    private TicketPaymentService paymentService;

    @Mock
    private SeatReservationService seatReservationService;

    @Test
    public void testPurchaseTicketsWithValidRequest(){
        TicketTypeRequest t1 =  new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10);
        TicketTypeRequest t2 =  new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 5);
        TicketTypeRequest t3 =  new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 5);
        
        ticketServiceImpl.purchaseTickets(ACCOUNT_ID, t1, t2, t3);
        
        Mockito.verify(paymentService, times(1)).makePayment(1234L, 250);
        Mockito.verify(seatReservationService, times(1)).reserveSeat(1234L, 15);
    }

    @Test
    public void testPurchaseTicketsWithInfantCountExceedingLimit(){
        TicketTypeRequest t1 =  new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10);
        TicketTypeRequest t2 =  new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 7);
        TicketTypeRequest t3 =  new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 5);

        Assertions.assertThrows(InvalidPurchaseException.class, () -> {
            ticketServiceImpl.purchaseTickets(ACCOUNT_ID, t1, t2, t3);
        }, "Total Number of tickets exceeded the purchase limit");

}
    
    @Test
    public void testPurchaseTicketsWithInValidRequestNegativeNoOfPeople(){
        TicketTypeRequest t1 =  new TicketTypeRequest(TicketTypeRequest.Type.ADULT, -10);
        TicketTypeRequest t2 =  new TicketTypeRequest(TicketTypeRequest.Type.CHILD, -5);
        TicketTypeRequest t3 =  new TicketTypeRequest(TicketTypeRequest.Type.INFANT, -5);
        
        Assertions.assertThrows(InvalidPurchaseException.class, () -> {
        	ticketServiceImpl.purchaseTickets(ACCOUNT_ID, t1, t2, t3);
    	}, "NoOfTickets can not negative");
        
    }
    
    @Test
    public void testPurchaseTicketsWhenMaxAllocatedTicketsExceeded(){
        TicketTypeRequest t1 =  new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10);
        TicketTypeRequest t2 =  new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 20);
        TicketTypeRequest t3 =  new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 5);
        
        Assertions.assertThrows(InvalidPurchaseException.class, () -> {
        	ticketServiceImpl.purchaseTickets(ACCOUNT_ID, t1, t2, t3);
    	}, "Total Number of tickets exceeded the purchase limit");
    }

    @Test
    public void testPurchaseTicketsWithoutAdultTicket(){
        TicketTypeRequest t2 =  new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 10);
        TicketTypeRequest t3 =  new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 5);
        
        Assertions.assertThrows(InvalidPurchaseException.class, () -> {
        	ticketServiceImpl.purchaseTickets(ACCOUNT_ID, t2, t3);
    	}, "Atleast one Adult ticket should be purchased");
    }

    @Test
    public void testPurchaseTicketsWithInvalidAccountNumber(){
        TicketTypeRequest t2 =  new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10);
        TicketTypeRequest t3 =  new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 5);
        
        Assertions.assertThrows(InvalidPurchaseException.class, () -> {
        	ticketServiceImpl.purchaseTickets(0L, t2, t3);
    	}, "Invalid account id");
    }
    
    @Test
    public void testPurchaseTicketsWithEmptyTicketRequest(){       
        Assertions.assertThrows(InvalidPurchaseException.class, () -> {
        	ticketServiceImpl.purchaseTickets(ACCOUNT_ID);
    	}, "Atleast one ticket should be bought");
    }
}
