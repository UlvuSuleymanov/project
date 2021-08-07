package az.edadi.back.controller;

import az.edadi.back.model.request.PasswordRecoverRequest;
import az.edadi.back.model.request.SignInRequestModel;
import az.edadi.back.model.request.SignUpRequestModel;
import az.edadi.back.model.response.SignInResponseModel;
import az.edadi.back.model.response.SignUpResponseModel;
import az.edadi.back.service.AuthenticationService;
import az.edadi.back.service.UserService;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.io.IOException;


@RestController
@RequestMapping(value = "api/auth")
 public class AuthController {


    private final UserService userService;
    private  final AuthenticationService authenticationService;

    @Autowired
    public AuthController( AuthenticationService authenticationService,UserService userService) {
        this.userService = userService;
        this.authenticationService = authenticationService;
    }



    @PostMapping(value = "/signin")
    public ResponseEntity<?> login (@RequestBody SignInRequestModel signInRequestModel){
        SignInResponseModel signUpResponseModel=   authenticationService.login(signInRequestModel);
        return  ResponseEntity.ok(signUpResponseModel);
     }


    @PostMapping(value = "/signup")
    public ResponseEntity addUser( @Validated @RequestBody final SignUpRequestModel signUpRequestModel){

      SignUpResponseModel signUpResponseModel = new SignUpResponseModel();
      authenticationService.register(signUpRequestModel);
      return  new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping(value = "/recover")
    public ResponseEntity sendToken(@RequestBody PasswordRecoverRequest emailOrUsername) throws MessagingException, IOException, TemplateException {
    return ResponseEntity.ok(authenticationService.sendTokenByEmail(emailOrUsername.getUsernameOrEmail()));
    }

    @PutMapping(value = "/recover")
    public ResponseEntity recoverPassword(@RequestParam String token){

        SignUpResponseModel signUpResponseModel = new SignUpResponseModel();
        return  new ResponseEntity(HttpStatus.OK);
    }



}
