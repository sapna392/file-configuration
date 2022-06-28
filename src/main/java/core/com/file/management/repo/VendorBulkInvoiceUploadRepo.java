package core.com.file.management.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import core.com.file.management.entity.VendorBulkInvoiceUploadEntity;

@Repository
public interface VendorBulkInvoiceUploadRepo extends JpaRepository<VendorBulkInvoiceUploadEntity, Long> {

	Page<VendorBulkInvoiceUploadEntity> findByImCode(String imCode, Pageable pageable);

	Page<VendorBulkInvoiceUploadEntity> findByStatusAndImCode(String status, String imCode, Pageable pageable);

	@Query("SELECT COUNT(1) FROM VendorBulkInvoiceUploadEntity vbiue WHERE vbiue.guid = :guid")
	public long checkIfGuidPresent(@Param("guid") String guid);

	@Query("SELECT vbiue FROM VendorBulkInvoiceUploadEntity vbiue "
			+ "WHERE vbiue.id = :id AND vbiue.imCode = :imCode ORDER BY vbiue.updated DESC")
	VendorBulkInvoiceUploadEntity getFileById(@Param("id") Long id, @Param("imCode") String imCode);
}
