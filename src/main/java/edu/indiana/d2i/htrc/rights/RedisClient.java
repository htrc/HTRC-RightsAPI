package edu.indiana.d2i.htrc.rights;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

public class RedisClient {
	private static final Logger logger = LoggerFactory.getLogger(RedisClient.class);

	private static final String DEFAULT_NUM_KEYS_PER_MGET = "1000";
	private static final String DEFAULT_NUM_MGETS_PER_PIPELINE = "1000";
	private static final String DEFAULT_NUM_HMGETS_PER_PIPELINE = "1000";
	
	private static int numKeysPerMget = Integer.parseInt(DEFAULT_NUM_KEYS_PER_MGET);
	private static int numMgetsPerPipeline = Integer.parseInt(DEFAULT_NUM_MGETS_PER_PIPELINE);
	private static int numHmgetsPerPipeline = Integer.parseInt(DEFAULT_NUM_HMGETS_PER_PIPELINE);

	private JedisPool jedisPool;
	
	// initializes parameters using values in Hub.initParams; this method should be called at startup
	public static void initParams() {
		numKeysPerMget = Integer.parseInt(Hub.getInitParams().getParamValue(ParamValues.REDIS_NUM_KEYS_PER_MGET_PARAM, DEFAULT_NUM_KEYS_PER_MGET));
		numMgetsPerPipeline = Integer.parseInt(Hub.getInitParams().getParamValue(ParamValues.REDIS_NUM_MGETS_PER_PIPELINE_PARAM, DEFAULT_NUM_MGETS_PER_PIPELINE));		
		numHmgetsPerPipeline = Integer.parseInt(Hub.getInitParams().getParamValue(ParamValues.REDIS_NUM_HMGETS_PER_PIPELINE_PARAM, DEFAULT_NUM_HMGETS_PER_PIPELINE));		
	}
	
	public RedisClient(String redisHost, int redisPort, int redisTimeout, String redisPassword) {
		this.jedisPool = new JedisPool(new JedisPoolConfig(), redisHost,redisPort, redisTimeout,redisPassword);
	}
	
	public List<String> getKeyValues(List<String> keys) {
		if ((keys == null) || (keys.size() == 0)) {
			return Collections.emptyList();
		}
		
        try (Jedis jedis = this.jedisPool.getResource()) {
        	Pipeline pipeline = jedis.pipelined();
        	int i = 0; 
        	int numMgets = 0;
        	int size = keys.size();
        	List<String> res = new ArrayList<String>();
        	List<Response<List<String>>> batchRes;
        	long start = System.currentTimeMillis();
        	while (i < size) {
        		batchRes = new ArrayList<Response<List<String>>>(numMgetsPerPipeline);
        		while ((i < size) && (numMgets < numMgetsPerPipeline)) {
        			int endIndex = Integer.min(i + numKeysPerMget, size);
        			batchRes.add(pipeline.mget(keys.subList(i, endIndex).toArray(new String[endIndex - i])));
        			i += numKeysPerMget;
        			numMgets++;
        		}
        		pipeline.sync();
        		numMgets = 0;
        		batchRes.forEach(response -> res.addAll(response.get()));
        	}
        	long end = System.currentTimeMillis();	
        	logger.info("Time to get values for {} keys = {} seconds", keys.size(), (end - start)/1000.0);
        	return res;
        } catch (Exception e) {
        	logger.error("Exception while trying to access redis: {}", e.getMessage(), e); 
        	return Collections.emptyList();
        }
	}
	
	// returns the values of the specified fields of the hashes at the given keys in redis; the requests to redis are pipelined
	// the result is a list where each element is a list of the values of the hash fields for one key; so, if n field names are specified then,
	// each element in the result list is a list of size n; returns an empty Optional in case of error or exception
	public Optional<List<List<String>>> getHashFieldValues(List<String> keys, String... fieldNames) {
		if ((keys == null) || (keys.size() == 0)) {
			return Optional.of(Collections.emptyList());
		}
		
        try (Jedis jedis = this.jedisPool.getResource()) {
        	Pipeline pipeline = jedis.pipelined();
        	int size = keys.size();
        	int i = 0;
        	List<List<String>> res = new ArrayList<List<String>>();
        	while (i < size) {
        		int numHmgets = 0;
        		List<Response<List<String>>> batchRes = new ArrayList<Response<List<String>>>(numHmgetsPerPipeline);
        		while ((i < size) && (numHmgets < numHmgetsPerPipeline)) {
        			batchRes.add(pipeline.hmget(keys.get(i), fieldNames));
        			i++;
        			numHmgets++;
        		}
        		pipeline.sync();
        		batchRes.forEach(response -> res.add(response.get()));
        	}
        	return Optional.of(res);
        } catch (Exception e) {
        	logger.error("getHashFieldValues: exception while trying to access redis, {}", e.getMessage(), e); 
        	return Optional.empty();
        }
	}
	
	// returns the values of the specified field of the hashes at the given keys in redis; the requests to redis are pipelined; notice that this
	// method is optimized for the case when only one hash field needs to be retrieved; for more than one hash fields, use getHashFieldValues
	// returns an empty Optional in case of error or exception
	public Optional<List<String>> getSingleHashFieldValue(List<String> keys, String fieldName) {
		if ((keys == null) || (keys.size() == 0)) {
			return Optional.of(Collections.emptyList());
		}
		
        try (Jedis jedis = this.jedisPool.getResource()) {
        	Pipeline pipeline = jedis.pipelined();
        	int size = keys.size();
        	int i = 0;
        	List<String> res = new ArrayList<String>();
        	while (i < size) {
        		int numHgets = 0;
        		List<Response<String>> batchRes = new ArrayList<Response<String>>(numHmgetsPerPipeline);
        		while ((i < size) && (numHgets < numHmgetsPerPipeline)) {
        			batchRes.add(pipeline.hget(keys.get(i), fieldName));
        			i++;
        			numHgets++;
        		}
        		pipeline.sync();
        		batchRes.forEach(response -> res.add(response.get()));
        	}
        	return Optional.of(res);
        } catch (Exception e) {
        	logger.error("getSingleHashFieldValue: exception while trying to access redis, {}", e.getMessage(), e); 
        	return Optional.empty();
        }
	}
}
