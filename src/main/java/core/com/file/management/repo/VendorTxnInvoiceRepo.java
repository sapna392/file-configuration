package core.com.file.management.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import core.com.file.management.entity.VendorTxnInvoiceEntity;

@Repository
public interface VendorTxnInvoiceRepo extends JpaRepository<VendorTxnInvoiceEntity, Long>{

	Page<VendorTxnInvoiceEntity> findByImCode(String imCode, Pageable pageable);

	Page<VendorTxnInvoiceEntity> findByStatusAndImCode(String status, String imCode, Pageable pageable);

}
