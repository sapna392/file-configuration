package core.com.file.management.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import core.com.file.management.entity.VendorTxnInvoiceEntity;

@Repository
public interface VendorBulkUploadFileRepo extends JpaRepository<VendorTxnInvoiceEntity, Long>{

}
