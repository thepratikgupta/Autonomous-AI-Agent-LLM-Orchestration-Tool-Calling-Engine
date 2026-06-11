package com.prateek.ai_agent;

import com.prateek.ai_agent.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiAgentApplicationTests {

	@Autowired
	private JwtService jwtService;
	@Test
	void contextLoads() {

	}

}
