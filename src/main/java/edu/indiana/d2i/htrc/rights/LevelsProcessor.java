package edu.indiana.d2i.htrc.rights;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LevelsProcessor {
	private static final Logger logger = LoggerFactory.getLogger(LevelsProcessor.class);
	
	private static final String DEFAULT_REDIS_VOLUME_ID_KEY_PREFIX = "volume:";
	private static final String DEFAULT_REDIS_VOLUME_ID_KEY_SUFFIX = ":info";
	private static final String DEFAULT_REDIS_ACCESS_LEVEL_HASH_FIELD = "access-level";
	private static final String DEFAULT_REDIS_AVAIL_STATUS_HASH_FIELD = "avail-status";

	// the following constant refers to the "level" parameter in the "filter" REST call
	private static final String LEVEL_SEP = "|";

	// the prefix added to a volume id to obtain a valid key in the database
	public static String volIdKeyPrefix = DEFAULT_REDIS_VOLUME_ID_KEY_PREFIX;
	// the suffix added to a volume id to obtain a valid key in the database
	public static String volIdKeySuffix = DEFAULT_REDIS_VOLUME_ID_KEY_SUFFIX;
	
	// the names of hash fields of keys in redis corresponding to access levels and availability status of volumes
	public static String accessLevelHashFieldName = DEFAULT_REDIS_ACCESS_LEVEL_HASH_FIELD;
	public static String availStatusHashFieldName = DEFAULT_REDIS_AVAIL_STATUS_HASH_FIELD;
	
	private List<String> volIdsList;
	private RedisClient redisClient;
	
	// initializes parameters using values in Hub.initParams; expected to be called at startup
	public static void initParams() {
		volIdKeyPrefix = Hub.getInitParams().getParamValue(ParamValues.REDIS_VOLUME_ID_KEY_PREFIX_PARAM, DEFAULT_REDIS_VOLUME_ID_KEY_PREFIX);
		volIdKeySuffix = Hub.getInitParams().getParamValue(ParamValues.REDIS_VOLUME_ID_KEY_SUFFIX_PARAM, DEFAULT_REDIS_VOLUME_ID_KEY_SUFFIX);
		accessLevelHashFieldName = Hub.getInitParams().getParamValue(ParamValues.REDIS_ACCESS_LEVEL_HASH_FIELD_PARAM, DEFAULT_REDIS_ACCESS_LEVEL_HASH_FIELD);
		availStatusHashFieldName = Hub.getInitParams().getParamValue(ParamValues.REDIS_AVAIL_STATUS_HASH_FIELD_PARAM, DEFAULT_REDIS_AVAIL_STATUS_HASH_FIELD);
	}
	
	public static Stream<String> parseLevels(String levelsArg) {
		// examples of valid values of levelsArg: "1A|1B", "1|2", "2a|2b", "3"
		if (levelsArg == null) {
			return Stream.empty();
		}

		return Stream.of(levelsArg.split(Pattern.quote(LEVEL_SEP)))
				.map(level -> level.trim().toUpperCase());
	}
	
	public LevelsProcessor(List<String> volIdsList) {
		this.volIdsList = volIdsList;
		this.redisClient = Hub.getRedisClient();
	}
    
	// return the volumes in this.volIdsList that have any of the data protection levels listed in filterLevels; note that the "startsWith" op is used 
	// instead of "equals" to allow us to conclude that a volume with data protection level "1A" is also at level 1
	public Optional<FilterResultJson> filterVolsByLevel(List<String> filterLevels) {
		List<String> volIdsKeys = this.volIdsList.stream().map(this::volumeIdToKey).collect(Collectors.toList());
		Optional<List<List<String>>> optVolRightsInfo = redisClient.getHashFieldValues(volIdsKeys, accessLevelHashFieldName, availStatusHashFieldName);
		return optVolRightsInfo.flatMap(volRightsInfo -> filterVolsByLevel(volRightsInfo, (String volLevel) -> filterLevels.stream()
				.anyMatch(filterLevel -> (filterLevel == null) ? (filterLevel == volLevel) : ((volLevel != null) && volLevel.startsWith(filterLevel)))));
	}
	   
    // given a list of the rights information (access level, availability status) for the volumes in this.volIdsList, and a predicate to test an
    // access level, this method returns a FilterResultJson object that contains
    // (a) the list of volume ids that are not available in HTRC Cassandra
    // (b) the list of available volume ids that satisfy the condition of the predicate levelTester
    // Returns an empty Optional if there is an error, e.g., unexpected values for rights info
    private Optional<FilterResultJson> filterVolsByLevel(List<List<String>> rightsInfo, Predicate<String> levelTester) {
    	Iterator<String> volIdsItr = this.volIdsList.iterator();
    	Iterator<List<String>> rightsInfoItr = rightsInfo.iterator();

    	List<String> unavailableAtHtrc = new ArrayList<String>();
    	List<String> filteredVols = new ArrayList<String>();

    	while (volIdsItr.hasNext() && rightsInfoItr.hasNext()) {
    		String volId = volIdsItr.next();
    		List<String> rights = rightsInfoItr.next();
    		if (rights.size() == 2) {
    			String accessLevel = rights.get(0);
    			String availStatus = rights.get(1);

    			if ((availStatus == null) || (availStatus.equals("false"))) {
    				unavailableAtHtrc.add(volId);
    			} else if (levelTester.test(accessLevel)) {
    				filteredVols.add(volId);
    			}
    		} else {
    			logger.error("filterVolsBylevel: expected access level and availability status fields of volume {}, but received {}", volId, rights.stream().collect(Collectors.joining(",", "[", "]")));
    			return Optional.empty();
    		}
    	}

    	return Optional.of(new FilterResultJson(filteredVols, unavailableAtHtrc));
    }
    
	private String volumeIdToKey(String volId) {
		  return volIdKeyPrefix + volId + volIdKeySuffix;
	}
}

