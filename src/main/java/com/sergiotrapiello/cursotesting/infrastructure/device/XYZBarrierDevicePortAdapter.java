package com.sergiotrapiello.cursotesting.infrastructure.device;

import com.sergiotrapiello.cursotesting.domain.spi.BarrierDevicePort;

public class XYZBarrierDevicePortAdapter implements BarrierDevicePort {
	
	@Override
	public void open() {
		System.out.println("ABRIENDO BARRERA...");
		System.out.println("BARRERA ABIERTA");
	}
}
