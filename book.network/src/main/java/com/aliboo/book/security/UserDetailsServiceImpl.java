package com.aliboo.book.security;
//hna userdetails d spring a nakhdoh o ndiro fih implimation dyalna

import com.aliboo.book.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService { //hna anjibo userdyalna mn database

    private final UserRepository repository;

    @Override
    @Transactional //hna bsh dir load l user ydir load ta roles wl auth meah (y3ni fsh njib user aydir load o yshof ta auth dylo )
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        return repository.findByEmail(userEmail) //hna method li drna f userrepo (ayjib lina user mn database 3an tari9 l mail dyalo lihowa unique)
                .orElseThrow(()-> new UsernameNotFoundException("User not found")); //wl dir hd err lmknsh
    }
}
