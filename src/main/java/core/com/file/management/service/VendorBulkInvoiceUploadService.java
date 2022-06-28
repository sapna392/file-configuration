package core.com.file.management.service;

import java.io.InputStream;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import core.com.file.management.exception.VendorBulkUploadException;
import core.com.file.management.model.VendorBulkInvoiceUploadResponse;
import core.com.file.management.model.VendorTxnInvoiceRest;

public interface VendorBulkInvoiceUploadService {

	public List<VendorTxnInvoiceRest> upload(MultipartFile file, String imCode) throws VendorBulkUploadException;

	public VendorBulkInvoiceUploadResponse getUploadFileDetails(Pageable pageable, String status, String imCode);

	public MultipartFile getUploadFileById(Long id, String userId, String userType) throws VendorBulkUploadException;
	
	public InputStream download(String userId, String userType, String mediaType) throws VendorBulkUploadException;

}
