package core.com.file.management.common;

public class ErrorCode {
	
	public static final String EMPTY_USER_ID = "User id is empty";
	public static final String EMPTY_USER_TYPE = "User type is empty";
	public static final String DELIMITER_REQ = "Delimiter is required if the file structure is delimiter";
	public static final String DUPLICATE_FILEDS = "Invalid configuration since there are duplicate fields";
	public static final String INVALID_ORDER = "Invalid field order";
	public static final String INVALID_POSITION = "Invalid field postions";
	public static final String INVALID_FILE_STRUCT = "Invalid file structure. Should be either delimiter or fixed";
	public static final String EMPTY_FILE_NAME = "File cannot be uploaded with empty name";
	public static final String EMPTY_FILE_CONTENT = "File is empty";
	public static final String INVALID_FILE_TYPE = "Invalid File Format. The file has to be in .txt or . xls or .csv format";
	public static final String FILE_SIZE_EXCEEDED = "File size cannot exceed 2 MB";
	public static final String FILE_PROCESSING_ERROR = "Error while processing the file";
	public static final String FILE_UPLOADING_ERROR = "Error while uploading the file. Please contact admin";
	public static final String FILE_RETRIEVING_ERROR = "Error while retriveing file configuration details. Please contact admin";
	public static final String FILE_DOWNLOADING_ERROR = "Error while downloading the file. Please contact admin";
	public static final String FILE_CONFIG_DOESNOT_EXISTS = "File configuration does not exists for the current user";
	public static final String USER_DOES_NOT_EXISTS = "User does not exists";
	public static final String FILE_CONFIG_DOESNOT_MATCH = "File configuration does not match with the uploaded file";
	public static final String INVOICE_NUMBER_MISSING = "Invoice number missing for one of the record in the uploaded file";
	public static final String MANDATORY_FIELD_MISSING = "Mandatory field %s missing for invoice number %s";
	public static final String INVALID_DATE = "Invalid %s, either date or format is invalid";
	public static final String PROCESSING_DATE_EARLY = "Processing date is earlier than the invoice date for invoice code %s";
	public static final String EARLIER_PROCESSING_DATE = "Processing date is earlier for invoice code %s";

}