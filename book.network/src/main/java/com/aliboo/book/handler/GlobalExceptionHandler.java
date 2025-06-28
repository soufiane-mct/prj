package com.aliboo.book.handler;

import com.aliboo.book.exception.OperationNotPermitedException;
import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashSet;
import java.util.Set;

import static com.aliboo.book.handler.BusinessErrorCode.*;
import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice

public class GlobalExceptionHandler { //hna dl exeption bch ndiro fiha shihaja matalan maknsh user dir lia hd exeption ola matalan mshi auth dir lia hd l exeption...

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ExceptionResponse> handleException (LockedException exp){ //handleException at catshi lina exeption libghina ila bzff dl exeption o db bdina b LockedException
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(ACCOUNT_LOCKED.getCode()) //ACCOUNT_LOCKED hia li 3mrnaha f BusinessErrorCode
                                .businessErrorDescription(ACCOUNT_LOCKED.getDescription())//hna jib lia description mn ACCOUNT_LOCKED li deja drna f BusinessErrorCode
                                .error(exp.getMessage())//hna jib lia msg mn exp li hia var li fiha LockedException lijbnaha mn ExceptionHandler annotation
                                .build()
                );
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ExceptionResponse> handleException (DisabledException exp){ //handleException at catshi lina exeption libghina ila bzff dl exeption o db bdina b LockedException
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(ACCOUNT_DISABLED.getCode()) //ACCOUNT_DISABLED hia li 3mrnaha f BusinessErrorCode
                                .businessErrorDescription(ACCOUNT_DISABLED.getDescription())//hna jib lia description mn ACCOUNT_DISABLED li deja drna f BusinessErrorCode
                                .error(exp.getMessage())//hna jib lia msg mn exp li hia var li fiha DisabledException lijbnaha mn ExceptionHandler annotation
                                .build()
                );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleException (BadCredentialsException exp){ //handleException at catshi lina exeption libghina ila bzff dl exeption o db bdina b LockedException
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(BAD_CREDENTIALS.getCode()) //ACCOUNT_DISABLED hia li 3mrnaha f BusinessErrorCode
                                .businessErrorDescription(BAD_CREDENTIALS.getDescription())//hna jib lia description mn ACCOUNT_DISABLED li deja drna f BusinessErrorCode
                                .error(BAD_CREDENTIALS.getDescription())//hna jib lia msg mn description dl BAD_CREDENTIALS
                                .build()
                );
    }

    @ExceptionHandler(MessagingException.class) //la mabghash ytsift lina email matalan l shi sabab mn bzff dir hdi
    public ResponseEntity<ExceptionResponse> handleException (MessagingException exp){ //handleException at catshi lina exeption libghina ila bzff dl exeption o db bdina b LockedException
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)//hit kyn err wst l app dylna
                .body(
                        ExceptionResponse.builder()
                                .error(exp.getMessage())//hna jib lia msg mn MessagingExc:eption
                                .build()
                );
    }

    //hdi ankhdmo biha f sharbel exeption l mknsh l user id connected tisawi l book owner id ra f updateShareableStatus f bookservice
    @ExceptionHandler(OperationNotPermitedException.class) //la mabghash ytsift lina email matalan l shi sabab mn bzff dir hdi
    public ResponseEntity<ExceptionResponse> handleException (OperationNotPermitedException exp){ //handleException at catshi lina exeption libghina ila bzff dl exeption o db bdina b LockedException
        return ResponseEntity
                .status(BAD_REQUEST)//hit err frequest
                .body(
                        ExceptionResponse.builder()
                                .error(exp.getMessage())// dir msg f OperationNotPermitedException (o dk l msg howa li ktbna hna f bookservice)
                                .build()
                );
    }


    @ExceptionHandler(MethodArgumentNotValidException.class) // hd err fhalat t9iyd user o masiftsh valid data bhal name email...
    public ResponseEntity<ExceptionResponse> handleException (MethodArgumentNotValidException exp){ //handleException at catshi lina exeption libghina ila bzff dl exeption o db bdina b LockedException
        Set<String> errors = new HashSet<>();//hna drna set bch lakan nfs msg err kit3awd ytl3o lia a mera whda

        exp.getBindingResult().getAllErrors()
                .forEach(error -> {
                    var errorMessage = error.getDefaultMessage();
                    errors.add(errorMessage);
                }); //hdshi gol d spring bch njbdo getDefaultMessage err li ytl3o lina fsh user masiftsh valid data bhal name email

        return ResponseEntity
                .status(BAD_REQUEST)//hit kyn err wst l app dylna
                .body(
                        ExceptionResponse.builder()
                                .validationErrors(errors) //errors l var dyalna li drna fo9
                                .build()
                );
    }


    @ExceptionHandler(Exception.class) //hna la kn any exaption f my app dir hdshi
    public ResponseEntity<ExceptionResponse> handleException (Exception exp){ //handleException at catshi lina exeption libghina ila bzff dl exeption o db bdina b LockedException
        //hadi f loging dir lia exp f inseption
        exp.printStackTrace();
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorDescription("Internal error, contact the admin")
                                .error(exp.getMessage())
                                .build()
                );
    }


}
