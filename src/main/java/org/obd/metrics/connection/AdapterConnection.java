package org.obd.metrics.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Defines OBD Adapter connection interface.
 * 
 * @since 0.6.1
 * @author tomasz.zebrowski
 */
public interface AdapterConnection {

	void connect() throws IOException;

	InputStream openInputStream() throws IOException;

	OutputStream openOutputStream() throws IOException;

	void reconnect() throws IOException;

	void close() throws IOException;
}