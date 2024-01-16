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
package org.obd.metrics.pid;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import lombok.AllArgsConstructor;

public interface Urls {

	@AllArgsConstructor
	static final class BytesHandler extends URLStreamHandler {
		final String content;

		@Override
		protected URLConnection openConnection(URL u) throws IOException {
			return new ByteUrlConnection(u, content);
		}
	}

	static final class ByteUrlConnection extends URLConnection {
		final String content;

		ByteUrlConnection(URL url, String content) {
			super(url);
			this.content = content;
		}

		@Override
		public void connect() throws IOException {
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return new ByteArrayInputStream(content.getBytes());
		}
	}

	static URL resourceToUrl(String name) {
		return Thread.currentThread().getContextClassLoader().getResource(name);
	}

	static URL stringToUrl(String name, String content) throws MalformedURLException {
		return new URL(null, "bytes:///" + name, new BytesHandler(content));
	}

}
