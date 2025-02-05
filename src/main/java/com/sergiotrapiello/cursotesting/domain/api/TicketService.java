package com.sergiotrapiello.cursotesting.domain.api;

import com.sergiotrapiello.cursotesting.domain.model.Ticket;
import com.sergiotrapiello.cursotesting.domain.spi.BarrierDevicePort;

public interface TicketService {

	Ticket issueTicket();

	double calculateAmount(int ticketNumber);

	Ticket openGate(int ticketNumber, BarrierDevicePort barrierDevicePort);
	
	Ticket pay(int ticketNumber);
}
