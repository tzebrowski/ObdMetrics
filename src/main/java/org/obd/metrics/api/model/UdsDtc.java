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
package org.obd.metrics.api.model;

import java.util.List;

import lombok.EqualsAndHashCode;

/**
 * DTO for a fully parsed UDS DTC
 */
@EqualsAndHashCode(of = "standardCode")
public class UdsDtc {
    public final String standardCode;
    public final String failureTypeByte;
    public final String rawHex;
    public final int statusMask;
    public final List<String> activeStatuses;

    public UdsDtc(String standardCode, String failureTypeByte, String rawHex, int statusMask, List<String> activeStatuses) {
        this.standardCode = standardCode;
        this.failureTypeByte = failureTypeByte;
        this.rawHex = rawHex;
        this.statusMask = statusMask;
        this.activeStatuses = activeStatuses;
    }

    @Override
    public String toString() {
        return String.format(
            "  {\n    \"DTC\": \"%s-%s\",\n    \"Raw Hex\": \"%s\",\n    \"Status Mask\": \"0x%02X\",\n    \"Statuses\": %s\n  }",
            standardCode, failureTypeByte, rawHex, statusMask, activeStatuses
        );
    }
}