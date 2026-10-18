package com.webportfolio.backend;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class BackendApplicationTests {

	@Autowired
	private DataSource dataSource;

	@Test
	void connectsToPostgresql() throws SQLException {
		try (Connection connection = dataSource.getConnection();
			 var statement = connection.prepareStatement("SELECT 1");
			 var result = statement.executeQuery()) {
			assertEquals("PostgreSQL", connection.getMetaData().getDatabaseProductName());
			assertTrue(result.next());
			assertEquals(1, result.getInt(1));
		}
	}

}
