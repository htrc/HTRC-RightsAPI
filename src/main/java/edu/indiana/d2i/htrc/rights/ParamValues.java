package edu.indiana.d2i.htrc.rights;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ParamValues {
	public static final String REDIS_HOST_PARAM = "redis.host";
	public static final String REDIS_PORT_PARAM = "redis.port";
    public static final String REDIS_PASSWORD_PARAM = "redis.password";
	public static final String REDIS_TIMEOUT_PARAM = "redis.timeout";
	public static final String REDIS_NUM_KEYS_PER_MGET_PARAM = "redis.num.keys.per.mget";
	public static final String REDIS_NUM_MGETS_PER_PIPELINE_PARAM = "redis.num.mgets.per.pipeline";
	public static final String REDIS_NUM_HMGETS_PER_PIPELINE_PARAM = "redis.num.hmgets.per.pipeline";
	public static final String REDIS_VOLUME_ID_KEY_PREFIX_PARAM = "redis.volume.id.key.prefix";
	public static final String REDIS_VOLUME_ID_KEY_SUFFIX_PARAM = "redis.volume.id.key.suffix";
	public static final String REDIS_ACCESS_LEVEL_HASH_FIELD_PARAM = "redis.access.level.hash.field";
	public static final String REDIS_AVAIL_STATUS_HASH_FIELD_PARAM = "redis.avail.status.hash.field";

	Map<String, String> paramValues = null;
	
	public ParamValues() {
		paramValues = new HashMap<String, String>();
	}
	
	public void setParamValue(String name, String value) {
		paramValues.put(name,  value);
	}
	
	public String getParamValue(String name) {
		return paramValues.get(name);
	}

	public String getParamValue(String name, String defaultValue) {
		return paramValues.getOrDefault(name, defaultValue);
	}

	@Override
	public String toString() {
		return paramValues.entrySet().stream().map(entry -> "(" + entry.getKey() + ", " + entry.getValue() + ")").collect(Collectors.joining(", ", "[", "]"));
	}
	
}
