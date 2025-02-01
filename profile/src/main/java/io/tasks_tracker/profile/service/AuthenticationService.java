package io.tasks_tracker.profile.service;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import io.tasks_tracker.profile.dto.SignInRequest;
import io.tasks_tracker.profile.dto.SignUpRequest;
import io.tasks_tracker.profile.dto.UpdatePasswordRequest;
import io.tasks_tracker.profile.entity.Role;
import io.tasks_tracker.profile.entity.User;
import io.tasks_tracker.profile.enumeration.RoleEnum;
import io.tasks_tracker.profile.exception.EmailUsedException;
import io.tasks_tracker.profile.exception.InvalidPassword;
import io.tasks_tracker.profile.exception.InvalidSignUpForm;
import io.tasks_tracker.profile.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthenticationService 
{
    private final MongoTemplate mongoTemplate;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository securityContextRepository;
    private final UserRepository userRepository;

    public AuthenticationService(
        MongoTemplate mongoTemplate,
        AuthenticationManager authenticationManager,
        PasswordEncoder passwordEncoder,
        SecurityContextRepository securityContextRepository,
        UserRepository userRepository
    ) {
        this.mongoTemplate = mongoTemplate;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.securityContextRepository = securityContextRepository;
        this.userRepository = userRepository;
    }

    public Long getUserId(Authentication authentication)
    {
        log.debug("Getting user id for authentication: {}", authentication.getName());
        return (Long) authentication.getDetails();
    }

    public void signUp(SignUpRequest form) throws InvalidSignUpForm
    {
        log.info("Starting registration for username: {}", form.getUsername());

        if(userRepository.findByUsername(form.getUsername()).isPresent()){
            log.warn("Registration failed: username already exists: {}", form.getUsername());
            throw new InvalidSignUpForm("Username already exist");
        }

        if(userRepository.findByEmail(form.getUser_details().getEmail()).isPresent()){
            log.warn("Registration failed: email already exist: {}", form.getUser_details().getEmail());
            throw new EmailUsedException();
        }

        User user = new User();
        user.setUsername(form.getUsername());
        user.setPassword(passwordEncoder.encode(form.getPassword()));

        user.setFirstname(form.getUser_details().getFirstname());
        user.setLastname(form.getUser_details().getLastname());
        user.setEmail(form.getUser_details().getEmail());
        user.setBirthDate(form.getUser_details().getBirth_date());
        
        user.addRole(new Role(RoleEnum.USER));

        User registredUser = userRepository.save(user);
        log.info("User: {} successfully registred with username: {}",
            registredUser.getId(), registredUser.getUsername());
    }

    public void signIn(
        HttpServletRequest request, 
        HttpServletResponse response,
        SignInRequest form
    ) {
        log.info("Logging attemp for username: {}", form.getUsername());
        UsernamePasswordAuthenticationToken token = UsernamePasswordAuthenticationToken.unauthenticated(
            form.getUsername(), form.getPassword()
        );

        User user = userRepository.findByUsername(form.getUsername())
            .orElseThrow(() -> new UsernameNotFoundException("User not found."));
        token.setDetails(user.getId());

        Authentication authentication = authenticationManager.authenticate(token);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        log.info("User successfully authenticated: {}", form.getUsername());
    }

    public void changePassword(
        Authentication authentication, 
        UpdatePasswordRequest form
    ) throws UsernameNotFoundException 
    {
        Long userId = getUserId(authentication);
        log.info("Password change request for user: {}", userId);
        User user = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> {
                log.error("Password change failed - user not found: {}", authentication.getName());
                return new UsernameNotFoundException("User not found");
            });
        
        if(!passwordEncoder.matches(form.getOld_password(), user.getPassword())) {
            log.warn("Invalid old password for user: {}", userId);
            throw new InvalidPassword();
        }

        user.setPassword(passwordEncoder.encode(form.getNew_password()));
        userRepository.save(user);
        log.info("Password successfully changed for user: {}", userId);
    }

    public void logoutAll(Authentication authentication, HttpServletRequest request)
    {
        Long userId = getUserId(authentication);

        log.info("Invalidating session for user: {}", userId);
        request.getSession().invalidate();

        log.info("Logging out all sessions for user: {}", userId);
        deleteAllSessions(authentication.getName());
    }

    public void deleteAllSessions(String username)
    {
        log.info("Delete all session for user with username: {}", username);

        Query query = new Query(Criteria.where("principal").is(username));
        long deletedCount = mongoTemplate.remove(query, "sessions").getDeletedCount();

        log.info("Deleted {} sessions for user with username: {}", deletedCount, username);
    }
}
