package htrc.indiana;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class VolumeIdsJson {
	List<String> volumeIdsList;
	
	public List<String> getVolumeIdsList() {
		return volumeIdsList;
	}

	public void setVolumeIdsList(List<String> volumeIdsList) {
		this.volumeIdsList = volumeIdsList;
	}

	public int size() {
		return volumeIdsList.size();
	}
		
	@Override
	public String toString() {
		return volumeIdsList.stream().collect(Collectors.joining(",", "[", "]"));
	}

	public static VolumeIdsJson empty() {
		VolumeIdsJson res = new VolumeIdsJson();
		res.setVolumeIdsList(Collections.emptyList());
		return res;
	}
}
