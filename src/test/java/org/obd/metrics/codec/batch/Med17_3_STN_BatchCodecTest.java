 /**
 * Copyright 2019-2026, Tomasz Żebrowski
 *
 * <p>Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.obd.metrics.codec.batch;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.STNxxExtensions;

public class Med17_3_STN_BatchCodecTest extends BatchCodecTestRunner {
	
	final Adjustments ADJUSTEMENTS = Adjustments
			.builder()
			.stNxx(STNxxExtensions.builder()
					.enabled(Boolean.TRUE)
					.promoteSlowGroupsEnabled(Boolean.FALSE).build())
			.cachePolicy(CachePolicy.builder().resultCacheEnabled(false).build())  
			.batchPolicy(BatchPolicy.builder()
					  .calculateResponseFrames(true)
					  .enabled(Boolean.TRUE).build())
			  .build();
	
	@Test
	public void case_12_pids() {
		final Map<Object, Object> expectedValues = new HashMap<>();
		expectedValues.put("1000", 0);
		expectedValues.put("1924", 0.0);
		expectedValues.put("1827", 3227);
		expectedValues.put("1828", 753);
		expectedValues.put("1937", 3227);
		expectedValues.put("181F", 999);
		expectedValues.put("180E", 0.0);
		expectedValues.put("1867", 1151.1);
		expectedValues.put("1002", 0);
		expectedValues.put("1821", 5.0);
		expectedValues.put("1812", 0.0);
		expectedValues.put("1001", 0.0);
		
		final String query = "STPX H:18DA10F1, D:22 1000 1924 1827 1828 1937 181F 180E 1867 1002 1821 1812 1001";
		final String ecuAnswer = "02F0:6210000000191:2400001827A15D2:182825AB1937A13:5D181F63EE180E4:000018672CF7105:02000018210CCD6:181200100100";
		runTest(query, 
				Arrays.asList(new ValidationInput(expectedValues, ecuAnswer)),
				ADJUSTEMENTS,"alfa.json"
		
		);
	}
	
	@Test
	public void case_14_pids() {
		final Map<Object, Object> expectedValues = new HashMap<>();
		expectedValues.put("1000", 0);
		expectedValues.put("1924", 0.0);
		expectedValues.put("1827", 3227);
		expectedValues.put("1828", 753);
		expectedValues.put("1937", 3227);
		expectedValues.put("181F", 999);
		expectedValues.put("180E", 0.0);
		expectedValues.put("1867", 1151.1);
		expectedValues.put("1002", 0);
		expectedValues.put("1821", 5.0);
		expectedValues.put("1812", 0.0);
		expectedValues.put("1001", 0.0);
		expectedValues.put("1004", 12.26);
		expectedValues.put("1935", -39);
		
		final String query = "STPX H:18DA10F1, D:22 1000 1924 1827 1828 1937 181F 180E 1867 1002 1821 1812 1001 1004 1935";
		final String ecuAnswer = "0350:6210000000191:2400001827A15D2:182825AB1937A13:5D181F63EE180E4:000018672CF7105:02000018210CCD6:181200100100107:048219350B";
		runTest(query, 
				Arrays.asList(new ValidationInput(expectedValues, ecuAnswer)),
				ADJUSTEMENTS,"alfa.json"
		
		);
	}
	
	@Test
	public void case_16_pids() {
		final Map<Object, Object> expectedValues = new HashMap<>();
		expectedValues.put("1000", 0);
		expectedValues.put("1924", 0.0);
		expectedValues.put("1827", 3227);
		expectedValues.put("1828", 753);
		expectedValues.put("1937", 3227);
		expectedValues.put("181F", 998);
		expectedValues.put("180E", 0.0);
		expectedValues.put("1867", 1151.1);
		expectedValues.put("1002", 0);
		expectedValues.put("1821", 5.0);
		expectedValues.put("1812", 0.0);
		expectedValues.put("1001", 0.0);
		expectedValues.put("1004", 12.36);
		expectedValues.put("1935", -39);
		expectedValues.put("1003", -24);
		expectedValues.put("194F", -21.03);
		
		final String query = "STPX H:18DA10F1, D:22 1000 1924 1827 1828 1937 181F 180E 1867 1002 1821 1812 1001 1004 1935 1003 194F";
		final String ecuAnswer = "03C0:6210000000191:2400001827A15D2:182825AB1937A13:5D181F63DC180E4:000018672CF7105:02000018210CCD6:181200100100107:048319350B10038:1F194F2A05";
		runTest(query, 
				Arrays.asList(new ValidationInput(expectedValues, ecuAnswer)),
				ADJUSTEMENTS,"alfa.json"
		
		);
	}
	
	
	@Test
	public void case_18_pids() {
		final Map<Object, Object> expectedValues = new HashMap<>();
		expectedValues.put("1000", 0);
		expectedValues.put("1924", 0.0);
		expectedValues.put("1827", 3227);
		expectedValues.put("1828", 753);
		expectedValues.put("1937", 3227);
		expectedValues.put("181F", 996);
		expectedValues.put("180E", 0.0);
		expectedValues.put("1867", 1151.1);
		expectedValues.put("1002", 0);
		expectedValues.put("1821", 5.0);
		expectedValues.put("1812", 0.0);
		expectedValues.put("1001", 0.0);
		expectedValues.put("1004", 12.36);
		expectedValues.put("1935", -39);
		expectedValues.put("1003", -24);
		expectedValues.put("194F", -27.78);
		expectedValues.put("1837", 20);
		expectedValues.put("183F", -29);
		
		final String query = "STPX H:18DA10F1, D:22 1000 1924 1827 1828 1937 181F 180E 1867 1002 1821 1812 1001 1004 1935 1003 194F 1837 183F";
		final String ecuAnswer = "7F22780420:6210000000191:2400001827A15D2:182825A91937A13:5D181F63B2180E4:000018672CF7105:02000018210CCD6:181200100100107:048319350B10038:1F194F28E518379:0E183F19";
		runTest(query, 
				Arrays.asList(new ValidationInput(expectedValues, ecuAnswer)),
				ADJUSTEMENTS,"alfa.json"
		
		);
	}
	
	
	
	@Test
	public void case_21_pids() {
		final Map<Object, Object> expectedValues = new HashMap<>();
		expectedValues.put("1000", 0);
		expectedValues.put("1924", 0.0);
		expectedValues.put("186B", 1200);
		expectedValues.put("1827", 3227);
		expectedValues.put("1828", 753);
		expectedValues.put("1937", 3227);
		expectedValues.put("181F", 996);
		expectedValues.put("180E", 0.0);
		expectedValues.put("1867", 1151.1);
		expectedValues.put("186C", 0.0);
		expectedValues.put("186D", 0.0);
		expectedValues.put("186E", 0.0);
		expectedValues.put("186D", 0.0);
		expectedValues.put("1002", 0);
		expectedValues.put("18AD", 0.0);
		expectedValues.put("18AE", 20.07);
		expectedValues.put("18C7", 19.92);
		expectedValues.put("18AF", 0.0);
		expectedValues.put("18C8", 3.0);
		expectedValues.put("1910", 11.0);
		expectedValues.put("1911", 11.0);
		
		final String query = "STPX H:18DA10F1, D:22 1000 1924 186B 1827 1828 1937 181F 180E 1867 186C 186D 186E 186F 1002 18AD 18AE 18C7 18AF 18C8 1910 1911";
		final String ecuAnswer = "7F227804E0:6210000000191:240000186B78182:27A15D182825A73:1937A15D181F634:B0180E000018675:2CF7186C00186D6:00186E00186F007:1002000018AD008:0018AE336018C79:3318AF000018C8A:03191008981911B:0898";
		runTest(query, 
				Arrays.asList(new ValidationInput(expectedValues, ecuAnswer)),
				ADJUSTEMENTS,"alfa.json"	
		);
	}
}
