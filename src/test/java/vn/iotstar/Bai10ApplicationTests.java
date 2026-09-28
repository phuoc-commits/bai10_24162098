package vn.iotstar;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;
import vn.iotstar.models.RegisterUserModel;
import vn.iotstar.services.AuthenticationService;
import vn.iotstar.services.JwtService;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:bai10test;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password="
})
class Bai10ApplicationTests {
	@Autowired
	private JwtService jwtService;
	@Autowired
	private AuthenticationService authenticationService;

	@Test
	void contextLoads() {
	}

	@Test
	void generatedNimbusTokenIsValidForItsUser() {
		UserDetails user = User.withUsername("student@example.com")
				.password("unused")
				.authorities("ROLE_USER")
				.build();
		String token = jwtService.generateToken(user);

		assertEquals("student@example.com", jwtService.extractUsername(token));
		assertTrue(jwtService.isTokenValid(token, user));
		assertFalse(jwtService.isTokenValid(token, User.withUsername("other@example.com")
				.password("unused")
				.authorities("ROLE_USER")
				.build()));
	}

	@Test
	void rejectsTokenWithModifiedPayload() {
		UserDetails user = User.withUsername("student@example.com")
				.password("unused")
				.authorities("ROLE_USER")
				.build();
		String token = jwtService.generateToken(user);
		int payloadStart = token.indexOf('.') + 1;
		String modifiedToken = token.substring(0, payloadStart)
				+ (token.charAt(payloadStart) == 'e' ? 'f' : 'e')
				+ token.substring(payloadStart + 1);

		assertFalse(jwtService.isTokenValid(modifiedToken, user));
	}

	@Test
	void duplicateSignupReturnsConflict() {
		RegisterUserModel registration = new RegisterUserModel(
				"duplicate@example.com",
				"password",
				"Duplicate User"
		);
		authenticationService.signup(registration);

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> authenticationService.signup(registration)
		);

		assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
	}

}
