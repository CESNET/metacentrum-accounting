package cz.cesnet.meta.accounting.server.util;

import java.util.List;

import org.displaytag.pagination.PaginatedList;
import org.displaytag.properties.SortOrderEnum;

public class Page implements PaginatedList {
	private int fullListSize;
	private List<? extends Object> list;
	private int pageSize;
	private int pageNumber;
	private boolean ascending;
	private String sortColumn;

	@Override
	public int getFullListSize() {
		return fullListSize;
	}
	
	public void setFullListSize(Integer fullListSize) {
		this.fullListSize = fullListSize;
	}
	
	
	@Override
	public List<? extends Object> getList() {
		return list;
	}
	
	public void setList(List<? extends Object> list) {
		this.list = list;
	}

	@Override
	public int getObjectsPerPage() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
	
	@Override
	public int getPageNumber() {
		return pageNumber;
	}
	
	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}

	@Override
	public String getSearchId() {
		return null;
	}

	@Override
	public String getSortCriterion() {
		return sortColumn;
	}
	
	public void setSortColumn(String sortColumn) {
		this.sortColumn = sortColumn;
	}

	@Override
	public SortOrderEnum getSortDirection() {
		if (ascending) {
			return SortOrderEnum.ASCENDING;
		} else {
			return SortOrderEnum.DESCENDING;
		}		
	}
	
	public void setAscending(Boolean isAscending) {
		this.ascending = isAscending;
	}

}
