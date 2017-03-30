package edu.indiana.d2i.htrc.rights;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	
	private static int numKeysPerMget = Integer.parseInt(DEFAULT_NUM_KEYS_PER_MGET);
	private static int numMgetsPerPipeline = Integer.parseInt(DEFAULT_NUM_MGETS_PER_PIPELINE);

	private JedisPool jedisPool;
	
	// initialize configuration settings for redis pipelining, using the parameters in Hub; this method should be called during initialization
	public static void initPipelineSettings() {
		numKeysPerMget = Integer.parseInt(Hub.getInitParams().getParamValue(ParamValues.REDIS_NUM_KEYS_PER_MGET_PARAM, DEFAULT_NUM_KEYS_PER_MGET));
		numMgetsPerPipeline = Integer.parseInt(Hub.getInitParams().getParamValue(ParamValues.REDIS_NUM_MGETS_PER_PIPELINE_PARAM, DEFAULT_NUM_MGETS_PER_PIPELINE));		
	}
	
	public RedisClient(String redisHost) {
		this.jedisPool = new JedisPool(new JedisPoolConfig(), redisHost);
	}
	
	public List<String> getKeyValuesPipelined(List<String> keys) {
		if (keys.size() == 0) {
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
        			// System.out.println("iteration: i = " + i + ", endIndex = " + endIndex);
        			// printArrayElems(keys.subList(i, endIndex).toArray(new String[numKeysPerMget]));
        			//System.out.println("  list = " + keys.subList(i, endIndex).stream().collect(Collectors.joining(",", "[", "]")));
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
}
