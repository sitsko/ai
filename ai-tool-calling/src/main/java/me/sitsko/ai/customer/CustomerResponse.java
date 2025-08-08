package me.sitsko.ai.customer;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**Wrapper for response
 *
 * @author Mikalai Sitsko , 06/25/2025
 */
@Setter
@Getter
public class CustomerResponse {

	private int listId;
	private List<Customer> customers;

}
