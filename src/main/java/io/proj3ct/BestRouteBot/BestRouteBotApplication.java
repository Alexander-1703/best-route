package io.proj3ct.BestRouteBot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BestRouteBotApplication {

	public static void main(String[] args) {
		try {
			SpringApplication.run(BestRouteBotApplication.class, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
