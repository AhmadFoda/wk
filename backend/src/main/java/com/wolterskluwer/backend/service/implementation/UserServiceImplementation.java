package com.wolterskluwer.backend.service.implementation;

import com.wolterskluwer.backend.dto.UserDTO;
import com.wolterskluwer.backend.mapper.UserMapper;
import com.wolterskluwer.backend.model.User;
import com.wolterskluwer.backend.repository.UserRepository;
import com.wolterskluwer.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImplementation implements UserService {


    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDTO me(Jwt jwt) {
        return UserMapper.toDto(userRepository.findUserBySubjectId(jwt.getSubject()).orElseGet(() -> createNewUser(jwt)));
    }


    private User createNewUser(Jwt jwt) {
        final Map<String, Object> claims = jwt.getClaims();
        User user = new User(jwt.getSubject(), claims.get("name").toString(), claims.get("family_name").toString(), claims.get("email").toString(), UUID.randomUUID().toString());
        return userRepository.save(user);
    }
}
