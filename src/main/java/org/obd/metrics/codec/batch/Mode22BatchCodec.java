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
package org.obd.metrics.codec.batch;

import java.util.List;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.command.obd.ObdCommand;

abstract class Mode22BatchCodec extends AbstractBatchCodec {

	private final int defaultBatchSize;

	protected Mode22BatchCodec(final BatchCodecType codecType, final Init init, final Adjustments adjustments, final String query,
			final List<ObdCommand> commands, int defaultBatchSize) {
		super(codecType, init, adjustments, query, commands);
		this.defaultBatchSize = defaultBatchSize;
	}

	@Override
	protected int determineBatchSize(final String mode) {
		final Integer mode22BatchSize = adjustments.getBatchPolicy().getMode22BatchSize();
		
		if (MODE_22.equals(mode)) {
			return mode22BatchSize == null || mode22BatchSize <= 0 ? defaultBatchSize : mode22BatchSize;
		} else {
			return DEFAULT_BATCH_SIZE;
		}
	}
}
