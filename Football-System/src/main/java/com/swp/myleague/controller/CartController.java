package com.swp.myleague.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.swp.myleague.model.entities.saleproduct.CartItem;
import com.swp.myleague.model.service.saleproductservice.ProductService;

import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping(value = { "/cart" })
public class CartController {

    @Autowired
    ProductService productService;

    @GetMapping(value = { "/", "" })
    public String getCart(Model model, HttpServletRequest request, HttpSession session) {
        HashMap<String, CartItem> cart = (HashMap<String, CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
        }

        // build product id list local to this request to avoid controller-level state
        StringBuilder listProductIds = new StringBuilder();
        cart.values().stream()
            .filter(item -> item != null && item.getProduct() != null)
            .forEach(item -> {
                listProductIds.append(":").append(item.getProduct().getProductId());
            });
        model.addAttribute("listProductIds", listProductIds.toString());
        model.addAttribute("cartProducts", cart);
        return "Checkout";
    }

    @GetMapping(value = { "/dp" })
    public String decreaseAmountProduct(@RequestParam(name = "productId") String productId,
            Model model, HttpServletRequest request, HttpSession session) {
        HashMap<String, CartItem> cart = (HashMap<String, CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("cart", cart);
        }
        Integer amount = 0;
        CartItem ct = cart.get(productId);
        if (ct != null) {
            amount = cart.get(productId).getProductAmount();
        } else {
            try {
                ct = new CartItem();
                ct.setProduct(productService.getById(productId));
                // ensure newly created CartItem has a numeric amount to avoid NPE
                ct.setProductAmount(0);
            } catch (Exception e) {
                // Product not found, return redirect without error (silent fail)
                return "redirect:/cart";
            }
        }
        if (amount > 1) {
            amount = amount - 1;
            ct.setProductAmount(amount);
            cart.put(productId, ct);
            session.setAttribute("cart", cart);
        } else if (amount == 1) {
            cart.remove(productId);
            session.setAttribute("cart", cart);
        }
        return "redirect:/cart";
    }

    @GetMapping(value = { "/ip" })
    public String increaseAmountProduct(@RequestParam(name = "productId") String productId,
            Model model, HttpServletRequest request, HttpSession session) {
        HashMap<String, CartItem> cart = (HashMap<String, CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("cart", cart);
        }
        Integer amount = 0;
        CartItem ct = cart.get(productId);
        if (ct != null) {
            amount = cart.get(productId).getProductAmount();
        } else {
            try {
                ct = new CartItem();
                ct.setProduct(productService.getById(productId));
                // ensure newly created CartItem has a numeric amount to avoid NPE
                ct.setProductAmount(0);
            } catch (Exception e) {
                // Product not found, return redirect without error (silent fail)
                return "redirect:/cart";
            }
        }
        // use a safe numeric amount value
        amount = (ct.getProductAmount() == null ? 0 : ct.getProductAmount()) + 1;
        ct.setProductAmount(amount);
        cart.put(productId, ct);
        session.setAttribute("cart", cart);
        return "redirect:/cart";
    }

    @PostMapping(value = { "", "/" })
    public String addProductToCart(Model model, HttpServletRequest request, @RequestParam("productId") String productId,
            @RequestParam(name = "productAmount", required = false) String productAmount,
            @RequestParam(name = "size", required = false) String size,
            @RequestParam(name = "url", required = false) String url, HttpSession session) {
        Object o = session.getAttribute("cart");

        if (o == null) {
            session.setAttribute("cart", new HashMap<>());
        }
        Map<String, CartItem> cart = (HashMap<String, CartItem>) session.getAttribute("cart");
        Integer amount = 0;
        CartItem ct = cart.get(productId);
        if (ct != null) {
            amount = cart.get(productId).getProductAmount();
        } else {
            try {
                ct = new CartItem();
                ct.setProduct(productService.getById(productId));
            } catch (Exception e) {
                // Product not found, redirect back to product page
                return "redirect:product";
            }
        }
        // validate numeric input for product amount; default to 1 on parse error or missing
        int parsed = 1;
        try {
            if (productAmount != null) {
                parsed = Integer.parseInt(productAmount);
                if (parsed < 1) parsed = 1;
            }
        } catch (NumberFormatException ex) {
            parsed = 1;
        }
        amount = parsed + amount;
        ct.setProductAmount(amount);
        cart.put(productId, ct);
        session.setAttribute("cart", cart);
        return "redirect:product";
    }

}
