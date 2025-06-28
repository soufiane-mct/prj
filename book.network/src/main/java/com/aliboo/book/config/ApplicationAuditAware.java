package com.aliboo.book.config;

//hdi andiroha bch nearfo who did what (3antari9 user id)

import com.aliboo.book.user.User;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.lang.NonNull;

import java.util.Optional;
//hd class 3eytna liha fl beansConfig bch tkhdm
public class ApplicationAuditAware implements AuditorAware<Integer> { //bch ngolo l spring use this to fetch auditer l hali bch nhwlo l class dyalna l AuditAware o dik Integer hit user id int
    @Override
    @NonNull
    public Optional<Integer> getCurrentAuditor() {//hna ki anjibo l current auditor
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // hna fsh n implement security(jwt)ndiro update l SecurityContextHolder
        if (authentication == null ||
            !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {//lakan user not authenticated ola kn double tcheck ola kn t2kd wsh mshi mn shi toked mshi dyalna
            return Optional.empty();
        }//ms la kn l 3eks o user Authenticated dirlia
        User userPrincipal = (User) authentication.getPrincipal();
        return Optional.ofNullable(userPrincipal != null ? userPrincipal.getId() : null);//la maknsh userPrincipal nullbel jb lia user id lihowa authenticated
    }

}
