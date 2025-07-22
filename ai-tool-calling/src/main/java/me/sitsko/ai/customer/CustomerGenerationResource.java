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
	@Path("/{userId}/customer")
	public CustomerResponse generatePersons(@PathParam("userId") int userId) {
		return customerAiService.generatePersonList(userId);
	}

	@GET
	@Path("/{userId}/customer/{id}")
	public Customer getPersonById(@PathParam("userId") int userId, @PathParam("id") int id) {
		return customerAiService.getPersonById(userId, id);
	}


}
