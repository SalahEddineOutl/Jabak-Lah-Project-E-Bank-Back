package com.example.jabakallah.Controllers;


import com.example.jabakallah.models.Admin;
import com.example.jabakallah.models.Agent;
import com.example.jabakallah.models.UserApp;
import com.example.jabakallah.repository.AdminRepo;
import com.example.jabakallah.repository.AgentRepo;
import com.example.jabakallah.repository.UserRepo;
import com.example.jabakallah.request.*;
import com.example.jabakallah.response.JwtAgentResponse;
import com.example.jabakallah.response.JwtResponse;
import com.example.jabakallah.security.AppAdminDetails;
import com.example.jabakallah.security.AppAgentDetails;
import com.example.jabakallah.security.AppUserDetails;
import com.example.jabakallah.security.JwtUtils;
import com.example.jabakallah.service.AdminService;
import com.example.jabakallah.service.AgentService;
import com.example.jabakallah.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.validation.Valid;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class TestController {
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    AdminRepo adminRepo;
    @Autowired
    AdminService adminService;
    @Autowired
    AgentRepo agentRepo;
    @Autowired
    AgentService agentService;
    @Autowired
    UserRepo userRepo;
    @Autowired
    PasswordEncoder encoder;

    @Autowired
    UserService userService;




    @Autowired
    JwtUtils jwtUtils;
    @GetMapping("/listClients")
    public ResponseEntity<List<UserApp>>  getAgents() {
        return ResponseEntity.ok().body(
                agentService.getAllClient());
    }
    @PostMapping("/adminSignin")
    public ResponseEntity<?> authenticateUserAdmin(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        List<String> roleAuthentification = authentication.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());


        if (roleAuthentification.get(0).contentEquals("ROLE_ADMIN")){
            AppAdminDetails userDetails = (AppAdminDetails) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            return  ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    roles.toString()));

        }

        return null;
    }

    @PostMapping("/AgentSignin")
    public JwtAgentResponse authenticateUserAgent(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        List<String> roleAuthentification = authentication.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());


        if (roleAuthentification.get(0).contentEquals("ROLE_AGENT")){

            AppAgentDetails userDetails = (AppAgentDetails) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            return new JwtAgentResponse(jwt,
                    userDetails.getId()
                    ,userDetails.getUsername(),
                    userDetails.getEmail(),
                    roles.toString()
                    ,userDetails.getNom(),
                    userDetails.getPrenom(),

                    userDetails.getAdress(),
                    userDetails.getDateNaissance(),

                    userDetails.getIdType(),
                    userDetails.getNumId(),
                    userDetails.getNumMatricule(),
                    userDetails.getNumPatente(),
                    userDetails.getPhone(),
                    userDetails.getPassword());

        }
        return null;
    }

    /**/
    @PostMapping("/clientSignin")
    public ResponseEntity<?> authenticateUserClient(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        List<String> roleAuthentification = authentication.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());


        if (roleAuthentification.get(0).contentEquals("ROLE_CLIENT")) {
            AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            return  ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    roles.toString()));
        }
        return null;
    }



    @PostMapping("/admin/signup")
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody SignupRequest signUpRequest) {
        if (adminRepo.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Username is already taken!");
        }

        if (adminRepo.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Email is already in use!");
        }

        // Create new user's account
        Admin user = new Admin(signUpRequest.getUsername(),
                encoder.encode(signUpRequest.getPassword()),
                signUpRequest.getEmail());


        adminRepo.save(user);

        return ResponseEntity.ok("User registered successfully!");
    }
    @PostMapping("/agent/add")
    public ResponseEntity<?> registerAgent(@Valid @RequestBody SignupRequestAgent signupRequestAgent) {
        if (agentRepo.existsByUsername(signupRequestAgent.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Username is already taken!");
        }

        if (agentRepo.existsByEmail(signupRequestAgent.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Email is already in use!");
        }

        // Create new user's account
        try {
            adminService.createAgent(signupRequestAgent.getNom(),signupRequestAgent.getPrenom(),signupRequestAgent.getPieceIdentite(),
                    signupRequestAgent.getNumPieceIdentite(),signupRequestAgent.getDateNaissance(),signupRequestAgent.getAdresse(),
                    signupRequestAgent.getEmail(),signupRequestAgent.getNumTel(),signupRequestAgent.getNumMatriculation(),
                    signupRequestAgent.getNumPattente());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.ok().body(agentRepo.findByEmail(signupRequestAgent.getEmail()));
    }
    @PostMapping("/client/add")
    public ResponseEntity<?> registerClient(@Valid @RequestBody SignupRequestClient signupRequestClient) {
        if (adminRepo.existsByUsername(signupRequestClient.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Username is already taken!");
        }

        if (adminRepo.existsByEmail(signupRequestClient.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Email is already in use!");
        }

        // Create new user's account

        try {
            agentService.createUser(signupRequestClient.getUsername(),signupRequestClient.getNom(),signupRequestClient.getPrenom(),
                    signupRequestClient.getNumTel(),signupRequestClient.getEmail());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping(value="/client/changePassword",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequestClient changePasswordRequestClient){

        return ResponseEntity.ok().body( userService.changePassword(changePasswordRequestClient.getNumTel(),changePasswordRequestClient.getNewPassword()));
    }

    @PostMapping("/agent/changePassword")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequestAgent changePasswordRequestAgent){
        agentService.changePassword(changePasswordRequestAgent.getUsername(),changePasswordRequestAgent.getNewPassword());

        return ResponseEntity.ok("password changed successfully for Agent");
    }

    @PostMapping("/accountExists")
    public ResponseEntity<Boolean> getClientAccountExists(@Valid @RequestBody String numTel) {
        return ResponseEntity.ok().body(userService.clienHasAccount(numTel));
    }

    @PostMapping("/updateSold")
    public ResponseEntity<Boolean> updateSoldToUser(@RequestParam("numTel") String numTel, @RequestParam("solde") Double solde) {
        return ResponseEntity.ok().body(userService.updateSolde(numTel, solde));
    }

    @PostMapping(value = "/createbankAccount",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createClientBankAccount(@Valid @RequestBody CreateCompteRequest createCompteRequest) throws IOException {

        System.out.println("inside api : "+createCompteRequest.getTypecompte() +" ----"+createCompteRequest.getNumTel());
        userService.createCompteToUser(createCompteRequest.getNumTel(),createCompteRequest.getTypecompte(), createCompteRequest.getSolde());
        return ResponseEntity.ok().body("bank account has been created");
    }

    @PostMapping("/changePassword")
    public ResponseEntity<Boolean> getClientHaschangedPassword(@Valid @RequestBody String numTel) {
        return ResponseEntity.ok().body(userService.clienHaschangedPassword(numTel));
    }
    @PostMapping("/agent/changedPassword")
    public ResponseEntity<Boolean> getAgentHasChangedPassword(@Valid @RequestBody String username) {
        return ResponseEntity.ok().body(agentService.agentHaschangedPassword(username));
    }

    @PostMapping("/getClient")
    public ResponseEntity<UserApp> getClientInfo(@Valid @RequestBody String numTel) {
        return ResponseEntity.ok().body(userService.getClientInfo(numTel));
    }
    @PostMapping("/getAgent")
    public ResponseEntity<Agent> getAgent(@Valid @RequestBody String numTel) {
        return ResponseEntity.ok().body(agentService.getAgent(numTel));
    }

}
