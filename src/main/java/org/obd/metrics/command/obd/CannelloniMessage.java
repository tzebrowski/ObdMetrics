/** 
 * Copyright 2019-2024, Tomasz Żebrowski
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package org.obd.metrics.command.obd;

import org.obd.metrics.command.Command;

public final class CannelloniMessage extends Command {
	public final static String PEER_HELLO_MESSAGE = "CANNELLONIv1";

	public static CannelloniMessage hello() {
		return new CannelloniMessage(PEER_HELLO_MESSAGE);
	}

	public static CannelloniMessage uds(final String canId, final String data) {
		// calculate length
		return new CannelloniMessage(canId, String.format("%02X ", data.length() / 2) + data);
	}

	public CannelloniMessage(final String canId, final String data) {
		super(canId, data);
	}

	public CannelloniMessage(final byte[] canId, final byte[] data) {
		super(canId, data);
	}

	private CannelloniMessage(String msg) {
		super(msg, null, null);
	}
}
