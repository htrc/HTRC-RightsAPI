package htrc.indiana;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ParamValues {
	public static final String LEVEL12_VOLS_FILEPATH_PARAM = "level12.vols.filepath";

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

	@Override
	public String toString() {
		return paramValues.entrySet().stream().map(entry -> "(" + entry.getKey() + ", " + entry.getValue() + ")").collect(Collectors.joining(", ", "[", "]"));
	}
	
}
