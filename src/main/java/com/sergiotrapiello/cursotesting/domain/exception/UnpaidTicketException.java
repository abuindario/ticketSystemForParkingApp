package com.sergiotrapiello.cursotesting.domain.exception;

import com.sergiotrapiello.cursotesting.domain.model.Ticket;

public class UnpaidTicketException extends RuntimeException {

	private static final long serialVersionUID = -2929311616687299473L;

	private final Ticket ticket;
	
	public UnpaidTicketException(Ticket ticket) {
		this.ticket = ticket;
	}
	
	public Ticket getTicket() {
		return ticket;
	}

}
