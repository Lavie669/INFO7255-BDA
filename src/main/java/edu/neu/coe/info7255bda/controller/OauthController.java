package edu.neu.coe.info7255bda.controller;

import edu.neu.coe.info7255bda.model.VO.ResultData;
import edu.neu.coe.info7255bda.utils.jwt.JWTUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/oauth2")
public class OauthController {

    @Autowired
    private JWTUtil jwtUtil;

    @SneakyThrows
    @PostMapping("/token")
    public Object getOauthToken(@RequestBody String clientId){
        return ResultData.success(jwtUtil.creatTokenByRS256(clientId));
    }
}
