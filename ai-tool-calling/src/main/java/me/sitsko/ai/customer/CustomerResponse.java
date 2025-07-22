package me.sitsko.ai.customer;

import java.util.List;

/**
 * @author Mikalai Sitsko , 06/25/2025
 */
public class CustomerResponse {

	private List<Customer> customers;

	public List<Customer> getPersons() {
		return customers;
	}

	public void setPersons(List<Customer> persons) {
		this.customers = persons;
	}
}
