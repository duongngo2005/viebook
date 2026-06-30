package com.ndd.viebook;

import com.ndd.viebook.common.utils.SlugUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ViebookBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ViebookBackendApplication.class, args);
	}

}
