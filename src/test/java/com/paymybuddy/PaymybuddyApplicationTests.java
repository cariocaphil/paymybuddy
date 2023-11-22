package com.paymybuddy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@SpringBootTest(classes = PaymybuddyApplicationTests.TestConfiguration.class)
class PaymybuddyApplicationTests {

	@Configuration
	@ComponentScan(basePackages = "com.paymybuddy") // Adjust the package scan path accordingly
	static class TestConfiguration {
		// Define any additional configurations if needed
	}
	@Test
	void contextLoads() {
	}

}
