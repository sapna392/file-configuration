package core.com.file.management.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class ResponseMetadata implements Serializable{
	
	private static final long serialVersionUID = 5102750798532211729L;
	
	private long totalPages;
	private Integer page;
	private int size;
	private long elements;
	
}
