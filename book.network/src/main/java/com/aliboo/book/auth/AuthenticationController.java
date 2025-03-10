package com.aliboo.book.auth;

//import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController//bsh trdha controller
@RequestMapping("auth") //deja drna f yml api/v1 y3ni bla matktbha ay3rf l path
@RequiredArgsConstructor
//@Tag(name = "Authentication") //hdi d openai

public class AuthenticationController {

    private final AuthenticationService service; //hd class dylna dernaha
    //hna andiro les methode regester user o auth o activate user
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> register( //valid li ltht at validi lina gae obj li andirolihom required o atsift msg lfinal user
            @RequestBody @Valid RegistrationRequest request //RegistrationRequest hdi dyalna dernaha d regester
    ) throws MessagingException {
        service.register(request);

        return ResponseEntity.accepted().build();
    }

    @PostMapping("/authenticate")
    public  ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody @Valid AuthenticationRequest request
    ) {
//        System.out.println("Authentication Request received: " + request.getEmail());
//        AuthenticationResponse response = service.authenticate(request);
//        return ResponseEntity.ok(response);
        return ResponseEntity.ok(service.authenticate(request));  //hna adir l authentication l dakshi li endna f requestBody 3an tari9 AuthenticationResponse li hia class dyalna fiha vars o authenticate ra method dyalna f file authservice o AuthenticationRequest fiha var d token
    }

    @GetMapping("/activate-account")
    public void confirm(
            @RequestParam String token
    ) throws MessagingException {
        service.activateAccount(token); //acctivilina l cnt 3an token 3an tari9 method activateAccont
    }
}
