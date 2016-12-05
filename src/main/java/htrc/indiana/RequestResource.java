package htrc.indiana;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import java.time.LocalTime;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
public class RequestResource {
	private static final Logger logger = LoggerFactory.getLogger(RequestResource.class);
	
	@Path("reload-rights")
	@PUT
	public void reloadRightsData() {
		logger.debug("Received PUT reload-rights");
		Hub.getLevel12Vols().reloadVolumes(Hub.getInitParams().getParamValue(ParamValues.LEVEL12_VOLS_FILEPATH_PARAM));
		logger.debug("Completed processing for reload-rights request");
	}
	
	@Path("filter")
	@POST
    @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public VolumeIdsJson processJsonPost(@QueryParam("level") int level, VolumeIdsJson input) throws JSONException {
		logger.debug("Received POST filter, level = {}, input list size = {}", level, input.size());
		
		if (level == 0) {
			// return an empty list, since no volumes are at level 0
			return VolumeIdsJson.empty();
		} else if (level != 3) {
			String errorMsg = "<html><body>Unsupported value of param \"level\".</body></html>";
			throw new WebApplicationException(Response.status(400).entity(errorMsg).build());
		}
		
		List<String> resVols = Hub.getLevel12Vols().reverseDiff(input.getVolumeIdsList(), "inputList[" + input.size() + "]");
		VolumeIdsJson res = new VolumeIdsJson();
		res.setVolumeIdsList(resVols);
		
		logger.debug("Completed processing for filter request, level = {}, input list size = {}", level, input.size());

		return res;	
	}
}
