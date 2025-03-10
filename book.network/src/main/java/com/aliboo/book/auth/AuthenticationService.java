package com.aliboo.book.auth;

import com.aliboo.book.email.EmailService;
import com.aliboo.book.email.EmailTemplateName;
import com.aliboo.book.role.RoleRepository;
import com.aliboo.book.security.JwtService;
import com.aliboo.book.user.Token;
import com.aliboo.book.user.TokenRepository;
import com.aliboo.book.user.User;
import com.aliboo.book.user.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager; //hdi interface d spring o anzido fiha obj dyalna fl config
    private final JwtService jwtService;

    @Value("${application.mailing.frontend.activation-url}") //drnaha f dev.yml
    private String activationUrl;

    public void register(RegistrationRequest request) throws MessagingException {//hdi regter method drnaha f authController
        var userRole = roleRepository.findByName("USER") //hna default role dyalna howa User
                //hna bsh t handy l exaption mzn
                //todo - better exception handling
                .orElseThrow(()-> new IllegalStateException("ROLE USER was not initialized "));
        var user = User.builder() //hna la kn created lina l user obj
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) //hna paas aykon encoder fsh aymshi database
                .accountLocked(false)
                .enabled(false) //hna bsh l user ydir lih enable using verifecation code li ansiftolih li drnah ltht b sendValidationEmail
                .roles(List.of(userRole))//dkshi elsh drna fetsh rolo lfo9
                .build();
        userRepository.save(user); //save lina user f database
        sendValidationEmail(user);
    }

    private void sendValidationEmail(User user) throws MessagingException { //hdi atgeneri lina new token li drnah l tet bsh ytsift l user validation email o y validiraso
        var newToken = generateAndSaveActivationToken(user);
        //send email l user akhir haja
         emailService.sendEmail(
                 user.getEmail(),
                 user.fullName(),
                 EmailTemplateName.ACTIVATE_ACCOUNT,
                 activationUrl,
                 newToken,
                 "Accont activation"

         );
    }

    private String generateAndSaveActivationToken(User user) {

        String generatedToken = generateActivationCode(6); //generateActivationCode hia li fiha l code d verification dl mail dernaha l teht
        var token = Token.builder()
                .token(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(15))//ytexpira 15 min
                .user(user)
                .build();
        tokenRepository.save(token);

        return generatedToken;
    }

    //hna at cree code li nsiftoh l user randamly
    private String generateActivationCode(int length) { //hna an generiw Token mkwn mn shhal mabghina mn degit
        String characters = "0123456789"; //hdo homa charcters li aytkwn mno token
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();//hna bsh ykon secured ta dok randemly degites
        for (int i= 0; i <length; i++) {
            int randomIndex = secureRandom.nextInt(characters.length()); //hna y3ni random index aykon mn 0 tal 9 hit characters var drna fiha 9
            codeBuilder.append(characters.charAt(randomIndex));
        }
        return codeBuilder.toString(); //hna atgenerina activationcode mkwn mn 6 deget hit drna generateActivationCode(6) bach n validiw email dl user
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) { //hdi hia l methed li an3tiw fiha l authentication l user
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        ) ; //hd method hia li adir l authentication atshof la can email o pass shah dir l auth lamknsh tl3  err
        //mn mor mykon klshi mzn o ndiro l auth
        var claims = new HashMap<String, Object>();
        var user = ((User)auth.getPrincipal());//fetchi wl jib lina user  a mn authenticate 3an tari9 Principal bla mnjiboh mn database o Principal interface drnaha f User class
        claims.put("fullName", user.fullName());
        var jwtToken = jwtService.generateToken(claims ,user) ; //hna ead bch khdmna b jwt o andirobih l authentication

        return AuthenticationResponse.builder()
                .token(jwtToken)//l var d token f AuthenticationResponse class dir fiha jwt dyalna li drna fih user o claims li 3an tari9o aydir l authentication
                .build(); // hna ankono salina l authenticate li ankhdmo biha f authController
    }

    //hdi li tht hia methode li atvalidi biha l account
    public void activateAccount(String token) throws MessagingException {
        Token savedToken = tokenRepository.findByToken(token)  //hna jib lia token mn database
                //todo exception has to be defined
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        if(LocalDateTime.now().isAfter(savedToken.getExpiresAt())){//hna la t expira token
            //andiro send validation email again ondiro exception
            sendValidationEmail(savedToken.getUser());
            throw new RuntimeException("Activation token expired .A new token has been send to the same email address");
        }
        //hna la matexpirash dir lia
        var user = userRepository.findById(savedToken.getUser().getId())//jib lina user mn database 3an tari9 token
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);
        savedToken.setValidateAt(LocalDateTime.now());
        tokenRepository.save(savedToken);
    }
}
