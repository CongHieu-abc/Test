package com.swp.myleague.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.swp.myleague.model.entities.saleproduct.CartItem;
import com.swp.myleague.model.entities.saleproduct.Product;
import com.swp.myleague.model.entities.saleproduct.ProductSize;
import com.swp.myleague.model.service.saleproductservice.ProductService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequestMapping(value = { "/product" })
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping(value = { "", "/" })
    public String getProducts(Model model, HttpSession session) {
        List<Product> products = productService.getAll();

        HashMap<String, CartItem> cart = (HashMap<String, CartItem>) session.getAttribute("cart");
        if (cart == null) cart = new HashMap<>();
        model.addAttribute("products", products);
        model.addAttribute("cartProducts", cart);

        return "Products";
    }

    @GetMapping(value = { "/{productId}", "/{productId}/" })
    public String getDetailProduct(@PathVariable(name = "productId") String productId, Model model,
            HttpServletRequest request, HttpSession session) {
        Product product = productService.getById(productId);
        model.addAttribute("productSizes", ProductSize.values());
        model.addAttribute("product", product);

        HashMap<String, CartItem> cart = (HashMap<String, CartItem>) session.getAttribute("cart");
        if (cart == null)
            cart = new HashMap<>();

        model.addAttribute("cartProducts", cart);

        String currentUrl = request.getRequestURL().toString();
        model.addAttribute("url", currentUrl);

        Map<String, List<Product>> relatedProduct = productService.getRelatedProduct(product);
        model.addAttribute("relatedProduct", relatedProduct);

        return "ProductDetail";
    }

    @PostMapping("")
    public String saveProduct(@RequestBody Product product, Model model) {
        Product newProduct = productService.save(product);
        model.addAttribute("product", newProduct);
        return "ProductDetail";
    }

    @GetMapping(value = { "/dp" })
    public String decreaseAmountProduct(@RequestParam(name = "productId") String productId,
            Model model, HttpServletRequest request, HttpSession session) {
        HashMap<String, CartItem> cart = (HashMap<String, CartItem>) session.getAttribute("cart");
        Integer amount = 0;
        CartItem ct = cart.get(productId);
        if (ct != null) {
            amount = cart.get(productId).getProductAmount();
        } else {
            ct = new CartItem();
            ct.setProduct(productService.getById(productId));
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
        return "redirect:/product";
    }

    @GetMapping(value = { "/ip" })
    public String increaseAmountProduct(@RequestParam(name = "productId") String productId,
            Model model, HttpServletRequest request, HttpSession session) {
        HashMap<String, CartItem> cart = (HashMap<String, CartItem>) session.getAttribute("cart");
        Integer amount = 0;
        CartItem ct = cart.get(productId);
        if (ct != null) {
            amount = cart.get(productId).getProductAmount();
        } else {
            ct = new CartItem();
            ct.setProduct(productService.getById(productId));
        }
        amount = ct.getProductAmount() + 1;
        ct.setProductAmount(amount);
        cart.put(productId, ct);
        session.setAttribute("cart", cart);
        return "redirect:/product";
    }

}
