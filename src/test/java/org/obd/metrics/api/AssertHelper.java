package org.obd.metrics.api;

import java.util.concurrent.BlockingDeque;

import org.assertj.core.api.Assertions;

final class AssertHelper {

	static void assertInitializationCommands(final BlockingDeque<String> recordedQueries) {
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATZ");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATL0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATH1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATE0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATAL");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATAT2");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSP7");
	}
}
