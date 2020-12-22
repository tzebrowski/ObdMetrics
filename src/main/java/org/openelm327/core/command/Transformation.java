package org.openelm327.core.command;

import java.util.Arrays;
import java.util.List;

public interface Transformation {
	default List<String> transform(String raw){
		return Arrays.asList();
	}
} 
