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

	private static final String HAPAG_LLOYD = "Hapag‑Lloyd";
	private static final String MAERSK = "Maersk Line";
	private static final String MSC = "MSC";
	private static final String EVERGREEN = "Evergreen Marine Corp.";
	private static final String ONE = "Ocean Network Express";

	private static final List<Vessel> VESSELS_MOCK = List.of(
			// Hapag‑Lloyd
			new Vessel(9540118, "Berlin Express", 23_661, HAPAG_LLOYD),
			new Vessel(9943865, "Hamburg Express", 23_662, HAPAG_LLOYD),
			new Vessel(9943877, "Gdansk Express", 23_663, HAPAG_LLOYD),
			new Vessel(9708851, "Barzan", 19_870, HAPAG_LLOYD),

			// Maersk
			new Vessel(9884200, "Ane Maersk", 16_500, MAERSK),
			new Vessel(9998874, "Madrid Maersk", 20_568, MAERSK),
			new Vessel(9720095, "Algeciras", 23_400, MAERSK),

			// MSC
			new Vessel(9690465, "MSC Fabiola", 12_562, MSC),
			new Vessel(9690477, "MSC Flaminia", 12_562, MSC),
			new Vessel(9690489, "MSC Oscar", 19_224, MSC),

			// Evergreen
			new Vessel(9893890, "Ever Ace", 24_005, EVERGREEN),
			new Vessel(9893979, "Ever Aperture", 24_006, EVERGREEN),
			new Vessel(9893967, "Ever Alot", 24_007, EVERGREEN),

			// ONE
			new Vessel(9511290, "ONE Apus", 14_144, ONE),
			new Vessel(9686666, "ONE Stork", 14_144, ONE),
			new Vessel(9731440, "ONE Mechanical", 14_144, ONE),

			new Vessel(7777777, "dummy vesel", 88_888, EVERGREEN)
			//new Vessel(7777777, "Real vesel", 88_888, EVERGREEN)
	);

	@Tool("Return generic vessels information such as IMO number, name, TEU size and owner.")
	public List<Vessel> retrieveVessels() {
		return VESSELS_MOCK;
	}
}
