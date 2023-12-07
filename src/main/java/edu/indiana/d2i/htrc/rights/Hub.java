package edu.indiana.d2i.htrc.rights;

import java.util.Enumeration;

import javax.servlet.ServletConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hub {	
	private static final Logger logger = LoggerFactory.getLogger(Hub.class);

	private static ParamValues initParams = null;
	private static RedisClient redisClient = null;
	
	public static void init(ServletConfig servletConfig) {
		// LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		// print logback's internal status
		// StatusPrinter.print(lc);
		
		initializeParams(servletConfig);
		logger.debug("Servlet params: {}", initParams);
		
		LevelsProcessor.initParams();
		RedisClient.initParams();
		
		redisClient = new RedisClient(initParams.getParamValue(ParamValues.REDIS_HOST_PARAM), Integer.parseInt(initParams.getParamValue(ParamValues.REDIS_PORT_PARAM)), Integer.parseInt(initParams.getParamValue(ParamValues.REDIS_TIMEOUT_PARAM)), initParams.getParamValue(ParamValues.REDIS_PASSWORD_PARAM));
	}
	
	public static ParamValues getInitParams() {
		return initParams;
	}
	
	public static RedisClient getRedisClient() {
		return redisClient;
	}
     
	// initialize parameters to the servlet
	private static void initializeParams(ServletConfig servletConfig) {
		logger.debug("Initializing servlet parameters ...");
		Enumeration<String> paramNames = servletConfig.getInitParameterNames();
		initParams = new ParamValues();
		while (paramNames.hasMoreElements()) {
			String paramName = paramNames.nextElement();
			initParams.setParamValue(paramName, servletConfig.getInitParameter(paramName));
		}
	}
}
