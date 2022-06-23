package core.com.file.management.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import core.com.file.management.entity.BulkUploadFileEntity;

@Repository
public interface BulkUploadFileRepo extends JpaRepository<BulkUploadFileEntity, Long> {

	Page<BulkUploadFileEntity> findByImCode(String imCode, Pageable pageable);

	Page<BulkUploadFileEntity> findByStatusAndImCode(String status, String imCode, Pageable pageable);

	@Query("SELECT COUNT(1) FROM BulkUploadFileEntity bu WHERE bu.guid = :guid")
	public long checkIfGuidPresent(@Param("guid") String guid);

	@Query("SELECT bue FROM BulkUploadFileEntity bue "
			+ "WHERE bue.id = :id AND bue.imCode = :imCode ORDER BY bue.updated DESC")
	BulkUploadFileEntity getFileById(@Param("id") Long id, @Param("imCode") String imCode);
}
