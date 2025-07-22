package me.sitsko.ai.vessel;

import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Simulate external service for retrieving vessel information.
 *
 * @author Mikalai Sitsko , 06/27/2025
 */
@ApplicationScoped
public class VesselTool {

	public static final String HAPAG_LLOYD = "Hapag‑Lloyd";
	public static final String MAERSK_LINE = "Maersk Line";
	public static final String MSC = "MSC";
	public static final String EVERGREEN_MARINE_CORP = "Evergreen Marine Corp.";
	public static final String ONE = "Ocean Network Express";

	@Tool("Return generic vessels information such as IMO number, name, TEU size and owner.")
	public List<Vessel> retrieveVessels() {

		return List.of(
				// Hapag‑Lloyd
				new Vessel(9540118, "Berlin Express", 23664, HAPAG_LLOYD),
				new Vessel(9943865, "Hamburg Express", 23664, HAPAG_LLOYD),
				new Vessel(9943877, "Gdansk Express", 23664, HAPAG_LLOYD),
				new Vessel(9708851, "Barzan", 19870, HAPAG_LLOYD),

// Maersk
				new Vessel(9884200, "Ane Maersk", 16500, MAERSK_LINE),
				new Vessel(9998874, "Madrid Maersk", 20568, MAERSK_LINE),
				new Vessel(9720095, "Algeciras", 23400, MAERSK_LINE),

// MSC
				new Vessel(9690465, "MSC Fabiola", 12562, MSC),
				new Vessel(9690477, "MSC Flaminia", 12562, MSC),
				new Vessel(9690489, "MSC Oscar", 19224, MSC),

// Evergreen
				new Vessel(9893890, "Ever Ace", 24005, EVERGREEN_MARINE_CORP),
				new Vessel(9893979, "Ever Aperture", 24006, EVERGREEN_MARINE_CORP),
				new Vessel(9893967, "Ever Alot", 24007, EVERGREEN_MARINE_CORP),

// ONE
				new Vessel(9511290, "ONE Apus", 14144, ONE),
				new Vessel(9686666, "ONE Stork", 14144, ONE),
				new Vessel(9731440, "ONE Mechanical", 14144, ONE)
		);
	}
}
