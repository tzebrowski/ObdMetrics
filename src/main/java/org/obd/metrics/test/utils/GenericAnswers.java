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
package org.obd.metrics.test.utils;

import java.util.HashMap;
import java.util.Map;

public final class GenericAnswers {

	public static Map<String, String> genericAnswers() {
		final Map<String, String> requestResponse = new HashMap<>();
		requestResponse.put("ATZ", "connected?");
		requestResponse.put("ATL0", "atzelm327v1.5");
		requestResponse.put("ATH0", "ath0ok");
		requestResponse.put("ATE0", "ate0ok");
		requestResponse.put("ATSP0", "ok");
		requestResponse.put("AT I", "elm327v1.5");
		requestResponse.put("AT @1", "obdiitors232interpreter");
		requestResponse.put("AT @2", "?");
		requestResponse.put("AT DP", "auto");
		requestResponse.put("AT DPN", "a0");
		requestResponse.put("AT RV", "11.8v");
		return requestResponse;
	}
}
