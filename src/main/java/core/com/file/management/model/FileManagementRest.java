/**
 * created by supro
 */
package core.com.file.management.model;

import lombok.Data;

/**
 * @author supro
 *
 */
@Data
public class FileManagementRest {
	
	private String fileId;
	private String fileName;
	private String fileType;
	private String content;

}
