package core.com.file.management.mapper;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Date;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import core.com.file.management.common.ErrorCode;

@Component
public class DateDeSerializer extends StdDeserializer<Date> {

	private static final long serialVersionUID = 3027403532323368996L;

	public DateDeSerializer() {
		super(Date.class);
	}

	@Override
	public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		try {
			String dateString = p.readValueAs(String.class);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu")
					.withResolverStyle(ResolverStyle.STRICT);
			LocalDate localDate = LocalDate.parse(dateString, formatter);
			return java.sql.Date.valueOf(localDate);
		} catch (DateTimeParseException exp) {
			throw new IOException(String.format(ErrorCode.INVALID_DATE, p.getCurrentName()));
		}
	}
}
