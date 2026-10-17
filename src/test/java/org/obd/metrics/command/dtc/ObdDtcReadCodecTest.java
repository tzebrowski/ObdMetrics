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
package org.obd.metrics.command.dtc;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.DiagnosticTroubleCode;
import org.obd.metrics.pid.PIDsGroup;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.pid.Resource;
import org.obd.metrics.translation.TranslationProvider;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

public class ObdDtcReadCodecTest {

	private static final PidDefinitionRegistry REGISTRY = PidDefinitionRegistry.builder()
			.source(Resource.builder().name("mode01.json")
					.inputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("mode01.json")).build())
			.build();

	private static List<DiagnosticTroubleCode> decode(String service, String reply) {
		return decode(new ObdDtcReadCodec(), service, reply);
	}

	private static List<DiagnosticTroubleCode> decode(ObdDtcReadCodec codec, String service, String reply) {
		final PidDefinition pid = REGISTRY.findBy(PIDsGroup.DTC_OBD_READ).stream()
				.filter(p -> p.getMode().equals(service)).findFirst().get().withModule("OBD");
		return codec.decode(pid, ConnectorResponseFactory.wrap(reply.getBytes()));
	}

	@Test
	public void storedCodesAreConfirmed() {
		final List<DiagnosticTroubleCode> codes = decode("03", "430201200300");
		Assertions.assertThat(codes).extracting(DiagnosticTroubleCode::getStandardCode).containsExactly("P0120", "P0300");
		Assertions.assertThat(codes).allSatisfy(dtc -> {
			Assertions.assertThat(dtc.getStatusMask()).isEqualTo(0x08);
			Assertions.assertThat(dtc.isPermanent()).isFalse();
			Assertions.assertThat(dtc.getModule()).isEqualTo("OBD");
			Assertions.assertThat(dtc.getRawHex()).hasSize(4);
			Assertions.assertThat(dtc.getFailureType()).isNull();
		});
	}

	@Test
	public void pendingAndPermanentCodes() {
		Assertions.assertThat(decode("07", "47010300")).singleElement()
				.satisfies(dtc -> Assertions.assertThat(dtc.getStatusMask()).isEqualTo(0x04));
		Assertions.assertThat(decode("0A", "4A01C123")).singleElement().satisfies(dtc -> {
			Assertions.assertThat(dtc.getStandardCode()).isEqualTo("U0123");
			Assertions.assertThat(dtc.getStatusMask()).isZero();
			Assertions.assertThat(dtc.isPermanent()).isTrue();
		});
	}

	@Test
	public void permanentStatusIsTranslated() {
		final ObdDtcReadCodec codec = new ObdDtcReadCodec(TranslationProvider.instance("pl"));
		Assertions.assertThat(decode(codec, "0A", "4A010420").get(0).getActiveStatuses())
				.containsExactly("Trwały kod usterki (DTC)");
	}

	// Each emissions ECU answers a functional request; the answers follow one another.
	@Test
	public void answersOfSeveralEcusAreAllRead() {
		Assertions.assertThat(decode("03", "4301012043020300 0420\r4300"))
				.extracting(DiagnosticTroubleCode::getStandardCode).containsExactly("P0120", "P0300", "P0420");
	}

	@Test
	public void multiFrameAnswer() {
		Assertions.assertThat(decode("03", "00C0:4305012003001:0420017191232:0000"))
				.extracting(DiagnosticTroubleCode::getStandardCode)
				.containsExactly("P0120", "P0300", "P0420", "P0171", "B1123");
	}

	@Test
	public void noCodesPaddingAndOtherReplies() {
		Assertions.assertThat(decode("03", "4300")).isEmpty();
		Assertions.assertThat(decode("03", "430200000120")).extracting(DiagnosticTroubleCode::getStandardCode)
				.containsExactly("P0120");
		Assertions.assertThat(decode("03", "")).isEmpty();
		final List<DiagnosticTroubleCode> other = decode("03", "41050A");
		Assertions.assertThat(other).isEmpty();
		Assertions.assertThat(other).isNotInstanceOf(DtcReadReply.class);
	}

	@Test
	public void negativeAnswerIsARefusal() {
		final List<DiagnosticTroubleCode> codes = decode("0A", "7F0A11");
		Assertions.assertThat(codes).isEmpty();
		Assertions.assertThat(codes).isInstanceOf(DtcReadReply.class);
		Assertions.assertThat(((DtcReadReply) codes).getStatus()).isEqualTo(DtcModuleStatus.REFUSED);
	}

	// The requests are data in mode01.json: 03 / 07 / 0A read, 04 clear, each with its answer code.
	@Test
	public void requestsComeFromMode01() {
		Assertions.assertThat(REGISTRY.findBy(PIDsGroup.DTC_OBD_READ)).extracting(PidDefinition::getQuery)
				.containsExactly("03", "07", "0A");
		Assertions.assertThat(REGISTRY.findBy(PIDsGroup.DTC_OBD_READ)).extracting(PidDefinition::getSuccessCode)
				.containsExactly("43", "47", "4A");
		Assertions.assertThat(REGISTRY.findBy(PIDsGroup.DTC_OBD_CLEAR)).extracting(PidDefinition::getQuery,
				PidDefinition::getSuccessCode).containsExactly(Assertions.tuple("04", "44"));
		Assertions.assertThat(REGISTRY.findBy(PIDsGroup.DTC_READ)).isEmpty();
	}
}
