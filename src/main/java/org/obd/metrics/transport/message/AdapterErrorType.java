package org.obd.metrics.transport.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AdapterErrorType {
	CANERROR("CANERROR".getBytes()), 
	BUSBUSY("BUSBUSY".getBytes()), 
	STOPPED("STOPPED".getBytes()), 
	ERROR("ERROR".getBytes()), 
	NO_DATA("NODATA".getBytes()), 
	NONE("NONE".getBytes()), 
	BUSINIT("BUSINIT".getBytes()), 
	UNABLETOCONNECT("UNABLETOCONNECT".getBytes()),
	LVRESET("LVRESET".getBytes()), 
	TIMEOUT("TIMEOUT".getBytes()),
	FCRXTIMEOUT("FCRXTIMEOUT".getBytes());
		
	@Getter
	private final byte []bytes;
	
}