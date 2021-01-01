package org.openobd2.core;

import java.io.IOException;

import org.openobd2.core.channel.Channel;
import org.openobd2.core.channel.bt.BluetoothChannel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class IntegrationTestBase {

	protected Channel openStream() {
		try {
			return BluetoothChannel.builder().adapter("AABBCC112233").build();
		} catch (IOException e) {
			log.error("Failed to open BT channel",e);
		}
		return null;
	}

}
