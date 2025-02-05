package com.sergiotrapiello.cursotesting.domain.api;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import com.sergiotrapiello.cursotesting.domain.exception.UnexistingTicketException;
import com.sergiotrapiello.cursotesting.domain.exception.UnpaidTicketException;
import com.sergiotrapiello.cursotesting.domain.model.Ticket;
import com.sergiotrapiello.cursotesting.domain.spi.BarrierDevicePort;
import com.sergiotrapiello.cursotesting.domain.spi.TicketRepositoryPort;

public final class TicketServiceImpl implements TicketService {

	private static final int MINUTES_IN_DAY = 1440;
	
	private final Clock clock;
	
	private final TicketRepositoryPort ticketRepositoryPort;
	
	private final double pricePerMin;

	private final double dailyMaximum;
	
	public TicketServiceImpl(Clock clock, double pricePerMin, double dailyMaximum, TicketRepositoryPort ticketRepositoryPort) {
		this.clock = clock;
		this.ticketRepositoryPort = ticketRepositoryPort;
		this.pricePerMin = pricePerMin;
		this.dailyMaximum = dailyMaximum;
	}

	@Override
	public Ticket issueTicket() {
		return ticketRepositoryPort.save(new Ticket(clock));
	}

	@Override
	public double calculateAmount(int ticketNumber) {
		Optional<Ticket> ticket = ticketRepositoryPort.get(ticketNumber);
		ticket.orElseThrow(() -> new UnexistingTicketException(ticketNumber));
		long totalMins = getTotalMins(ticket.get());
		return calculateFullDaysAmount(totalMins) + calculateRemainderMinutesAmount(totalMins);
	}

	private double calculateRemainderMinutesAmount(long totalMins) {
		double remainderMinutes = totalMins % MINUTES_IN_DAY;
		double remainderMinutesAmount = remainderMinutes * pricePerMin;
		return remainderMinutesAmount <= dailyMaximum ? remainderMinutesAmount : dailyMaximum;
	}

	private double calculateFullDaysAmount(long totalMins) {
		return totalMins / MINUTES_IN_DAY * dailyMaximum;
	}

	private long getTotalMins(Ticket ticket) {
		LocalDateTime now = LocalDateTime.now(clock);
		return ChronoUnit.MINUTES.between(ticket.getIssuedDateTime(), now);
	}

	@Override
	public Ticket openGate(int ticketNumber, BarrierDevicePort barrierDevicePort) {
		Optional<Ticket> ticketOpt = ticketRepositoryPort.get(ticketNumber);
		ticketOpt.orElseThrow(() -> new UnexistingTicketException(ticketNumber));
	
		if(ticketOpt.get().isPaid()) {
			barrierDevicePort.open();
			return ticketOpt.get();			
		} else {
			throw new UnpaidTicketException(ticketOpt.get());
		}
	}

	@Override
	public Ticket pay(int ticketNumber) {
		Optional<Ticket> ticketOpt = ticketRepositoryPort.get(ticketNumber);
		ticketOpt.orElseThrow(() -> new UnexistingTicketException(ticketNumber));
		Ticket ticket = ticketOpt.get();
		ticket.setPaid(true);
		ticketRepositoryPort.update(ticket);
		Optional<Ticket> ticketOptAfterUpdate = ticketRepositoryPort.get(ticketNumber);
		ticketOptAfterUpdate.orElseThrow(() -> new UnexistingTicketException(ticketNumber));
		return ticketOptAfterUpdate.get();
	}

}
