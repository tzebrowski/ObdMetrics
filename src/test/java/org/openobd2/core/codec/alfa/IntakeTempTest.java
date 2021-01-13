package org.openobd2.core.codec.alfa;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openobd2.core.codec.PidTest;

public class IntakeTempTest  implements PidTest{
	@Test
	public void tempTest() throws IOException {
		mode22Test("62193540",0.0);
		mode22Test("62193542",2.0);
	}
}
