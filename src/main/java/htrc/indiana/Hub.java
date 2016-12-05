package htrc.indiana;

import java.util.Enumeration;
import javax.servlet.ServletConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hub {	
	private static final Logger logger = LoggerFactory.getLogger(Hub.class);

	private static ParamValues initParams = null;
	private static VolumesSet level12Vols = null;
	
	public static void init(ServletConfig servletConfig) {
		// LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		// print logback's internal status
		// StatusPrinter.print(lc);
		
		initializeParams(servletConfig);
		logger.debug("Servlet params: {}", initParams);

		initLevel12Vols();
	}
	
	public static ParamValues getInitParams() {
		return initParams;
	}
	
	public static VolumesSet getLevel12Vols() {
		return level12Vols;
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
	
	// read the volume ids at data protection levels 1 and 2, and initialize level12Vols
	private static void initLevel12Vols() {
		logger.debug("Initializing set of volumes at levels 1, 2 ...");
		String level12VolsFilePath = initParams.getParamValue(ParamValues.LEVEL12_VOLS_FILEPATH_PARAM);
		level12Vols = new VolumesSet(level12VolsFilePath, "level12Vols");
	}
}
