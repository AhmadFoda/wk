package com.wolterskluwer.backend.service;

import com.wolterskluwer.backend.dto.UserDTO;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;


@Service
public interface UserService {

    UserDTO me(Jwt jwt);

}
