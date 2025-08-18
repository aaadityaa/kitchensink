package com.mongodb.kitchensink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@EnableMongoAuditing
@SpringBootApplication
public class KitchensinkApplication {

	public static void main(String[] args) {
		SpringApplication.run(KitchensinkApplication.class, args);
	}

}
