/** 
 * Copyright 2019-2023, Tomasz Żebrowski
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
package org.obd.metrics.executor;

import java.util.HashMap;
import java.util.Map;

import org.obd.metrics.api.model.Reply;
import org.obd.metrics.codec.Codec;
import org.obd.metrics.command.Command;
import org.obd.metrics.pid.PIDsGroup;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class VehicleMetadataReader extends PIDsGroupReader<Map<String, String>>  {

	VehicleMetadataReader() {
		super(PIDsGroup.METADATA);
		value = new HashMap<String, String>();
	}
	
	@Override
	public void onNext(Reply<?> reply) {
		final Command command = (Command) reply.getCommand();
		log.debug("Recieved vehicle metadata: {}", reply);
		
		if (command instanceof Codec<?>) {
			final Object decode = ((Codec<?>) command).decode(reply.getRaw());
			if (decode == null) {
				value.put(command.getLabel(), reply.getRaw().getMessage());
			} else {
				value.put(command.getLabel(), decode.toString());
			}
		} else {
			value.put(command.getLabel(), reply.getRaw().getMessage());
		}
	}
}
