package org.obd.metrics.codec;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.obd.metrics.PidRegistryCache;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

//raw=410Bff, code=410b
//raw=410Bff, code=410b
//raw=41155aff, code=4115
//raw=41155aff, code=4115
//raw=410500, code=4105
//raw=410680, code=4106
//raw=41078b, code=4107
//raw=410C0000, code=410c
//raw=410F00, code=410f
public class ConnectorResponseTest {
	
	@ParameterizedTest
	@CsvSource(value = { 
		"410Bff;true;12",
		"420bff;false;12",
	}, delimiter = ';')
	public void responseCodeSuccessTest(String input, String result,String pid) throws IOException {
		final PidDefinitionRegistry pidRegistry = PidRegistryCache.get("mode01.json");
		
		ConnectorResponse connectorResponse = ConnectorResponseFactory.wrap(input.getBytes());
		boolean answerCodeSuccess = connectorResponse.isResponseCodeSuccess(pidRegistry.findBy(Long.valueOf(pid)));
		Assertions.assertThat(answerCodeSuccess).isEqualTo(Boolean.parseBoolean(result));
	}
}
