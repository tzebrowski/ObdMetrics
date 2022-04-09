package org.obd.metrics.connection;

import java.util.HashMap;
import java.util.Map;

class GenericAnswers {

	static Map<String, String> genericAnswers() {
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
