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
package org.obd.metrics.codec.giulia_2_0_gme;

import org.obd.metrics.codec.CodecTest;

public interface Giulia_2_0_GME_Test extends CodecTest {

	public default String getPidFile() {
		return "giulia_2.0_gme.json";
	}
	
	default void assertEquals(long id, String actualValue, Object expectedValue) {
		assertEquals(false, actualValue.substring(2, 6), id, getPidFile(), actualValue, expectedValue);
	}
	
	default void assertEquals(String actualValue, Object expectedValue) {
		assertEquals(Boolean.FALSE, actualValue, expectedValue);
	}

	default void assertCloseTo(String actualValue, float expectedValue, float offset) {
		assertCloseTo(false, actualValue.substring(2, 6), getPidFile(), actualValue, expectedValue, offset);
	}
	
	default void assertEquals(boolean debug, String actualValue, Object expectedValue) {
		assertEquals(debug, actualValue.substring(2, 6), null, getPidFile(), actualValue, expectedValue);
	}
}
