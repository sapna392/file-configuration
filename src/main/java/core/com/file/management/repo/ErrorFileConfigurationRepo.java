package core.com.file.management.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import core.com.file.management.entity.ErrorFileConfigurationEntity;

@Repository
public interface ErrorFileConfigurationRepo extends JpaRepository<ErrorFileConfigurationEntity, Long>{
	
	@Query("SELECT COUNT(1) FROM ErrorFileConfigurationEntity efce WHERE efce.imCode = ?1")
	long checkIfConfigurationExists(@Param("imCode") String imCode);
	
	@Query("SELECT NEW core.com.file.management.entity.ErrorFileConfigurationEntity ( efce.fileStructure, "
			+ "efce.fileDelimiter, efce.fileId, efce.referenceNo, efce.creationTime, efce.invoiceNumber, "
			+ "efce.invoiceAmount, efce.reversalDate, efce.invoiceDate, efce.vendorCode, efce.vendorName, "
			+ "efce.status, efce.statusDescription, efce.echequeNo, efce.additionalField1, efce.additionalField2, "
			+ "efce.additionalField3, efce.additionalField4, efce.additionalField5, efce.additionalField6, "
			+ "efce.additionalField7, efce.additionalField8, efce.additionalField9, efce.additionalField10) "
			+ "FROM ErrorFileConfigurationEntity efce "
			+ "WHERE efce.imCode = :imCode "
			+ "ORDER BY efce.updated DESC")
	List<ErrorFileConfigurationEntity> getErrorFileConfiguration(@Param("imCode") String imCode);
	
	@Query("SELECT NEW core.com.file.management.entity.ErrorFileConfigurationEntity ( efce.fileStructure, "
			+ "efce.fileDelimiter, efce.fileId, efce.referenceNo, efce.creationTime, efce.invoiceNumber, "
			+ "efce.invoiceAmount, efce.reversalDate, efce.invoiceDate, efce.vendorCode, efce.vendorName, "
			+ "efce.status, efce.statusDescription, efce.echequeNo, efce.created, efce.createdBy) "
			+ "FROM ErrorFileConfigurationEntity efce "
			+ "WHERE efce.imCode = :imCode "
			+ "ORDER BY efce.updated DESC")
	List<ErrorFileConfigurationEntity> getErrorFileConfigurationWithoutAdditionalFields(
			@Param("imCode") String imCode);
	
	@Query("SELECT efce FROM ErrorFileConfigurationEntity efce WHERE efce.imCode =:imCode ORDER BY efce.updated DESC")
	List<ErrorFileConfigurationEntity> findByImCode(@Param("imCode") String imCode);
	
}
