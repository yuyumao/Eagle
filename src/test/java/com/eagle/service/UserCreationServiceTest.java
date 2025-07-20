package com.eagle.service;

import com.eagle.entity.User;
import com.eagle.entity.User.Address;
import com.eagle.dtos.CreateUserRequest;

import com.eagle.dtos.UserAddressDTO;
import com.eagle.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCreationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserCreationService userCreationService;

    private CreateUserRequest createSampleRequest() {
        UserAddressDTO address = new UserAddressDTO(
                "123 Main St",
                "Apt 4B",
                "Building C",
                "Springfield",
                "State County",
                "12345"
        );

        return new CreateUserRequest(
                "John Doe",
                address,
                "+1234567890",
                "john.doe@example.com",
                "Password123!"
        );
    }

    @Test
    void createUser_Success() {
        // Setup
        CreateUserRequest request = createSampleRequest();
        String hashedPassword = "hashed_Password123!";

        when(passwordEncoder.encode(request.getPassword())).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute
        User result = userCreationService.create(request);

        // Verify
        assertNotNull(result);
        assertEquals("John Doe", result.getUserName());
        assertEquals("+1234567890", result.getPhoneNumber());
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals(hashedPassword, result.getPasswordHash());

        // Verify address
        Address address = result.getAddress();
        assertNotNull(address);
        assertEquals("123 Main St", address.getLine1());
        assertEquals("Apt 4B", address.getLine2());
        assertEquals("Building C", address.getLine3());
        assertEquals("Springfield", address.getTown());
        assertEquals("State County", address.getCounty());
        assertEquals("12345", address.getPostcode());

        // Verify password encoding
        verify(passwordEncoder).encode("Password123!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_WithNullOptionalFields() {
        // Setup
        UserAddressDTO address = new UserAddressDTO(
                "456 Oak St",
                null,  // Optional field
                null,  // Optional field
                "Riverside",
                "Wood County",
                "67890"
        );

        CreateUserRequest request = new CreateUserRequest(
                "Jane Smith",
                address,
                "+0987654321",
                "jane.smith@example.com",
                "SecurePass456!"
        );

        String hashedPassword = "hashed_SecurePass456!";

        when(passwordEncoder.encode(request.getPassword())).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute
        User result = userCreationService.create(request);

        // Verify
        Address resultAddress = result.getAddress();
        assertNull(resultAddress.getLine2());
        assertNull(resultAddress.getLine3());

        assertEquals("456 Oak St", resultAddress.getLine1());
        assertEquals("Riverside", resultAddress.getTown());
        assertEquals("Wood County", resultAddress.getCounty());
        assertEquals("67890", resultAddress.getPostcode());
    }

    @Test
    void createUser_PasswordEncoding() {
        // Setup
        CreateUserRequest request = createSampleRequest();
        String hashedPassword = "$2a$10$5kz8hGXb2/3JdKf7gYhTZe";

        when(passwordEncoder.encode("Password123!")).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute
        User result = userCreationService.create(request);

        // Verify
        assertEquals(hashedPassword, result.getPasswordHash());
        verify(passwordEncoder).encode("Password123!");
    }

    @Test
    void createUser_VerifyRepositoryCall() {
        // Setup
        CreateUserRequest request = createSampleRequest();
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute
        userCreationService.create(request);

        // Verify repository call
        verify(userRepository).save(any(User.class));

        User savedUser = userCaptor.getValue();
        assertEquals("John Doe", savedUser.getUserName());
        assertEquals("john.doe@example.com", savedUser.getEmail());
        assertEquals("hashed_password", savedUser.getPasswordHash());
    }

    @Test
    void createUser_AllFieldsCaptured() {
        // Setup
        CreateUserRequest request = createSampleRequest();
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(passwordEncoder.encode(anyString())).thenReturn("hashed_pass");
        when(userRepository.save(userCaptor.capture())).thenReturn(new User());

        // Execute
        userCreationService.create(request);

        // Verify all fields are properly set
        User capturedUser = userCaptor.getValue();

        assertEquals(request.getName(), capturedUser.getUserName());
        assertEquals(request.getPhoneNumber(), capturedUser.getPhoneNumber());
        assertEquals(request.getEmail(), capturedUser.getEmail());
        assertEquals("hashed_pass", capturedUser.getPasswordHash());

        // Verify address mapping
        UserAddressDTO reqAddress = request.getAddress();
        Address savedAddress = capturedUser.getAddress();

        assertEquals(reqAddress.getLine1(), savedAddress.getLine1());
        assertEquals(reqAddress.getLine2(), savedAddress.getLine2());
        assertEquals(reqAddress.getLine3(), savedAddress.getLine3());
        assertEquals(reqAddress.getTown(), savedAddress.getTown());
        assertEquals(reqAddress.getCounty(), savedAddress.getCounty());
        assertEquals(reqAddress.getPostcode(), savedAddress.getPostcode());
    }
}