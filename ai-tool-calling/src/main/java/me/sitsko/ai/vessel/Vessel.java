package me.sitsko.ai.vessel;

import dev.langchain4j.model.output.structured.Description;

/**
 * @author Mikalai Sitsko , 06/27/2025
 */
public record Vessel(int imoNumber, String name, @Description("TEU size") int teuSize, String owner) {
}
