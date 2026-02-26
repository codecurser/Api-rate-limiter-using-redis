package com.example.ratelimiter.controller;

import com.example.ratelimiter.dto.UserDto;
import com.example.ratelimiter.exception.GlobalExceptionHandler;
import com.example.ratelimiter.interceptor.RateLimitInterceptor;
import com.example.ratelimiter.model.User;
import com.example.ratelimiter.repository.UserRepository;
import com.example.ratelimiter.service.RateLimiterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RateLimiterService rateLimiterService;

    @InjectMocks
    private UserController userController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Mock interceptor so it passes
        RateLimitInterceptor rateLimitInterceptor = new RateLimitInterceptor(rateLimiterService);
        when(rateLimiterService.isAllowed(anyString())).thenReturn(true);

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .addInterceptors(rateLimitInterceptor)
                .build();
    }

    @Test
    void shouldCreateUser() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setName("John Doe");
        userDto.setEmail("john@example.com");

        User savedUser = new User("1", "John Doe", "john@example.com");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.name").value("John Doe"));
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        User user1 = new User("1", "John", "john@example.com");
        User user2 = new User("2", "Jane", "jane@example.com");
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void shouldRateLimitRequest() throws Exception {
        when(rateLimiterService.isAllowed(anyString())).thenReturn(false);

        mockMvc.perform(get("/users"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Rate limit exceeded. Try again later."));
    }
}
