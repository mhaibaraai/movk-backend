/*
 * @Author yixuanmiao
 * @Date 2025/08/31 16:35
 */

package com.movk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class MovkBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovkBackendApplication.class, args);
    }

}
