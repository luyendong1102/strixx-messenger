package net.ldst.chatchik;

import lombok.extern.slf4j.Slf4j;
import net.ldst.chatchik.services.EncrypMessageService;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class ChatchikApplication {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(ChatchikApplication.class, args);
	}

}
