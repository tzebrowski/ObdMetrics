package org.openelm327.core.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Streams {
	InputStream getInputStream() throws IOException;
	OutputStream getOutputStream() throws IOException;
}