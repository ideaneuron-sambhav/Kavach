package com.login.Login;

import com.login.Login.entity.Permission;
import com.login.Login.entity.Role;
import com.login.Login.entity.User;
import com.login.Login.repository.PermissionRepository;
import com.login.Login.repository.RoleRepository;
import com.login.Login.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class Kavach {
	static PermissionRepository permissionRepository;
	static UserRepository userRepository;
	static User user;
	static RoleRepository roleRepository;
	static Role role;

	public static void main(String[] args) {
		SpringApplication.run(Kavach.class, args);
	}
	@Bean
	CommandLineRunner init(RoleRepository roleRepository,
						   PermissionRepository permissionRepository,
						   UserRepository userRepository,
						   BCryptPasswordEncoder passwordEncoder) {
		return args -> {
			// Initialize roles if not present
			if (roleRepository.count() == 0) {
				List<Permission> allPermissions = permissionRepository.findAll();
				List<Long> permissionIds = allPermissions.stream()
						.map(Permission::getId)
						.collect(Collectors.toList());

				// Admin role
				Role adminRole = Role.builder()
						.name("admin")
						.permissionIds(permissionIds)
						.build();
				roleRepository.save(adminRole);

				// User role
				Role userRole = Role.builder()
						.name("user")
						.permissionIds(List.of())
						.build();
				roleRepository.save(userRole);
			}

			// Initialize admin user if not present
			if (userRepository.count() == 0) {
				Role adminRole = roleRepository.findByNameIgnoreCase("admin")
						.orElseThrow(() -> new RuntimeException("Admin role not found"));

				User adminUser = User.builder()
						.firstName("admin")
						.lastName("admin")
						.email("admin@admin.com")
						.password(passwordEncoder.encode("admin@123"))
						.role(adminRole)
						.build();

				userRepository.save(adminUser);
			}
		};
	}

}
