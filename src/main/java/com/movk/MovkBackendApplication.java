/*
 * @Author yixuanmiao
 * @Date 2025/09/01 14:14
 */

package com.movk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class MovkBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovkBackendApplication.class, args);
    }

}
