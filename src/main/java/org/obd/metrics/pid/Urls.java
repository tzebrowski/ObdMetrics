package org.obd.metrics.pid;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.List;

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

	static List<InputStream> toStreams(List<URL> urls) throws IOException {
		final List<InputStream> ret = new ArrayList<>();
		for (URL url : urls) {
			ret.add(url.openStream());
		}
		return ret;
	}

}
