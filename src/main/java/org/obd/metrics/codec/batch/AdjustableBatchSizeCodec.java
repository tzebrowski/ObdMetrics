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

	private final int defaultService22defaultBatchSize;
	private final int defaultService01defaultBatchSize;
	
	protected static final String SERVICE_22 = "22";
	protected static final String SERVICE_01 = "01";

	protected AdjustableBatchSizeCodec(final BatchCodecType codecType, final Init init, final Adjustments adjustments, 
			final String query, final List<ObdCommand> commands, int service22defaultBatchSize, int serviceO1defaultBatchSize) {
		super(codecType, init, adjustments, query, commands);
		this.defaultService22defaultBatchSize = service22defaultBatchSize;
		this.defaultService01defaultBatchSize = serviceO1defaultBatchSize;
	}

	@Override
	protected int determineBatchSize(final String service) {
		
		if (SERVICE_22.equals(service)) {
			final Integer service22BatchSize = adjustments.getBatchPolicy().getService22BatchSize();
			return service22BatchSize == null || service22BatchSize <= 0 ? defaultService22defaultBatchSize : service22BatchSize;
		} if (SERVICE_01.equals(service)) {
			final Integer service01BatchSize = adjustments.getBatchPolicy().getService01BatchSize();
			return service01BatchSize == null || service01BatchSize <= 0 ? defaultService01defaultBatchSize : service01BatchSize;
		} else { 
			return DEFAULT_BATCH_SIZE;
		}
	}
}
