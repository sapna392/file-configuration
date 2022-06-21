package core.com.file.management.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import core.com.file.management.entity.ReverseFileConfigurationEntity;

@Repository
public interface ReverseFileConfigurationRepo extends JpaRepository<ReverseFileConfigurationEntity, Long>{
	
	@Query("SELECT COUNT(1) FROM ReverseFileConfigurationEntity rfce WHERE rfce.imCode = ?1")
	long checkIfConfigurationExists(@Param("imCode") String imCode);
	
	@Query("SELECT NEW core.com.file.management.entity.ReverseFileConfigurationEntity ( rfce.fileStructure, "
			+ "rfce.fileDelimiter, rfce.fileId, rfce.referenceNo, rfce.creationTime, rfce.invoiceNumber, "
			+ "rfce.invoiceAmount, rfce.reversalDate, rfce.invoiceDate, rfce.vendorCode, rfce.vendorName, "
			+ "rfce.status, rfce.statusDescription, rfce.echequeNo, rfce.additionalField1, rfce.additionalField2, "
			+ "rfce.additionalField3, rfce.additionalField4, rfce.additionalField5, rfce.additionalField6, "
			+ "rfce.additionalField7, rfce.additionalField8, rfce.additionalField9, rfce.additionalField10) "
			+ "FROM ReverseFileConfigurationEntity rfce "
			+ "WHERE rfce.imCode = :imCode "
			+ "ORDER BY rfce.updated DESC")
	List<ReverseFileConfigurationEntity> getReverseFileConfiguration(@Param("imCode") String imCode);
	
}
