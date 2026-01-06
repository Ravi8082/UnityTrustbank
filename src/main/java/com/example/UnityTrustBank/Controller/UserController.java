package com.example.UnityTrustBank.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.UnityTrustBank.Service.UserService;
import com.example.UnityTrustBank.dto.UserDto;

import jakarta.websocket.server.PathParam;

@RestController
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserService userServ;
	
	@PostMapping("/customer")
	public ResponseEntity<UserDto> customer(@RequestBody UserDto userDto){
		UserDto user = userServ.createCustomer(userDto);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
	}
	
	@PostMapping("/manager")
	public ResponseEntity<UserDto> manager(@RequestBody UserDto userDto){
		UserDto user = userServ.createManager(userDto);
		return new ResponseEntity<>(user,HttpStatus.CREATED);
	}
	
	@GetMapping("/{id}/customer")
	public ResponseEntity<UserDto> getCutomerWithId(@PathVariable Long id){
		 UserDto user = userServ.getUserById(id);
	        return ResponseEntity.ok(user);
		
	}
	
	@GetMapping("/all/customer")
	public ResponseEntity<List<UserDto>> getAllCustomer(){
		return ResponseEntity.ok(userServ.getAllCustomer());
	}
	
	
	@GetMapping("/all/manager")
	public ResponseEntity<List<UserDto>> getAllManager(){
		return ResponseEntity.ok(userServ.getAllManagers());
	}
	
	

}
