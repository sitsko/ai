package me.sitsko.ai.vessel;

import dev.langchain4j.model.output.structured.Description;

/**
 * DTO for vessel information used in the AI tool calling application.
 * @author Mikalai Sitsko , 06/27/2025
 */
public record Vessel(
		@Description("Vessel IMO number")
		int imoNumber,

		@Description("Last official vessel name")
		String name,

		@Description("TEU size")
		int teuSize,

		@Description("Vessel owner")
		String owner) {
}
