package com.rmtech.ecom;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class EcomApplicationTests {

	@Test
	void applicationCanBeInstantiated() {
		assertDoesNotThrow(EcomApplication::new);
	}

}
