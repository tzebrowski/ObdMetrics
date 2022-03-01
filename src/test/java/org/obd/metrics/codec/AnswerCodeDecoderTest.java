package org.obd.metrics.codec;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.SimpleWorkflowFactory;
import org.obd.metrics.api.Workflow;
import org.obd.metrics.pid.PidDefinitionRegistry;

//raw=410Bff, code=410b
//raw=410Bff, code=410b
//raw=41155aff, code=4115
//raw=41155aff, code=4115
//raw=410500, code=4105
//raw=410680, code=4106
//raw=41078b, code=4107
//raw=410C0000, code=410c
//raw=410F00, code=410f
//
public class AnswerCodeDecoderTest {

	@Test
	public void isAnswerCodeSuccess() throws IOException {
		Workflow workflow = SimpleWorkflowFactory.getMode01Workflow();
		PidDefinitionRegistry pidRegistry = workflow.getPidRegistry();
		
		AnswerCodeDecoder decoder = new AnswerCodeDecoder();
		boolean answerCodeSuccess = decoder.isAnswerCodeSuccess(pidRegistry.findBy(12l), "410bff");
		Assertions.assertThat(answerCodeSuccess).isEqualTo(true);
	}
	
	
	@Test
	public void isAnswerCodeError() throws IOException {
		Workflow workflow = SimpleWorkflowFactory.getMode01Workflow();
		PidDefinitionRegistry pidRegistry = workflow.getPidRegistry();
		
		AnswerCodeDecoder decoder = new AnswerCodeDecoder();
		boolean answerCodeSuccess = decoder.isAnswerCodeSuccess(pidRegistry.findBy(12l), "420bff");
		Assertions.assertThat(answerCodeSuccess).isEqualTo(false);
	}
	
}
