package core.com.file.management.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import core.com.file.management.common.ErrorCode;
import core.com.file.management.common.FileManagementConstant;
import core.com.file.management.exception.VendorBulkUploadException;
import core.com.file.management.repo.ErrorFileDetailsRepo;
import core.com.file.management.repo.VendorBulkInvoiceUploadRepo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FileManagementUtil {

	@Value("${core.scfu.simple.date.format}")
	private String simpleDateFormat;

	@Autowired
	private ErrorFileDetailsRepo errorFileDetailsRepo;

	@Autowired
	private VendorBulkInvoiceUploadRepo vendorBulkInvoiceUploadRepo;

	public List<String> readFromExcelWorkbook(InputStream inputStream) throws VendorBulkUploadException {

		log.info("Entering readFromExcelWorkbook of {}", this.getClass().getSimpleName());

		List<String> contentList = new ArrayList<>();
		try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
			XSSFSheet sheet = workbook.getSheetAt(0);
			Iterator<?> rowIterator = sheet.rowIterator();
			while (rowIterator.hasNext()) {
				Row row = (XSSFRow) rowIterator.next();
				StringBuilder sbInside = new StringBuilder();
				for (int cellIndex = row.getFirstCellNum(); cellIndex < row.getLastCellNum(); cellIndex++) {
					Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					if (cell.getCellType() == CellType.STRING) {
						sbInside.append(cell.getStringCellValue());
					} else if (DateUtil.isCellDateFormatted(cell)) {
						sbInside.append(new SimpleDateFormat(simpleDateFormat).format(cell.getDateCellValue()));
					} else if (cell.getCellType() == CellType.NUMERIC) {
						sbInside.append(cell.getNumericCellValue());
					}
					sbInside.append(FileManagementConstant.COMMA);
				}
				contentList.add(sbInside.toString());
			}
		} catch (IOException ioe) {
			throw new VendorBulkUploadException(ErrorCode.FILE_PROCESSING_ERROR);
		}

		log.info("Exiting readFromExcelWorkbook of {}", this.getClass().getSimpleName());
		return contentList;
	}

	public synchronized String getContentHash(String content) throws NoSuchAlgorithmException {

		log.info("Entering getContentHash of {}", this.getClass().getSimpleName());

		MessageDigest digest = MessageDigest.getInstance(FileManagementConstant.ENCRYPTION_FUNCTION);
		byte[] digestByte = digest.digest(content.getBytes(StandardCharsets.UTF_8));

		log.info("Exiting getContentHash of {}", this.getClass().getSimpleName());
		return bytesToHex(digestByte);
	}

	private static String bytesToHex(byte[] hash) {
		StringBuilder hexString = new StringBuilder(2 * hash.length);
		for (int i = 0; i < hash.length; i++) {
			String hex = Integer.toHexString(0xff & hash[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}

	public Pageable getPageable(String dir, String sortBy, Integer size, Integer page) {

		log.info("Entering getPageable of {}", this.getClass().getSimpleName());

		if (StringUtils.isBlank(sortBy)) {
			sortBy = FileManagementConstant.DEAFULT_SORT_FIELD;
		}
		if (size == null) {
			size = FileManagementConstant.DEFAULT_PAGE_SIZE;
		}
		if (page == null) {
			page = FileManagementConstant.DEAFULT_PAGE_NUM;
		}
		if (StringUtils.isBlank(dir)) {
			dir = Sort.Direction.ASC.name();
		}
		Sort sort = dir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
				: Sort.by(sortBy).descending();

		Pageable pageable = PageRequest.of(page, size, sort);

		log.info("Exiting getPageable of {}", this.getClass().getSimpleName());
		return pageable;
	}

	public String getGuid(String operation) {

		log.info("Entering getGuid of {}", this.getClass().getSimpleName());
		String guid = UUID.randomUUID().toString();
		long count = 0;

		switch (operation) {
		case FileManagementConstant.BULK_UPLOAD:
			count = vendorBulkInvoiceUploadRepo.checkIfGuidPresent(guid);
			if (count != 0) {
				return getGuid(FileManagementConstant.BULK_UPLOAD);
			} else {
				break;
			}
		case FileManagementConstant.ERROR_FILE:
			count = errorFileDetailsRepo.checkIfGuidPresent(guid);
			if (count != 0) {
				return getGuid(FileManagementConstant.BULK_UPLOAD);
			} else {
				break;
			}
		default:

		}

		log.info("Exiting getGuid of {}", this.getClass().getSimpleName());
		return guid;
	}

	public String getFilePath(String hashKey) {

		log.info("Entering getFilePath of {}", this.getClass().getSimpleName());

		String path = hashKey.substring(0, 2).concat("/").concat(hashKey.substring(2, 4)).concat("/");

		log.info("Exiting getFilePath of {}", this.getClass().getSimpleName());
		return "src/main/resources/" + path;
	}

	public ByteArrayInputStream writeToCsvFile(List<String> contentList) throws IOException {

		log.info("Entering writeToCsvFile of {}", this.getClass().getSimpleName());

		try (final ByteArrayOutputStream stream = new ByteArrayOutputStream();
				final CSVPrinter printer = new CSVPrinter(new PrintWriter(stream), CSVFormat.DEFAULT)) {
			printer.printRecord(contentList);
			printer.flush();

			log.info("Exiting writeToCsvFile of {}", this.getClass().getSimpleName());
			return new ByteArrayInputStream(stream.toByteArray());
		} catch (final IOException e) {
			throw new IOException(ErrorCode.FILE_DOWNLOADING_ERROR);
		}
	}

	public ByteArrayInputStream writeToErrorCsvFile(List<List<String>> contentList) throws IOException {

		log.info("Entering writeToErrorCsvFile of {}", this.getClass().getSimpleName());

		try (final ByteArrayOutputStream stream = new ByteArrayOutputStream();
				final CSVPrinter printer = new CSVPrinter(new PrintWriter(stream), CSVFormat.DEFAULT)) {
			printer.printRecord(contentList);
			printer.flush();

			log.info("Exiting writeToErrorCsvFile of {}", this.getClass().getSimpleName());
			return new ByteArrayInputStream(stream.toByteArray());
		} catch (final IOException e) {
			throw new IOException(ErrorCode.FILE_DOWNLOADING_ERROR);
		}
	}

	public ByteArrayInputStream writeToTxtFile(List<String> contentList, String delimiter) throws IOException {

		log.info("Entering writeToTxtFile of {}", this.getClass().getSimpleName());

		try (final ByteArrayOutputStream stream = new ByteArrayOutputStream();) {
			StringBuilder sb = new StringBuilder();
			for (String field : contentList) {
				sb.append(field);
				if (StringUtils.isNotEmpty(delimiter)) {
					sb.append(delimiter);
				}
			}
			stream.writeBytes(StringUtils.chop(sb.toString()).getBytes());

			log.info("Exiting writeToTxtFile of {}", this.getClass().getSimpleName());
			return new ByteArrayInputStream(stream.toByteArray());
		} catch (final IOException e) {
			throw new IOException(ErrorCode.FILE_DOWNLOADING_ERROR);
		}

	}

	public ByteArrayInputStream writeToErrorTxtFile(List<List<String>> contentList, String delimiter)
			throws IOException {

		log.info("Entering writeToErrorTxtFile of {}", this.getClass().getSimpleName());

		try (final ByteArrayOutputStream stream = new ByteArrayOutputStream();) {
			StringBuilder sb = new StringBuilder();
			contentList.forEach(cl -> {
				cl.forEach(c -> {
					sb.append(c);
					if (StringUtils.isNotEmpty(delimiter)) {
						sb.append(delimiter);
					}
				});
				sb.append(System.lineSeparator());
			});

			stream.writeBytes(StringUtils.chop(sb.toString()).getBytes());

			log.info("Exiting writeToErrorTxtFile of {}", this.getClass().getSimpleName());
			return new ByteArrayInputStream(stream.toByteArray());
		} catch (final IOException e) {
			throw new IOException(ErrorCode.FILE_DOWNLOADING_ERROR);
		}

	}

	public ByteArrayInputStream writeToXlsFile(List<String> contentList) throws IOException {

		log.info("Entering writeToXlsFile of {}", this.getClass().getSimpleName());

		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
			Sheet sheet = workbook.createSheet();
			Row row = sheet.createRow(0);
			int cellCount = 0;
			for (String field : contentList) {
				row.createCell(cellCount).setCellValue(field);
			}
			workbook.write(stream);

			log.info("Exiting writeToXlsFile of {}", this.getClass().getSimpleName());
			return new ByteArrayInputStream(stream.toByteArray());
		} catch (IOException e) {
			throw new IOException(ErrorCode.FILE_DOWNLOADING_ERROR);
		}
	}

	public ByteArrayInputStream writeToErrorXlsFile(List<List<String>> contentList) throws IOException {

		log.info("Entering writeToErrorXlsFile of {}", this.getClass().getSimpleName());

		try (Workbook workbook = new XSSFWorkbook();
				ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
			Sheet sheet = workbook.createSheet();
			Iterator <List<String>> contentListIterator = contentList.iterator();
			int cellCount = 0;
			int rowCount = 0;
			
			while (contentListIterator.hasNext()) {
				List<String> templist = contentListIterator.next();
				Iterator<String> tempIterator = templist.iterator();
				Row row = sheet.createRow(rowCount++);
				cellCount = 0;
				while (tempIterator.hasNext()) {
					String temp = tempIterator.next();
					Cell cell = row.createCell(cellCount++);
					cell.setCellValue(temp);
				}
			}

			workbook.write(stream);

			log.info("Exiting writeToErrorXlsFile of {}", this.getClass().getSimpleName());
			return new ByteArrayInputStream(stream.toByteArray());
		} catch (IOException e) {
			throw new IOException(ErrorCode.FILE_DOWNLOADING_ERROR);
		}
	}

}
