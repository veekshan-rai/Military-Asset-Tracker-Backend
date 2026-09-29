package com.mat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * MatApplicationTests
 *
 * Basic smoke test to verify the Spring application context loads without errors.
 * This test is the minimum bar — if it fails, the application won't start at all.
 */
@SpringBootTest
@TestPropertySource(properties = {
    // Use H2 in-memory DB for tests so MySQL is not required in CI/CD
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class MatApplicationTests {

    @Test
    void contextLoads() {
        // If the application context loads without throwing an exception, this test passes.
    }
}
