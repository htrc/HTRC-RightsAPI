package edu.indiana.d2i.htrc.rights;

import java.util.List;
import java.util.stream.Collectors;

public class FilterResultJson {
	List<String> filteredVolumeIdsList;
	List<String> invalidVolumeIdsList;
	
	public FilterResultJson(List<String> filteredVolumeIdsList, List<String> invalidVolumeIdsList) {
		super();
		this.filteredVolumeIdsList = filteredVolumeIdsList;
		this.invalidVolumeIdsList = invalidVolumeIdsList;
	}

	public List<String> getFilteredVolumeIdsList() {
		return filteredVolumeIdsList;
	}

	public void setFilteredVolumeIdsList(List<String> filteredVolumeIdsList) {
		this.filteredVolumeIdsList = filteredVolumeIdsList;
	}

	public List<String> getInvalidVolumeIdsList() {
		return invalidVolumeIdsList;
	}

	public void setInvalidVolumeIdsList(List<String> invalidVolumeIdsList) {
		this.invalidVolumeIdsList = invalidVolumeIdsList;
	}

	public int filteredVolumeIdsListSize() {
		return this.filteredVolumeIdsList.size();
	}
		
	public int invalidVolumeIdsListSize() {
		return this.invalidVolumeIdsList.size();
	}
	
	@Override
	public String toString() {
		return String.format("{ \"filteredVolumeIdsList\":%s, \"invalidVolumeIdsList\":%s }",
				listToString(this.filteredVolumeIdsList), listToString(this.invalidVolumeIdsList));
	}
	
	private String listToString(List<String> strList) {
		return strList.stream().collect(Collectors.joining(",", "[", "]"));
	}

//	public static FilterResultJson empty() {
//		FilterResultJson res = new FilterResultJson();
//		res.setVolumeIdsList(Collections.emptyList());
//		return res;
//	}
}
