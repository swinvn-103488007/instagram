package com.engineerpro.example.redis.service;

import com.engineerpro.example.redis.dto.UserPrincipal;
import com.engineerpro.example.redis.model.User;
import com.engineerpro.example.redis.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {
  private final UserRepository userRepository;
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    UserPrincipal userPrincipal = UserPrincipal.create(user);
    userPrincipal.setEnabled(user.isEnabled());
    userPrincipal.setAccountNonExpired(user.isAccountNonExpired());
    userPrincipal.setAccountNonLocked(user.isAccountNonLocked());
    userPrincipal.setCredentialsNonExpired(user.isAccountNonExpired());
    return userPrincipal;
  }
}
