package com.iasa.projectview.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import javax.sql.DataSource

@Configuration
@Profile("test")
class TestDataSourceConfig(
    @Autowired private val env: Environment
) {

    @Bean
    @Primary
    fun dataSource(): DataSource {
        return DataSourceBuilder.create()
            .driverClassName("org.postgresql.Driver")
            .url("jdbc:postgresql://${env.getRequiredProperty("POSTGRES_HOST")}:5432/${env.getRequiredProperty("POSTGRES_DB")}")
            .username(env.getRequiredProperty("POSTGRES_USER"))
            .password(env.getRequiredProperty("POSTGRES_PASSWORD"))
            .build()
    }
}
