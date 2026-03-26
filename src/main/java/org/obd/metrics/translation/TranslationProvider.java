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
package org.obd.metrics.translation;

/**
 * Provides translated descriptions for PID definitions and DTC components.
 * Implementations load translations from locale-specific resources.
 *
 * <p>To add a new language, create a JSON file at {@code translations/{locale}.json}
 * and use {@link JsonFileTranslationProvider} to load it.
 */
public interface TranslationProvider {

	TranslationProvider NOOP = new NoOpTranslationProvider();

	String translatePidDescription(Long pidId, String defaultDescription);

	String translatePidLongDescription(Long pidId, String defaultLongDescription);

	String translateDtcSystem(Character key, String defaultValue);

	String translateDtcCategory(Character key, String defaultValue);

	String translateDtcPowerTrain(Character key, String defaultValue);

	String translateDtcFailureType(String key, String defaultValue);

	String translateDtcStatusBit(String key, String defaultValue);

	String translateUnits(String defaultUnits);
}
