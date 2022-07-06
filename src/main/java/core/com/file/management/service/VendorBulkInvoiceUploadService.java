package core.com.file.management.service;

import java.io.InputStream;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import core.com.file.management.exception.VendorBulkUploadException;
import core.com.file.management.model.ErrorUploadFileDetailsResponse;
import core.com.file.management.model.VendorBulkInvoiceUploadResponse;
import core.com.file.management.model.VendorBulkInvoiceUploadRest;

public interface VendorBulkInvoiceUploadService {

	public VendorBulkInvoiceUploadRest upload(MultipartFile file, String imCode) throws VendorBulkUploadException;

	public VendorBulkInvoiceUploadResponse getUploadFileDetails(Pageable pageable, String status, String imCode);

	public MultipartFile getUploadFileById(Long id, String imCode, Boolean isErrorFile) throws VendorBulkUploadException;

	public InputStream download(String userId, String mediaType) throws VendorBulkUploadException;

	public ErrorUploadFileDetailsResponse getReversalFileDetails(Pageable pageable, String status, String imCode,
			Boolean isErrorFile);
	
}
