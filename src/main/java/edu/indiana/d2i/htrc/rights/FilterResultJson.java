package edu.indiana.d2i.htrc.rights;

import java.util.List;
import java.util.stream.Collectors;

public class FilterResultJson {
	List<String> volIdsAtFilterLevels; // volume ids that are at the specified filter levels
	List<String> volIdsUnavailableAtHtrc; // volume ids that are not available in the HTRC volume data store (Cassandra)
	
	public FilterResultJson(List<String> filteredVolumeIdsList, List<String> invalidVolumeIdsList) {
		super();
		this.volIdsAtFilterLevels = filteredVolumeIdsList;
		this.volIdsUnavailableAtHtrc = invalidVolumeIdsList;
	}

	public List<String> getVolIdsAtFilterLevels() {
		return volIdsAtFilterLevels;
	}

	public void setVolIdsAtFilterLevels(List<String> volIdsAtFilterLevels) {
		this.volIdsAtFilterLevels = volIdsAtFilterLevels;
	}

	public List<String> getVolIdsUnavailableAtHtrc() {
		return volIdsUnavailableAtHtrc;
	}

	public void setVolIdsUnavailableAtHtrc(List<String> volIdsUnavailableAtHtrc) {
		this.volIdsUnavailableAtHtrc = volIdsUnavailableAtHtrc;
	}

	public int volIdsAtFilterLevelsSize() {
		return this.volIdsAtFilterLevels.size();
	}
		
	public int volIdsUnavailableAtHtrcSize() {
		return this.volIdsUnavailableAtHtrc.size();
	}
	
	@Override
	public String toString() {
		return String.format("{ \"volIdsAtFilterLevels\":%s, \"volIdsUnavailableAtHtrc\":%s }",
				listToString(this.volIdsAtFilterLevels), listToString(this.volIdsUnavailableAtHtrc));
	}
	
	private String listToString(List<String> strList) {
		return strList.stream().collect(Collectors.joining(",", "[", "]"));
	}
}
