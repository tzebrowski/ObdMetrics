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
package org.obd.metrics.transport.message;

interface Bytes {
	byte at(int index);

	int remaining();

	long capacity();

	default int indexOf(final byte[] str, final int strCount, final int fromIndex) {

		final int valueCount = remaining();
		final byte first = str[0];
		final int max = (valueCount - strCount);
		for (int i = fromIndex; i <= max; i++) {
			if (at(i) != first) {
				while (++i <= max && at(i) != first) {
					;
				}
			}
			if (i <= max) {
				int j = i + 1;
				final int end = j + strCount - 1;
				for (int k = 1; j < end && at(j) == str[k]; j++, k++) {
					;
				}
				if (j == end) {
					return i;
				}
			}
		}
		return -1;
	}
}