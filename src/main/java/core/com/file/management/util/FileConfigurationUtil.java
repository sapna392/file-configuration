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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import core.com.file.management.common.ErrorCode;
import core.com.file.management.common.FileManagementConstant;
import core.com.file.management.exception.VendorBulkUploadException;
import core.com.file.management.repo.BulkUploadFileRepo;

@Component
public class FileConfigurationUtil {

	@Autowired
	BulkUploadFileRepo uploadFileRepo;

	public List<String> readFromExcelWorkbook(InputStream inputStream) throws VendorBulkUploadException {
		List<String> contentList = new ArrayList<>();
		try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
			XSSFSheet sheet = workbook.getSheetAt(0);
			Iterator<?> rowIterator = sheet.rowIterator();
			while (rowIterator.hasNext()) {
				Row row = (XSSFRow) rowIterator.next();
				StringBuffer sbInside = new StringBuffer();
				for (int cellIndex = row.getFirstCellNum(); cellIndex < row.getLastCellNum(); cellIndex++) {
					Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					if (cell.getCellType() == CellType.STRING) {
						sbInside.append(cell.getStringCellValue());
                    } else if(DateUtil.isCellDateFormatted(cell)){
						sbInside.append(new SimpleDateFormat("dd/MM/yyyy").format(cell.getDateCellValue()));
                    }else if (cell.getCellType() == CellType.NUMERIC) {
                    	sbInside.append(cell.getNumericCellValue());
                    }
					sbInside.append(FileManagementConstant.COMMA);
				}
				contentList.add(sbInside.toString());
			}
		} catch (IOException ioe) {
			throw new VendorBulkUploadException(ErrorCode.FILE_PROCESSING_ERROR);
		}
		return contentList;
	}

	public synchronized String getContentHash(String content) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance(FileManagementConstant.ENCRYPTION_FUNCTION);
		byte[] digestByte = digest.digest(content.getBytes(StandardCharsets.UTF_8));
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
		return pageable;
	}

	public String getGuid(String operation) {

		String guid = UUID.randomUUID().toString();

		switch (operation) {

		case FileManagementConstant.BULK_UPLOAD:
			long count = uploadFileRepo.checkIfGuidPresent(guid);
			if (count != 0) {
				getGuid(FileManagementConstant.BULK_UPLOAD);
			} else {
				break;
			}
		}
		return guid;
	}

	public String getFilePath(String hashKey) {
		String path = hashKey.substring(0, 2) + "/" + hashKey.substring(2, 4) + "/";
		return "src/main/resources/" + path;
	}

	public ByteArrayInputStream writeToCsvFile(List<String> sortedConfifMapKeys) throws IOException {
		try (final ByteArrayOutputStream stream = new ByteArrayOutputStream();
				final CSVPrinter printer = new CSVPrinter(new PrintWriter(stream), CSVFormat.DEFAULT)) {
			printer.printRecord(sortedConfifMapKeys);
			printer.flush();
			return new ByteArrayInputStream(stream.toByteArray());
		} catch (final IOException e) {
			throw new IOException(ErrorCode.FILE_DOWNLOADING_ERROR);
		}
	}

	public ByteArrayInputStream writeToTxtFile(List<String> sortedConfifMapKeys, String delimiter) throws IOException {
		try (final ByteArrayOutputStream stream = new ByteArrayOutputStream();) {
			StringBuilder sb = new StringBuilder();
			for (String field : sortedConfifMapKeys) {
				sb.append(field);
				if (StringUtils.isNotEmpty(delimiter)) {
					sb.append(delimiter);
				}
			}
			stream.writeBytes(StringUtils.chop(sb.toString()).getBytes());
			return new ByteArrayInputStream(stream.toByteArray());
		} catch (final IOException e) {
			throw new IOException(ErrorCode.FILE_DOWNLOADING_ERROR);
		}

	}

	public ByteArrayInputStream writeToXlsFile(List<String> sortedConfifMapKeys) throws IOException {
		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
			Sheet sheet = workbook.createSheet();
			Row row = sheet.createRow(0);
			int cellCount = 0;
			for (String field : sortedConfifMapKeys) {
				row.createCell(cellCount).setCellValue(field);
			}
			workbook.write(stream);
			return new ByteArrayInputStream(stream.toByteArray());
		} catch (IOException e) {
			throw new IOException(ErrorCode.FILE_DOWNLOADING_ERROR);
		}
	}

}
