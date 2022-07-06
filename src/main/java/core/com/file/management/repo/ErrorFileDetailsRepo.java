/**
 * created by supro
 */
package core.com.file.management.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import core.com.file.management.entity.ErrorFileDetailsEntity;

@Repository
public interface ErrorFileDetailsRepo extends JpaRepository<ErrorFileDetailsEntity, Long> {

	Page<ErrorFileDetailsEntity> findByImCodeAndIsErrorFile(String imCode, Boolean isErrorFile ,Pageable pageable);
	
	Page<ErrorFileDetailsEntity> findByStatusAndImCodeAndIsErrorFile(String status, String imCode,
			Boolean isErrorFile, Pageable pageable);
	
	@Query("SELECT COUNT(1) FROM ErrorFileDetailsEntity efde WHERE efde.guid = :guid")
	public long checkIfGuidPresent(@Param("guid") String guid);
	
	@Query("SELECT efde FROM ErrorFileDetailsEntity efde "
			+ "WHERE efde.id = :id AND efde.imCode = :imCode AND efde.isErrorFile =:isErrorFile "
			+ "ORDER BY efde.updated DESC")
	ErrorFileDetailsEntity getFileById(@Param("id") Long id, @Param("imCode") String imCode,
			@Param("isErrorFile") Boolean isErrorFile);
}
