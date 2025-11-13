package com.swp.myleague.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swp.myleague.model.entities.User;
import com.swp.myleague.model.entities.saleproduct.Orders;
import com.swp.myleague.model.service.UserService;
import com.swp.myleague.model.service.saleproductservice.OrderService;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping(value = {"/order", "/order/"})
public class OrderController {

    @Autowired
    UserService userService;
    
    @Autowired
    OrderService orderService;

    @GetMapping("")
    public String getOrderByUserId(Principal principal, Model model) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        List<Orders> orders = orderService.getByUserId(user.getUserId().toString());
        model.addAttribute("orders", orders);
        return new String();
    }
    

}
