package com.smartagriculture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartagriculture.dto.AdminSetupRequest;
import com.smartagriculture.dto.LoginRequest;
import com.smartagriculture.dto.RiskAnalysisRequest;
import com.smartagriculture.dto.SignupRequest;
import com.smartagriculture.entity.Assessment;
import com.smartagriculture.entity.User;
import com.smartagriculture.repository.AssessmentRepository;
import com.smartagriculture.repository.UserRepository;
import com.smartagriculture.service.AssessmentService;
import com.smartagriculture.service.AuthService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAuthenticationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanUp() {
        assessmentRepository.deleteAll();
        userRepository.deleteAll();
    }

    // 1. Successful user signup
    @Test
    void test1_SuccessfulUserSignup() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setFullName("Ramesh Kumar");
        req.setEmail("ramesh@example.com");
        req.setPhoneNumber("9876543210");
        req.setPassword("Secret123!");
        req.setConfirmPassword("Secret123!");
        req.setPreferredLanguage("te");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.fullName").value("Ramesh Kumar"))
                .andExpect(jsonPath("$.email").value("ramesh@example.com"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        assertTrue(userRepository.existsByEmailIgnoreCase("ramesh@example.com"));
    }

    // 2. Duplicate email registration
    @Test
    void test2_DuplicateEmailRegistrationRejected() throws Exception {
        SignupRequest req1 = new SignupRequest();
        req1.setFullName("Kisan User");
        req1.setEmail("kisan@example.com");
        req1.setPassword("Pass1234");
        req1.setConfirmPassword("Pass1234");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        // Attempt second registration with same email
        SignupRequest req2 = new SignupRequest();
        req2.setFullName("Kisan Duplicate");
        req2.setEmail("kisan@example.com");
        req2.setPassword("Pass5678");
        req2.setConfirmPassword("Pass5678");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Duplicate Registration"));
    }

    // 3. Password hashing
    @Test
    void test3_PasswordHashingWithBCrypt() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setFullName("Suresh Patel");
        req.setEmail("suresh@example.com");
        req.setPassword("PlaintextPassword99");
        req.setConfirmPassword("PlaintextPassword99");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        User user = userRepository.findByEmailIgnoreCase("suresh@example.com").orElseThrow();
        assertNotEquals("PlaintextPassword99", user.getPasswordHash());
        assertTrue(user.getPasswordHash().startsWith("$2a$") || user.getPasswordHash().startsWith("$2b$"));
        assertTrue(passwordEncoder.matches("PlaintextPassword99", user.getPasswordHash()));
    }

    // 4. Successful user login
    @Test
    void test4_SuccessfulUserLogin() throws Exception {
        // Create user
        User user = new User("Ramesh", "loginuser@example.com", "9876543210",
                passwordEncoder.encode("LoginPassword1"), "ROLE_USER", "en");
        userRepository.save(user);

        LoginRequest login = new LoginRequest("loginuser@example.com", "LoginPassword1");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.email").value("loginuser@example.com"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    // 5. Invalid password rejection
    @Test
    void test5_InvalidPasswordRejection() throws Exception {
        User user = new User("Ramesh", "wrongpass@example.com", "9876543210",
                passwordEncoder.encode("CorrectPassword1"), "ROLE_USER", "en");
        userRepository.save(user);

        LoginRequest login = new LoginRequest("wrongpass@example.com", "WrongPassword999");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password."));
    }

    // 6. USER cannot access admin APIs
    @Test
    void test6_UserCannotAccessAdminApis() throws Exception {
        User user = new User("Regular Farmer", "farmer@example.com", "9876543210",
                passwordEncoder.encode("Farmer123"), "ROLE_USER", "en");
        userRepository.save(user);

        LoginRequest login = new LoginRequest("farmer@example.com", "Farmer123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        // Attempt to access admin dashboard
        mockMvc.perform(get("/api/admin/dashboard").session(session))
                .andExpect(status().isForbidden());
    }

    // 7. ADMIN can access admin APIs
    @Test
    void test7_AdminCanAccessAdminApis() throws Exception {
        User admin = new User("Administrator", "admin@smartagri.org", null,
                passwordEncoder.encode("AdminStrongPass1!"), "ROLE_ADMIN", "en");
        userRepository.save(admin);

        LoginRequest login = new LoginRequest("admin@smartagri.org", "AdminStrongPass1!");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        mockMvc.perform(get("/api/admin/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").isNumber())
                .andExpect(jsonPath("$.totalAssessments").isNumber());
    }

    // 8. Public signup cannot assign ADMIN
    @Test
    void test8_PublicSignupCannotAssignAdmin() throws Exception {
        // Attempt to register as admin via public signup payload
        SignupRequest req = new SignupRequest();
        req.setFullName("Malicious Actor");
        req.setEmail("hacker@example.com");
        req.setPassword("Password123");
        req.setConfirmPassword("Password123");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ROLE_USER"));

        User saved = userRepository.findByEmailIgnoreCase("hacker@example.com").orElseThrow();
        assertEquals("ROLE_USER", saved.getRole());
    }

    // 9. Unauthenticated access to protected endpoints is rejected
    @Test
    void test9_UnauthenticatedAccessToProtectedEndpointsIsRejected() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(get("/api/assessments"))
                .andExpect(status().isUnauthorized());
    }

    // 10. Logout invalidates the authenticated session
    @Test
    void test10_LogoutInvalidatesSession() throws Exception {
        User user = new User("Logout User", "logout@example.com", null,
                passwordEncoder.encode("Pass1234"), "ROLE_USER", "en");
        userRepository.save(user);

        LoginRequest login = new LoginRequest("logout@example.com", "Pass1234");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        // Perform logout
        mockMvc.perform(post("/api/auth/logout").session(session))
                .andExpect(status().isOk());

        // Subsequent /api/auth/me should be 401
        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isUnauthorized());
    }

    // 11. One user cannot access another user's private records
    @Test
    void test11_OneUserCannotAccessAnotherUsersPrivateRecords() throws Exception {
        User userA = new User("Farmer A", "farmerA@example.com", null,
                passwordEncoder.encode("PassA1234"), "ROLE_USER", "en");
        userA = userRepository.save(userA);

        User userB = new User("Farmer B", "farmerB@example.com", null,
                passwordEncoder.encode("PassB1234"), "ROLE_USER", "en");
        userB = userRepository.save(userB);

        // Create assessment owned by User A
        Assessment assessmentA = new Assessment();
        assessmentA.setUserId(userA.getId());
        assessmentA.setFarmerName("Farmer A");
        assessmentA.setCropType("Rice");
        assessmentA.setHarvestDate(LocalDate.now().plusDays(2));
        assessmentA.setHarvestTime(LocalTime.of(10, 0));
        assessmentA.setLocationName("Guntur");
        assessmentA.setLatitude(16.3);
        assessmentA.setLongitude(80.4);
        assessmentA.setRiskLevel("LOW");
        assessmentA.setRiskScore(10);
        assessmentA = assessmentRepository.save(assessmentA);

        // User B logs in
        LoginRequest loginB = new LoginRequest("farmerB@example.com", "PassB1234");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginB)))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession sessionB = (MockHttpSession) loginResult.getRequest().getSession();

        // User B attempts to access assessment owned by User A
        mockMvc.perform(get("/api/assessments/" + assessmentA.getId()).session(sessionB))
                .andExpect(status().isForbidden());
    }

    // 12. Initial administrator provisioning cannot be reused after setup is complete
    @Test
    void test12_InitialAdminProvisioningCannotBeReused() throws Exception {
        AdminSetupRequest setup1 = new AdminSetupRequest();
        setup1.setFullName("First Admin");
        setup1.setEmail("admin1@smartagri.org");
        setup1.setPassword("SuperAdminPass123!");

        // First provisioning succeeds
        mockMvc.perform(post("/api/auth/setup-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(setup1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));

        // Second provisioning attempt must be permanently rejected
        AdminSetupRequest setup2 = new AdminSetupRequest();
        setup2.setFullName("Second Admin");
        setup2.setEmail("admin2@smartagri.org");
        setup2.setPassword("AnotherPassword123!");

        mockMvc.perform(post("/api/auth/setup-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(setup2)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Initial administrator setup is permanently locked. An administrator account already exists."));
    }
}
