package org.obd.metrics.api;

import org.obd.metrics.command.obd.ObdCommand;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class UDSConstants {
	static ObdCommand UDS_TESTER_AVAILIBILITY = new ObdCommand("3E00");
	static ObdCommand UDS_EXTENDED_SESSION = new ObdCommand("10 03");
	static ObdCommand UDS_DEFAULT_SESSION = new ObdCommand("10 01");
}
