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
package org.obd.metrics.codec.batch;

import java.util.List;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.command.obd.ObdCommand;

abstract class AdjustableBatchSizeCodec extends AbstractBatchCodec {

	private final int defaultMode22defaultBatchSize;
	private final int defaultMode01defaultBatchSize;
	
	protected static final String MODE_22 = "22";
	protected static final String MODE_01 = "01";

	protected AdjustableBatchSizeCodec(final BatchCodecType codecType, final Init init, final Adjustments adjustments, 
			final String query, final List<ObdCommand> commands, int mode22defaultBatchSize, int mode01defaultBatchSize) {
		super(codecType, init, adjustments, query, commands);
		this.defaultMode22defaultBatchSize = mode22defaultBatchSize;
		this.defaultMode01defaultBatchSize = mode01defaultBatchSize;
	}

	@Override
	protected int determineBatchSize(final String mode) {
		
		if (MODE_22.equals(mode)) {
			final Integer mode22BatchSize = adjustments.getBatchPolicy().getMode22BatchSize();
			return mode22BatchSize == null || mode22BatchSize <= 0 ? defaultMode22defaultBatchSize : mode22BatchSize;
		} if (MODE_01.equals(mode)) {
			final Integer mode01BatchSize = adjustments.getBatchPolicy().getMode01BatchSize();
			return mode01BatchSize == null || mode01BatchSize <= 0 ? defaultMode01defaultBatchSize : mode01BatchSize;
		} else { 
			return DEFAULT_BATCH_SIZE;
		}
	}
}
