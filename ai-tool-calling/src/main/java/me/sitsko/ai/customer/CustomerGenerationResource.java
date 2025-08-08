package me.sitsko.ai.customer;

/**
 * @author Mikalai Sitsko , 06/25/2025
 */

import jakarta.ws.rs.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;


@Slf4j
@RequiredArgsConstructor
@Path("/api")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class CustomerGenerationResource {

	private final CustomerAiService customerAiService;

	@GET
	@Path("/lists/{listId}")
	public CustomerResponse generatePersons(@PathParam("listId") int listId) {
		return customerAiService.generatePersonList(listId);
	}

	@GET
	@Path("/lists/{listId}/customers/{id}")
	public Customer getPersonById(@PathParam("listId") int listId, @PathParam("id") int id) {
		return customerAiService.getPersonById(listId, id);
	}


}
