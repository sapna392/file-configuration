package core.com.file.management.common;

import java.util.ArrayList;
import java.util.List;

public class FileManagementConstant {

	public static final String SUCCESS = "success";
	public static final String FAILURE = "failure";
	public static final String FILE_CONFIG_STRUCTURE = "fileStructure";
	public static final String FILE_CONFIG_DELIMITER = "fileDelimiter";
	public static final String FIXED = "fixed";
	public static final String COMMA = ",";
	public static final String PIPE = "|";
	public static final String PIPE_DELIMITER = "\\|";
	public static final List<String> FILE_TYPE = new ArrayList<String>(List.of(".txt", ".xls", ".csv"));
	public static final List<String> ADDITIONAL_FIELDS = new ArrayList<String>(
			List.of("ADDITIONAL_DETAILS_1", "ADDITIONAL_DETAILS_2", "ADDITIONAL_DETAILS_3", "ADDITIONAL_DETAILS_4",
					"ADDITIONAL_DETAILS_5", "ADDITIONAL_DETAILS_6", "ADDITIONAL_DETAILS_7", "ADDITIONAL_DETAILS_8",
					"ADDITIONAL_DETAILS_9", "ADDITIONAL_DETAILS_10"));
	public static final String TXT_FILE = ".txt";
	public static final String XLS_FILE = ".xls";
	public static final String CSV_FILE = ".csv";
	public static final String ENCRYPTION_FUNCTION = "SHA-256";
	public static final String FILE_UPLOADED_SUCCESS = "File uploaded successfully";
	public static final String FILE_CONFIG_SAVE_SUCCESS = "File configuration saved successfully";
	public static final String FILE_CONFIG_FETCH_SUCCESS = "File configuration fetched successfully";
	public static final String DEAFULT_SORT_FIELD = "id";
	public static final Integer DEFAULT_PAGE_SIZE = 20;
	public static final Integer DEAFULT_PAGE_NUM = 1;
	public static final String BULK_UPLOAD = "BULK_UPLOAD";
	public static final String LINE_DELIMITER = "\r\n";
	public static final String ADDITIONAL_FIELD = "additionalField";
	public static final String ADDITIONAL_FIELD_1 = "additionalField1";
	public static final String ADDITIONAL_FIELD_LIST = "additionalFieldList";
	public static final String CSV_MIME_TYPE = "text/csv";
	public static final String XLS_MIME_TYPE = "application/vnd.ms-excel";
	public static final String TXT_MIME_TYPE = "text/plain";
	public static final String EXCEL_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	public static final String STATUS_PENDING = "Pending";
	
}
