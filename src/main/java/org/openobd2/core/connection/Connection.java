package org.openobd2.core.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Connection {

	InputStream openInputStream() throws IOException;

	OutputStream openOutputStream() throws IOException;

	boolean isClosed();

	void reconnect() throws IOException;

	void close() throws IOException;
}