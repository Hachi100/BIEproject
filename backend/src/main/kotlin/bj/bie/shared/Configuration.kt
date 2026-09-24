package bj.bie.shared

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock
import java.time.ZoneId

@Configuration
class SharedConfiguration {
    /** Horloge injectée : les règles datées (âge d'un prix, échéances) restent testables. */
    @Bean
    fun clock(): Clock = Clock.system(ZoneId.of("Africa/Porto-Novo"))
}
