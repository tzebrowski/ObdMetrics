package org.obd.metrics.api.integration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.obd.metrics.CommandLoop;
import org.obd.metrics.DataCollector;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.command.ATCommand;
import org.obd.metrics.command.group.AlfaMed17CommandGroup;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.connection.AdapterConnection;
import org.obd.metrics.pid.PidDefinitionRegistry;

/**
 * 
 * OBD-request ID
11 bit functional: 0x7DF, psysical: 0x7E0
29 bit functional: 0x18DB33F1, psysical: 0x18DA10F1
OBD-response
11 bit ECU1: 0x7E8, ECU2: 0x7E9, ECU3: 0x7EA
29 bit ECU1: 0x18DAF110, ECU2: 0x18DAF118, ECU3: 0x18DAF128
 */
public class UdsTest {

	
	@Test
	public void case_01() throws IOException, InterruptedException, ExecutionException {

		final AdapterConnection connection = BluetoothConnection.openConnection();

		final InputStream source = Thread.currentThread().getContextClassLoader().getResourceAsStream("mode01.json");

		final PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder().source(source).build();

		final CommandsBuffer buffer = CommandsBuffer.instance();
		buffer.addFirst(new ATCommand("Z")); // reset
		buffer.addLast(new ATCommand("L0")); // line feed off
		buffer.addLast(new ATCommand("H0")); // headers off
		buffer.addLast(new ATCommand("E0")); // echo off
		buffer.addLast(new ATCommand("PP 2CSV 01"));
		buffer.addLast(new ATCommand("PP 2C ON")); // activate baud rate PP.
		buffer.addLast(new ATCommand("PP 2DSV 01")); // activate addressing pp.
		buffer.addLast(new ATCommand("PP 2D ON"));
		buffer.addLast(new ATCommand("S0")); // Print spaces on*/off
//		buffer.addLast(new ATCommand("SPB")); // SAE J1939 CAN (29 bit ID 250* kbaud)
//		buffer.addLast(new ATCommand("CP18")); // Set CAN priority to 18 (29 bit only)
//		buffer.addLast(new ATCommand("CRA 18DAF110")); // Set CAN hardware filter18DAF110
//		buffer.addLast(new ATCommand("SH DA10F1")); // Set CAN request message header: DA10F1
//		buffer.addLast(new ATCommand("ATSH ")); // Adaptive timing off auto1* auto2
//		buffer.addLast(new ATCommand("ST19")); // Set OBD response timeout.); // Add protocol initialization AT commands
		
		 
//		buffer.addLast(new ATCommand("SH 7DF"));
		buffer.addLast(new ATCommand("SP6")); // SAE J1939 CAN (29 bit ID 250* kbaud)

		
		buffer.addLast(new ObdCommand("7E0020105"));
		buffer.addLast(new ObdCommand("7DF02010C"))
		       .addLast(new QuitCommand());// Last command that will close the communication

		final DataCollector collector = new DataCollector(); // It collects the

		final CodecRegistry codecRegistry = CodecRegistry.builder().equationEngine("JavaScript")
		        .build();

		final CommandLoop executor = CommandLoop
		        .builder()
		        .connection(connection)
		        .buffer(buffer)
		        .observer(collector)
		        .pids(pidRegistry)
		        .codecs(codecRegistry)
		        .build();

		final ExecutorService executorService = Executors.newFixedThreadPool(1);
		executorService.invokeAll(Arrays.asList(executor));
		executorService.shutdown();
		source.close();
	}
}
