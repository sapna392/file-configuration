package core.com.file.management.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import core.com.file.management.model.VendorTxnInvoiceResponse;
import core.com.file.management.model.VendorTxnInvoiceRest;

public interface VendorTxnInvoiceService {

	public VendorTxnInvoiceResponse getVendorTxnInvoiceDetails(Pageable pageable, String status, String imCode);
	
	public List<VendorTxnInvoiceRest> authorizeTransaction(List<VendorTxnInvoiceRest> vendorTxnInvoiceRestList);
}
