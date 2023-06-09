package com.example.jabakallah.Controllers;

import com.example.jabakallah.models.Admin;
import com.example.jabakallah.models.Agent;
import com.example.jabakallah.models.UserApp;
import com.example.jabakallah.request.AddFileRequest;
import com.example.jabakallah.service.AdminService;
import com.example.jabakallah.service.AgentFileService;
import com.example.jabakallah.service.AgentService;
import com.example.jabakallah.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@CrossOrigin
@RestController
@RequestMapping("/adminController")
@RequiredArgsConstructor

public class AdminController {
    private final UserService userService;
    private final AdminService adminService;
    private final AgentService agentService;

    @Autowired
    private  AgentFileService agentFileService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public Admin check(@RequestBody String uid) throws IOException {

        return adminService.checkAdminExists(uid);

    }

    @GetMapping("/admin")
    public ResponseEntity<List<Admin>> getAdmins() {
        return ResponseEntity.ok().body(adminService.getAdmins());
    }


    @GetMapping("/listAgents")
    public ResponseEntity<List<Agent>>  getAgents() {
        return ResponseEntity.ok().body(
                adminService.getAgents());
    }

    @GetMapping("/listClients")
    public ResponseEntity<List<UserApp>> getClients() {
        return ResponseEntity.ok().body(agentService.getUsers());
    }




}

