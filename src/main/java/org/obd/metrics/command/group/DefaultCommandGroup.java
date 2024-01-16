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
package org.obd.metrics.command.group;

import org.obd.metrics.command.ATCommand;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.process.DelayCommand;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class DefaultCommandGroup<T extends Command> extends CommandGroup<T> {

	public static final CommandGroup<Command> INIT = new DefaultCommandGroup<>().of(
			new ATCommand("D"), // Set all to defaults
			new ATCommand("Z"), // Reset OBD
			new DelayCommand(0),
			new ATCommand("L0"), // Line feed off
			new ATCommand("H0"), // Headers off
			new ATCommand("E0"), // Echo off
			new ATCommand("PP 2CSV 01"), 
			new ATCommand("PP 2C ON"), // activate baud rate PP.
			new ATCommand("PP 2DSV 01"), // activate addressing pp.
			new ATCommand("PP 2D ON"),
			new ATCommand("AT2"));
}
