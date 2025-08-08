package me.sitsko.ai.customer;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Endpoint for customers
 * @author Mikalai Sitsko , 06/25/2025
 */
@RegisterAiService
@ApplicationScoped
public interface CustomerAiService {

	@SystemMessage("""
        You are a helpful assistant that generates realistic random person data.
        Always respond with valid JSON format.
        """)
	@UserMessage("""
        Generate exactly 3 unique persons.
        If the previously list of persons was generated in the past, find and return previous list.
        Otherwise a new list of persons should be generated.
        Set number of list as {listId}.
        
        Requirements:
        - Each person must have a unique integer ID (like 1, 2, 3, etc.)
        - Use realistic first and last names
        - Return ONLY the JSON array and include, no additional text
        """)
	CustomerResponse generatePersonList(@MemoryId int listId);

	@SystemMessage("""
        You are a helpful assistant that can recall generated person data from chat memory.
        """)
	@UserMessage("""
        In the previously generated list of persons with number of list {listId}, find and return the person with id {id}.
        
        Return ONLY the JSON object, no additional text.
        """)
	Customer getPersonById(@MemoryId int listId, int id);

}
