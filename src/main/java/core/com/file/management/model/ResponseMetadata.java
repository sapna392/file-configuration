/**
 * created by supro
 */
package core.com.file.management.model;

public class ResponseMetadata {
	
	private long totalPages;
	private Integer page;
	private int size;
	private long elements;
	
	/**
	 * @return the totalPages
	 */
	public long getTotalPages() {
		return totalPages;
	}
	/**
	 * @param totalPages the totalPages to set
	 */
	public void setTotalPages(long totalPages) {
		this.totalPages = totalPages;
	}
	/**
	 * @return the page
	 */
	public Integer getPage() {
		return page;
	}
	/**
	 * @param page the page to set
	 */
	public void setPage(Integer page) {
		this.page = page;
	}
	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}
	/**
	 * @param size the size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}
	/**
	 * @return the elements
	 */
	public long getElements() {
		return elements;
	}
	/**
	 * @param elements the elements to set
	 */
	public void setElements(long elements) {
		this.elements = elements;
	}
	
}
