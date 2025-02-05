package com.sergiotrapiello.cursotesting.itest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.core.connection.ConnectionHolderImpl;
import com.github.database.rider.junit5.DBUnitExtension;
import com.sergiotrapiello.cursotesting.application.controller.TicketController;
import com.sergiotrapiello.cursotesting.application.ui.Paths;
import com.sergiotrapiello.cursotesting.application.ui.RequestDispatcher;
import com.sergiotrapiello.cursotesting.application.ui.ResponseEntity;
import com.sergiotrapiello.cursotesting.application.ui.ResponseEntity.Status;
import com.sergiotrapiello.cursotesting.domain.api.TicketService;
import com.sergiotrapiello.cursotesting.domain.api.TicketServiceImpl;
import com.sergiotrapiello.cursotesting.domain.model.Ticket;
import com.sergiotrapiello.cursotesting.infrastructure.device.XYZBarrierDevicePortAdapter;
import com.sergiotrapiello.cursotesting.infrastructure.jdbc.TicketJdbcRepository;
import com.sergiotrapiello.cursotesting.util.TestUtils;

@ExtendWith(DBUnitExtension.class)
@DataSet("tickets.yml")
class TicketITest {

	@SuppressWarnings("unused")
	private ConnectionHolder connectionHolder;
	
	private RequestDispatcher dispatcher;
	
	private Clock clock;
	
	@BeforeEach
	void setup() throws SQLException {
		Connection conn = DriverManager.
			    getConnection("jdbc:h2:mem:test;INIT=runscript from 'classpath:schema.sql'", "sa", "");
		connectionHolder = new ConnectionHolderImpl(conn);
		clock = TestUtils.clock("2023-11-06T08:00:00.00Z");
		TicketService ticketService = new TicketServiceImpl(clock, 0.022d, 32d, new TicketJdbcRepository(conn));
		dispatcher = new RequestDispatcher(Set.of(new TicketController(ticketService)));
	}
	
	@Test
	@DataSet("tickets_before_payment.yml")
	@ExpectedDataSet(value = "tickets_after_payment.yml", ignoreCols = "id")
	void shouldCompletePayment() {
		// GIVEN
		int ticketNumber = 103;
		
		// WHEN
		ResponseEntity responseEntity = dispatcher.doDispatch(Paths.Ticket.PAYMENT, ticketNumber);
		
		// THEN
		assertNotNull(responseEntity);
		assertEquals(Status.OK, responseEntity.getStatus());
		Ticket responseTicket = (Ticket) responseEntity.getBody();
		assertNotNull(responseTicket);
		assertTrue(responseTicket.isPaid());
	}	
	
	@Test
	void shouldFailPayment_unexistingTicket() {
		// GIVEN
		int unexistingTicketNumber = 555;
		
		// WHEN
		ResponseEntity responseEntity = dispatcher.doDispatch(Paths.Ticket.PAYMENT, unexistingTicketNumber);
		
		// THEN
		assertNotNull(responseEntity);
		assertEquals(Status.ERROR, responseEntity.getStatus());
		assertEquals("Unexisting ticket for number: " + unexistingTicketNumber, responseEntity.getBody());
	}	
	
	@Test
	void shouldOpenGate() {
		// GIVEN
		int ticketNumber = 102;
		
		XYZBarrierDevicePortAdapter devicePortMock = mock(XYZBarrierDevicePortAdapter.class);
		
		// WHEN
		ResponseEntity responseEntity = dispatcher.doDispatch(Paths.Ticket.DEPARTURE, ticketNumber, devicePortMock);
		
		// THEN
		assertNotNull(responseEntity);
		assertEquals(Status.OK, responseEntity.getStatus());
		Ticket responseTicket = (Ticket) responseEntity.getBody();
		assertNotNull(responseTicket);
		assertTrue(responseTicket.isPaid());
		verify(devicePortMock, times(1)).open();
	}
	
	@Test
	void shouldNotOpenGate_unexistingTicket() {
		// GIVEN
		int unexistingTicketNumber = 555;
		
		XYZBarrierDevicePortAdapter devicePortMock = mock(XYZBarrierDevicePortAdapter.class);
		
		// WHEN
		ResponseEntity responseEntity = dispatcher.doDispatch(Paths.Ticket.DEPARTURE, unexistingTicketNumber, devicePortMock);
		
		// THEN
		assertNotNull(responseEntity);
		assertEquals(Status.ERROR, responseEntity.getStatus());
		assertEquals("Unexisting ticket for number: " + unexistingTicketNumber, responseEntity.getBody());
		verify(devicePortMock, never()).open();
	}
	
	@Test
	void shouldNotOpenGate_unpaidTicket() {
		// GIVEN
		int unpaidTicket = 101;
		
		XYZBarrierDevicePortAdapter devicePortMock = mock(XYZBarrierDevicePortAdapter.class);

		// WHEN
		ResponseEntity responseEntity = dispatcher.doDispatch(Paths.Ticket.DEPARTURE, unpaidTicket, devicePortMock);
		
		// THEN
		assertNotNull(responseEntity);
		assertEquals(Status.ERROR, responseEntity.getStatus());
		assertEquals("Unpaid ticket: " + unpaidTicket, responseEntity.getBody());
		verify(devicePortMock, never()).open();
	}
	
	@Test
	void shouldCalculateAmount() {

		// GIVEN
		int ticketNumber = 101;
		double expectedAmount = 0.308d;

		// WHEN
		ResponseEntity responseEntity = dispatcher.doDispatch(Paths.Ticket.CALCULATE_AMOUNT, ticketNumber);

		// THEN
		assertNotNull(responseEntity);
		assertEquals(Status.OK, responseEntity.getStatus());
		assertEquals(expectedAmount, (double) responseEntity.getBody());
	}
	
	@Test
	void shouldFailCalculatingAmount_unexistingTicket() {

		// GIVEN
		int unexistingTicketNumber = 666;

		// WHEN
		ResponseEntity responseEntity = dispatcher.doDispatch(Paths.Ticket.CALCULATE_AMOUNT, unexistingTicketNumber);

		// THEN
		assertNotNull(responseEntity);
		assertEquals(Status.ERROR, responseEntity.getStatus());
		assertEquals("Unexisting ticket for number: " + unexistingTicketNumber, responseEntity.getBody());
	}
	
	@Test
	@ExpectedDataSet(value = "tickets_expected_after_issue.yml", ignoreCols = "id")
	void shouldIssueTicket() {

		// GIVEN
		// dataset

		// WHEN
		ResponseEntity responseEntity = dispatcher.doDispatch(Paths.Ticket.ISSUE);

		// THEN
		assertNotNull(responseEntity);
		assertEquals(Status.OK, responseEntity.getStatus());
		Ticket responseTicket = (Ticket) responseEntity.getBody();
		assertNotNull(responseTicket);
		assertEquals(LocalDateTime.now(clock), responseTicket.getIssuedDateTime());
		assertNotNull(responseTicket.getId());
	}

}
