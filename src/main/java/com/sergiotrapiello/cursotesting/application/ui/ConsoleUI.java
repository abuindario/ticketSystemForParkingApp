package com.sergiotrapiello.cursotesting.application.ui;

import java.time.LocalDateTime;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sergiotrapiello.cursotesting.domain.model.Ticket;
import com.sergiotrapiello.cursotesting.infrastructure.device.XYZBarrierDevicePortAdapter;

public class ConsoleUI {

	private final RequestDispatcher dispatcher;

	private Gson gson;

	public ConsoleUI(RequestDispatcher dispatcher) {
		this.dispatcher = dispatcher;
		gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
	}

	public void run() {
		Scanner scanner = new Scanner(System.in);
		String option;

		do {
			showMenu();
			option = scanner.next();
			manageSelectedOption(option, scanner);

		} while (!"5".equals(option));

		scanner.close();
	}

	private void showMenu() {
		System.out.println("\n\n---------------------------");
		System.out.println(" 1. Emitir ticket");
		System.out.println(" 2. Calcular importe a pagar");
		System.out.println(" 3. Salir del parking");
		System.out.println(" 4. Pagar ticket");
		System.out.println(" 5. Salir");
		System.out.println("");
		System.out.print("Seleccione una opcion: ");
	}

	private void manageSelectedOption(String option, Scanner scanner) {
		switch (option) {
		case "1" -> issueTicket();
		case "2" -> manageCalculateAmount(scanner);
		case "3" -> departure(scanner);
		case "4" -> payment(scanner);
		case "5" -> System.out.println("fin");
		default -> System.out.println("\nSeleccione una opción válida");
		}
	}

	private void issueTicket() {
		System.out.println("Ticket: " + gson.toJson(dispatcher.doDispatch(Paths.Ticket.ISSUE).getBody()));
	}

	private void manageCalculateAmount(Scanner scanner) {
		System.out.println("Introducir número de ticket: ");
		String ticketNumber = scanner.next();
		if (StringUtils.isNumeric(ticketNumber)) {
			ResponseEntity response = dispatcher.doDispatch(Paths.Ticket.CALCULATE_AMOUNT,
					Integer.parseInt(ticketNumber));
			manageCalculateAmountResponse(response);
		} else {
			System.err.println("El formato del número de ticket es incorrecto. ");
		}
	}

	private void manageCalculateAmountResponse(ResponseEntity response) {
		if (response.isOk()) {
			System.out.println(String.format("El importe a pagar es: %s€", gson.toJson(response.getBody())));
		} else {
			System.err.println(response.getBody());
		}
	}
	
	private void departure(Scanner scanner) {
		System.out.println("Introducir número de ticket: ");
		String ticketNumber = scanner.next();
		if (StringUtils.isNumeric(ticketNumber)) {
			ResponseEntity response = dispatcher.doDispatch(Paths.Ticket.DEPARTURE,
					Integer.parseInt(ticketNumber), new XYZBarrierDevicePortAdapter());
			manageDepartureResponse(response);
		} else {
			System.err.println("El formato del número de ticket es incorrecto. ");
		}
	}
	
	private void manageDepartureResponse(ResponseEntity response) {
		if (response.isOk()) {
			Ticket ticket = (Ticket) response.getBody();
			System.out.println("El ticket está pagado, buen viaje. \n" + gson.toJson(ticket));
		} else {
			System.err.println(response.getBody());
		}
	}
	
	private void payment(Scanner scanner) {
		System.out.println("Introducir número de ticket: ");
		String ticketNumber = scanner.next();
		if (StringUtils.isNumeric(ticketNumber)) {
			ResponseEntity response = dispatcher.doDispatch(Paths.Ticket.PAYMENT, Integer.parseInt(ticketNumber));
			managePaymentResponse(response);
		} else {
			System.err.println("El formato del número de ticket es incorrecto. ");
		}
	}
	
	private void managePaymentResponse(ResponseEntity response) {
		if (response.isOk()) {
			Ticket ticket = (Ticket) response.getBody();
			System.out.println("El ticket está pagado\n" + gson.toJson(ticket));
		} else {
			System.err.println(response.getBody());
		}
	}

}
