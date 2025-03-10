package com.aliboo.book;

import com.aliboo.book.role.Role;
import com.aliboo.book.role.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware") //hdi drr dirha bsh tkhdm b @EntityListeners(AuditingEntityListener.class) o bch tkhdm b @CreatedDate...
//auditorAware hada bean dyalna drnaf fl config drna fih ApplicationAuditAware li drrna fiha l auth l user bch nearfo who did what (3antari9 user) o dernaha hna bch y3rf l Auditing li ankhdmo biha hit f lwl kna dayrin a EnableJpaAuditing
@EnableAsync //khdmna biha fl mail

public class Application {

	public static void main(String[] args) {SpringApplication.run(Application.class, args);}

	@Bean
	public CommandLineRunner runner(RoleRepository roleRepository) { //hna bch t runi lina USER role
		return args -> {
			if (roleRepository.findByName("USER").isEmpty()){ //hna ktgolih la makansh user role
				roleRepository.save(//creayih lina
						Role.builder().name("USER").build()
				);
			}
		};

	}

}
