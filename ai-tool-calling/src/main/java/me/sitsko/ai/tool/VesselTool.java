package me.sitsko.ai.tool;

import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import me.sitsko.ai.vessel.Vessel;

import java.util.List;

/**
 * @author Mikalai Sitsko , 06/27/2025
 */
@ApplicationScoped
public class VesselTool {

	@Tool("Return vessels information")
	public List<Vessel> retrieveVessels() {

		return List.of(
				// Hapag‑Lloyd
				new Vessel(9474419, "Glovis Universe", 13560, "Hapag‑Lloyd"),
				new Vessel(9473439, "Hamburg Express", 13177, "Hapag‑Lloyd"),
				new Vessel(9708851, "Barzan", 19870, "Hapag‑Lloyd"),

// Maersk
				new Vessel(9884200, "Ane Maersk", 16500, "Maersk Line"),
				new Vessel(9998874, "Madrid Maersk", 20568, "Maersk Line"),
				new Vessel(9720095, "Algeciras", 23400, "Maersk Line"),

// MSC
				new Vessel(9690465, "MSC Fabiola", 12562, "MSC"),
				new Vessel(9690477, "MSC Flaminia", 12562, "MSC"),
				new Vessel(9690489, "MSC Oscar", 19224, "MSC"),

// Evergreen
				new Vessel(9893890, "Ever Ace", 24005, "Evergreen Marine Corp."),
				new Vessel(9893979, "Ever Aperture", 24006, "Evergreen Marine Corp."),
				new Vessel(9893967, "Ever Alot", 24007, "Evergreen Marine Corp."),

// ONE
				new Vessel(9511290, "ONE Apus", 14144, "Ocean Network Express"),
				new Vessel(9686666, "ONE Stork", 14144, "Ocean Network Express"),
				new Vessel(9731440, "ONE Mechanical", 14144, "Ocean Network Express")
		);
	}
}
