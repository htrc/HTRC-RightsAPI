package edu.indiana.d2i.htrc.rights;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
public class RequestResource {
	private static final Logger logger = LoggerFactory.getLogger(RequestResource.class);
	
	@Path("filter")
	@POST
    @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public FilterResultJson processJsonPost(@QueryParam("level") String level, VolumeIdsJson input) throws JSONException {
		List<String> filterLevels = LevelsProcessor.parseLevels(level).collect(Collectors.toList());
		logger.debug("Received POST filter, level = \"{}\", parsed levels = {}, input list size = {}", level, 
				filterLevels.stream().collect(Collectors.joining(",", "[", "]")), input.size());
		
		if (level == null) {
			String errorMsg = "<html><body>Parameter \"level\" should have a non-null value.</body></html>";
			throw new WebApplicationException(Response.status(400).entity(errorMsg).build());			
//		} else if (level.equals("0")) {
//			// return an empty list, since no volumes are at level 0
//			return VolumeIdsJson.empty();
		} else {
			FilterResultJson res = (new LevelsProcessor(input.getVolumeIdsList())).filterVolsAtLevel(filterLevels);
//			VolumeIdsJson res = new VolumeIdsJson();
//			res.setVolumeIdsList(resVols);
		
			logger.debug("Completed processing for filter request, level = {}, input list size = {}, numFilteredVols = {}, numInvalidVols = {}", 
					level, input.size(), res.filteredVolumeIdsListSize(), res.invalidVolumeIdsListSize());

			return res;
		}
	}
}
