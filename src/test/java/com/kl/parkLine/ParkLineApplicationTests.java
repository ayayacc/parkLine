package com.kl.parkLine;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootTest
@EnableJpaAuditing(auditorAwareRef = "mockAuditorAware")
class ParkLineApplicationTests {

	@Test
	void contextLoads() {
	}

}
