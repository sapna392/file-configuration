package core.com.file.management.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import core.com.file.management.entity.FileConfigurationEntity;

@Repository
public interface FileConfigurationRepo extends JpaRepository<FileConfigurationEntity, Long> {

	@Query("SELECT COUNT(1) FROM FileConfigurationEntity fce WHERE fce.imCode = ?1")
	long checkIfConfigurationExists(@Param("imCode") String imCode);
	
	@Query("SELECT NEW core.com.file.management.entity.FileConfigurationEntity ( fce.fileStructure, fce.fileDelimiter, "
			+ "fce.invoiceNumber, fce.invoiceAmount, fce.invoiceDate, fce.vendorCode, fce.vendorName, fce.dueDate, "
			+ "fce.paymentIdentifier, fce.processingDate, fce.additionalField1, fce.additionalField2, "
			+ "fce.additionalField3, fce.additionalField4, fce.additionalField5, fce.additionalField6, "
			+ "fce.additionalField7, fce.additionalField8, fce.additionalField9, fce.additionalField10) "
			+ "FROM FileConfigurationEntity fce "
			+ "WHERE fce.imCode = :imCode "
			+ "ORDER BY fce.updated DESC")
	List<FileConfigurationEntity> getFileConfiguration(@Param("imCode") String imCode);
	
}
