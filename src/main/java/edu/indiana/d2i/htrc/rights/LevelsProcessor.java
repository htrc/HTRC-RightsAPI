package edu.indiana.d2i.htrc.rights;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LevelsProcessor {
	private static final Logger logger = LoggerFactory.getLogger(LevelsProcessor.class);
	
	private static final String DEFAULT_REDIS_VOLUME_ID_KEY_SUFFIX = ":level";

	// the following constant refers to the "level" parameter in the "filter" REST call
	private static final String LEVEL_SEP = "|";

	// the suffix added to a volume id to obtain a valid key in the database
	public static String volIdKeySuffix = DEFAULT_REDIS_VOLUME_ID_KEY_SUFFIX;
	
	private List<String> volIdsList;
	private RedisClient redisClient;
	
	// initializes parameters using values in Hub.initParams; expected to be called at startup
	public static void initParams() {
	  volIdKeySuffix = Hub.getInitParams().getParamValue(ParamValues.REDIS_VOLUME_ID_KEY_SUFFIX_PARAM, DEFAULT_REDIS_VOLUME_ID_KEY_SUFFIX);
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
	public FilterResultJson filterVolsAtLevel(List<String> filterLevels) {
		List<String> volIdsKeys = volIdsList.stream().map(this::volumeIdToKey).collect(Collectors.toList());
		List<String> volIdsLevels = redisClient.getKeyValuesPipelined(volIdsKeys);
		List<String> filteredVolIds = filterVolsAtLevel(volIdsLevels, (String volLevel) -> filterLevels.stream()
				.anyMatch(filterLevel -> (filterLevel == null) ? (filterLevel == volLevel) : ((volLevel != null) && volLevel.startsWith(filterLevel))));
		List<String> invalidVolIds = filterVolsAtLevel(volIdsLevels, (String volLevel) -> (volLevel == null));
		return new FilterResultJson(filteredVolIds, invalidVolIds);
	}
	
    private List<String> filterVolsAtLevel(List<String> volLevels, Predicate<String> levelTester) {
    	Iterator<String> volIdsItr = this.volIdsList.iterator();
    	Iterator<String> volLevelsItr = volLevels.iterator();
    	
    	List<String> res = new ArrayList<String>();
    	
    	while (volIdsItr.hasNext() && volLevelsItr.hasNext()) {
    		if (levelTester.test(volLevelsItr.next())) {
    			res.add(volIdsItr.next());
    		} else {
    			// consume the next element of volIdsItr
    			volIdsItr.next();
    		}
    	}
    	return res;    	
    }
    
	private String volumeIdToKey(String volId) {
		  return volId + volIdKeySuffix;
	}
}
