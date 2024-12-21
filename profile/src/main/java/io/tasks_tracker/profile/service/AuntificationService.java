package io.tasks_tracker.profile.service;

import org.springframework.beans.factory.annotation.Autowired;
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
import io.tasks_tracker.profile.exception.InvalidSignInForm;
import io.tasks_tracker.profile.exception.InvalidSignUpForm;
import io.tasks_tracker.profile.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuntificationService 
{
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SecurityContextRepository securityContextRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    public void signUp(SignUpRequest form) throws InvalidSignUpForm
    {
        if(userRepository.findByUsername(form.getUsername()).isPresent()){
            throw new InvalidSignUpForm("Username is already used");
        }


        if(userRepository.findByEmail(form.getEmail()).isPresent()){
            throw new InvalidSignUpForm("Email is already registered");
        }

        User user = new User();
        user.setUsername(form.getUsername());
        user.setPassword(passwordEncoder.encode(form.getPassword()));

        user.setFirstname(form.getFirstname());
        user.setLastname(form.getLastname());
        user.setEmail(form.getEmail());
        user.setBirthDate(form.getBirth_date());
        
        user.addRole(new Role(RoleEnum.USER));

        userRepository.save(user);
    }

    public void signIn(
            HttpServletRequest request, 
            HttpServletResponse response,
            SignInRequest form
    ) throws InvalidSignInForm
    {
        Authentication authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated(
                form.getUsername(), form.getPassword()
            )
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }

    public void changePassword(
            Authentication authentication, 
            UpdatePasswordRequest form
    ) throws UsernameNotFoundException
    {
        User user = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        if(!passwordEncoder.matches(form.getOld_password(), user.getPassword())) {
            throw new RuntimeException("Invalid password.");
        }

        user.setPassword(passwordEncoder.encode(form.getNew_password()));
        userRepository.save(user);
    }

    public void logoutAll(Authentication authentication, HttpServletRequest request)
    {
        request.getSession().invalidate();
        Query query = new Query(Criteria.where("principal").is(authentication.getName()));
        mongoTemplate.remove(query, "sessions");
    }
}
