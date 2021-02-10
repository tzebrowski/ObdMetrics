package org.obd.metrics.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Connection {
	
	void connect() throws IOException;
	
	InputStream openInputStream() throws IOException;

	OutputStream openOutputStream() throws IOException;

	boolean isClosed();

	void reconnect() throws IOException;

	void close() throws IOException;
}