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

import java.util.HashSet;
import java.util.Set;

import org.obd.metrics.api.model.Reply;
import org.obd.metrics.command.SupportedPIDsCommand;
import org.obd.metrics.pid.PIDsGroup;

final class VehicleCapabilitiesReader extends PIDsGroupReader<Set<String>> {

	VehicleCapabilitiesReader() {
		super(PIDsGroup.CAPABILITES);
		value = new HashSet<String>();
	}

	@Override
	public void onNext(Reply<?> reply) {
		final SupportedPIDsCommand command = (SupportedPIDsCommand) reply.getCommand();
		value.addAll(command.decode(command.getPid(), reply.getRaw()));
	}
}
