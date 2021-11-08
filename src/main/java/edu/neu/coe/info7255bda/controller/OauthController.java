package edu.neu.coe.info7255bda.controller;

import edu.neu.coe.info7255bda.model.VO.ResultData;
import edu.neu.coe.info7255bda.service.JwtTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/oauth2")
public class OauthController {

    @Autowired
    private JwtTokenService tokenService;

    @GetMapping("/token")
    public Object getOauthToken(){
        return ResultData.success(tokenService.creatToken());
    }

    @PostMapping("/validate")
    public Object validateOauthToken(@RequestBody String token, HttpServletResponse response){
        if (tokenService.validateMyToken(token)!=null){
            return ResultData.success("Authorization success!!!");
        }
        response.setStatus(401);
        return ResultData.fail(401, "Invalid token!!!");
    }
}
