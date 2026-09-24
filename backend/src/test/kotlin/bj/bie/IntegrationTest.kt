package bj.bie

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.boot.test.context.TestConfiguration
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

/** Base des tests d'intégration : application complète sur un vrai PostgreSQL 16. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(IntegrationTest.Containers::class)
abstract class IntegrationTest {

    @TestConfiguration(proxyBeanMethods = false)
    class Containers {
        @Bean
        @ServiceConnection
        fun postgres(): PostgreSQLContainer = SHARED_POSTGRES
    }

    companion object {
        /** Conteneur partagé entre classes de test : les migrations (10 093 prix) ne tournent qu'une fois. */
        val SHARED_POSTGRES: PostgreSQLContainer = PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine")).apply { start() }
    }
}
