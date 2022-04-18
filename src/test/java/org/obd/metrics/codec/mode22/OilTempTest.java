package org.obd.metrics.codec.mode22;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class OilTempTest implements Mode22Test {

	
	@Test
	public void case_00() {

		final Map<String, Number> mappings = new HashMap<String, Number>() {
			private static final long serialVersionUID = 1L;
			{
				put("62194F2E25", 3);
			}
		};
		mappings.forEach((k, v) -> {
			assertEquals(k, v);
		});
	}


	@Test
	public void case_01() {

		final Map<String, Number> mappings = new HashMap<String, Number>() {
			private static final long serialVersionUID = 1L;
			{
				put("62194F3B85", 83.97);
			}
		};
		mappings.forEach((k, v) -> {
			assertEquals(k, v);
		});
	}

	@Test
	public void case_02() {

		final Map<String, Number> mappings = new HashMap<String, Number>() {
			private static final long serialVersionUID = 1L;
			{
				put("62194F3BE5", 113.0);
			}
		};
		mappings.forEach((k, v) -> {
			assertEquals(k, v);
		});
	}

	@Test
	public void case_03() {

		final Map<String, Number> mappings = new HashMap<String, Number>() {
			private static final long serialVersionUID = 1L;
			{
				put("62194F2d85", 0.0);
			}
		};
		mappings.forEach((k, v) -> {
			assertEquals(k, v);
		});
	}

	@Test
	public void case_04() {

		final Map<String, Number> mappings = new HashMap<String, Number>() {
			private static final long serialVersionUID = 1L;
			{
				put("62194F2D85", -0.027);
			}
		};
		mappings.forEach((k, v) -> {
			assertEquals(k, v);
		});
	}

	@Test
	public void case_05() {

		final Map<String, Number> mappings = new HashMap<String, Number>() {
			private static final long serialVersionUID = 1L;
			{
				put("62194F3E65", 101.0);
			}
		};
		mappings.forEach((k, v) -> {
			assertEquals(k, v);
		});
	}

	@Test
	public void case_06() {

		final Map<String, Number> mappings = new HashMap<String, Number>() {
			private static final long serialVersionUID = 1L;
			{
				put("62194F2da5", 2.0);
			}
		};
		mappings.forEach((k, v) -> {
			assertEquals(k, v);
		});
	}
	
	@Test
	public void case_07() {

		final Map<String, Number> mappings = new HashMap<String, Number>() {
			private static final long serialVersionUID = 1L;
			{
				put("62194F2DC5", 1.0);
			}
		};
		mappings.forEach((k, v) -> {
			assertEquals(k, v);
		});
	}

}
