package com.jll.controller;

import cn.yiban.open.Authorize;
import com.jll.entity.User;
import com.jll.entity.YBUser;
import com.jll.mapper.UserMapper;
import com.jll.service.UserService;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by LES on 2018/6/13.
 */
@Controller
public class IndexController {
    private Logger logger = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    UserService userService;

    @Autowired
    AuthController authController;

    private String  APP_ID = "80c7bc59ea6f3f69";
    private String  APP_SECRET = "53b5ca41fea90828411ce5123181250d";
    private String REDIRECT_URI = "http://localhost:7070/index";

    @GetMapping("/")
    public String index(HttpServletRequest request, Model model){
        String token = (String) request.getSession().getAttribute("token");
        //如果没有token，则重定向到auth获取易班token
        if(token == null || token.isEmpty()){
            return "redirect:/auth1";
        }

        String username = (String) request.getSession().getAttribute("username");
        String usernameIsTimeOut = logoutBySystem(username, request);
        if("no".equals(usernameIsTimeOut)){
            return "redirect:/auth1";
        }

        YBUser ybUser = authController.getUserInfo(token);
        model.addAttribute("username",ybUser.getUsername());
        User user = userService.selectByYBId(ybUser.getUserId());
        System.out.println(user.getRole());
        System.out.println(user.getRole().equals("管理员"));
        if(user.getRole().equals("管理员")){
            return "teacher";
        }
        /*String username = "敬丽丽";
        request.getSession().setAttribute("username",username);
        model.addAttribute("username",username);*/
        return "student";
    }


    /**
     * 注销用户
     * @param request
     * @return 注销状态码
     * @throws IOException
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request) throws IOException {
        String token = (String) request.getSession().getAttribute("token");
        String url = authController.logoutToken(token, request);
        return "redirect:" + url;
    }

    public String logoutBySystem(String username,HttpServletRequest request){
        System.out.println("logogutbysystem 的username:" + username);
        String token = (String) request.getSession().getAttribute("token");
        System.out.println("logogutbysystem 的token:" + token);
        //判断本地session中token或username是否过期
        if(username == null || username.isEmpty() || token == null || token.isEmpty()){
            return "no";
        }
        if(!authController.isAuth(token)){
            return "no";
        }
        return "yes";
    }
}
