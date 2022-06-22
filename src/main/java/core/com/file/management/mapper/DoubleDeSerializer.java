package core.com.file.management.mapper;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import core.com.file.management.common.ErrorCode;

@Component
public class DoubleDeSerializer extends StdDeserializer<Double>{

	private static final long serialVersionUID = -1342794379509308763L;

	protected DoubleDeSerializer() {
		super(Double.class);
	}

	@Override
	public Double deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		try {
			return Double.parseDouble(p.readValueAs(String.class));
		} catch(IOException ioe) {
			throw new IOException(ErrorCode.INVALID_AMOUNT);
		}
	}
}
