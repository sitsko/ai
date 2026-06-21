package dev.sitsko.ai.observability;

public record TokenUsage(String model, String agent, int tokenCount) {}
