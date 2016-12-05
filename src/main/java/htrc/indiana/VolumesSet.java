package htrc.indiana;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VolumesSet {
	private static final Logger logger = LoggerFactory.getLogger(VolumesSet.class);

	private Set<String> vols;
	private ReadWriteLock volsLock; // allows multiple parallel readers, but only a single writer access to this.vols
	private String setName; // a name that describes the set, e.g., "level12Vols"; used in log messages
	
	public VolumesSet(String volFilePath, String setName) {
		this.vols = volIdsFromFileToSet(volFilePath);
		this.volsLock = new ReentrantReadWriteLock();
		this.setName = setName;
	}
		
	public int size() {
		int res;
		volsLock.readLock().lock();
		try {
			res = vols.size();
		} finally {
			volsLock.readLock().unlock();
		}
		return res;
	}
	
	// returns (volList - this.vols), i.e., the set difference of volList and this.vols
	public List<String> reverseDiff(List<String> volList, String listName) {
		List<String> res;
		long start = System.currentTimeMillis();
		volsLock.readLock().lock();
		try {
			res = volList.stream().filter(vol -> ! vols.contains(vol)).collect(Collectors.toList());
		} finally {
			volsLock.readLock().unlock();
		}
		long end = System.currentTimeMillis();

		logger.debug("Time for ({} - {}) = {} s; result size = {}", listName, this.setName, (end - start)/1000.0, res.size());
		return res;
	}
	
	// reads volume ids in the given file, and resets this.vols to the new set of volume ids
	public void reloadVolumes(String volsFilePath) {
		Set<String> newVols = volIdsFromFileToSet(volsFilePath);
		volsLock.writeLock().lock();
		this.vols = newVols;
		volsLock.writeLock().unlock();
	}

	private static Set<String> volIdsFromFileToSet(String volFilePath) {
		long start = System.currentTimeMillis();
		Set<String> res = streamToSet(readVolIdsFromFile(volFilePath));
		long end = System.currentTimeMillis();
		logger.debug("Time to read volume ids from {} to set = {} s; set size = {}", volFilePath, (end - start)/1000.0, res.size());
		return res;
	}
	
	private static Set<String> streamToSet(Stream<String> stream) {
		return stream.collect(Collectors.toCollection(HashSet::new));
	}

	private static Stream<String> readVolIdsFromFile(String filePath) {
		Stream<String> res = null;
		try {
 			res = Files.lines(Paths.get(filePath));
		} catch (IOException e) {
			logger.error("IOException while reading volume ids from " + filePath, e);
		}
		if (res == null)
			return Stream.empty();
		else
			return res;
	}

}
