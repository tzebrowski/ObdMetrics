package org.obd.metrics.command;

import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.Getter;


@EqualsAndHashCode(of = { "query" })
public abstract class Command {

	@Getter
	protected final String uid = UUID.randomUUID().toString();

	@Getter
	protected final String query;

	@Getter
	protected final String label;
	
	@Getter
	protected final byte []data; 
	
	@Getter
	protected final String mode;
	
	protected Command(String query,String mode,String label){
		this.query = query;
		this.label = label;
		this.mode = mode;
		this.data = (query + "\r").getBytes();
	}
	
	@Override
	public String toString() {
		return "[query=" + query + "]";
	}
}
