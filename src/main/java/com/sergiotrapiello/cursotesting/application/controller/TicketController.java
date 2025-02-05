package com.sergiotrapiello.cursotesting.application.controller;

import com.sergiotrapiello.cursotesting.application.ui.Paths;
import com.sergiotrapiello.cursotesting.application.ui.RequestMapping;
import com.sergiotrapiello.cursotesting.application.ui.ResponseEntity;
import com.sergiotrapiello.cursotesting.domain.api.TicketService;
import com.sergiotrapiello.cursotesting.domain.exception.UnexistingTicketException;
import com.sergiotrapiello.cursotesting.domain.exception.UnpaidTicketException;
import com.sergiotrapiello.cursotesting.domain.spi.BarrierDevicePort;

public final class TicketController implements Controller {

	private TicketService ticketService;

	public TicketController(TicketService ticketService) {
		this.ticketService = ticketService;
	}

	@RequestMapping(Paths.Ticket.ISSUE)
	public ResponseEntity issueTicket() {
		return ResponseEntity.ok(ticketService.issueTicket());
	}

	@RequestMapping(Paths.Ticket.CALCULATE_AMOUNT)
	public ResponseEntity calculateAmount(int ticketNumber) {
		try {
			return ResponseEntity.ok(ticketService.calculateAmount(ticketNumber));
		} catch (UnexistingTicketException e) {
			return ResponseEntity.error("Unexisting ticket for number: " + e.getTicketNumber());
		}
	}
	
	@RequestMapping(Paths.Ticket.DEPARTURE)
	public ResponseEntity departure(int ticketNumber, BarrierDevicePort barrierDevicePort) {
		try {
			return ResponseEntity.ok(ticketService.openGate(ticketNumber, barrierDevicePort));				
		} catch(UnpaidTicketException e) {
			return ResponseEntity.error("Unpaid ticket: " + e.getTicket().getId());
		} catch (UnexistingTicketException e) {
			return ResponseEntity.error("Unexisting ticket for number: " + e.getTicketNumber());
		}
	}
	
	@RequestMapping(Paths.Ticket.PAYMENT)
	public ResponseEntity departure(int ticketNumber) {
		try {
			return ResponseEntity.ok(ticketService.pay(ticketNumber));				
		} catch (UnexistingTicketException e) {
			return ResponseEntity.error("Unexisting ticket for number: " + e.getTicketNumber());
		}
	}

}
