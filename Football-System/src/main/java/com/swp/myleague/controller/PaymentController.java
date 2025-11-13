package com.swp.myleague.controller;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.security.Principal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.swp.myleague.common.CommonFunc;
import com.swp.myleague.model.entities.User;
import com.swp.myleague.model.entities.saleproduct.CartItem;
import com.swp.myleague.model.entities.saleproduct.OrderStatus;
import com.swp.myleague.model.entities.saleproduct.Orders;
import com.swp.myleague.model.entities.saleproduct.Product;
import com.swp.myleague.model.entities.ticket.Ticket;
import com.swp.myleague.model.service.EmailService;
import com.swp.myleague.model.service.UserService;
import com.swp.myleague.model.service.saleproductservice.OrderService;
import com.swp.myleague.model.service.saleproductservice.ProductService;
import com.swp.myleague.model.service.ticketservice.TicketService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;

@Controller
@RequestMapping(value = { "/payment" })
public class PaymentController {

    @Autowired
    UserService userService;

    @Autowired
    EmailService emailService;

    @Autowired
    ProductService productService;

    @Autowired
    TicketService ticketService;

    @Autowired
    OrderService orderService;

    String listProductIds = "";

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {

        HashMap<String, CartItem> cart = (HashMap<String, CartItem>) session.getAttribute("cart");
        if (cart == null)
            cart = new HashMap<>();
        cart.values().stream().forEach(item -> {
            listProductIds += ":" + item.getProduct().getProductId();
        });
        model.addAttribute("listProductIds", listProductIds);
        model.addAttribute("cartProducts", cart);
        return "Checkout";
    }

    @Value("${payos.client-id}")
    private String clientId;
    @Value("${payos.api-key}")
    private String apiKey;
    @Value("${payos.checksum-key}")
    private String checksumKey;
    @Value("${payos.api-base}")
    private String apiBase;

    private final PayOS payOsClient;

    public PaymentController(PayOS payOsClient) {
        this.payOsClient = payOsClient;
    }

    @GetMapping("/create-payment")
    public void createPayment(
            @RequestParam("amount") Double amount,
            @RequestParam("email") String email,
            @RequestParam("orderInfo") String orderInfo,
            @RequestParam(name = "shippingAddress", required = false) String shippingAddress,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        // 1️⃣ Lấy baseUrl cho return/cancel
        String baseUrl = getBaseUrl(request);
        String returnUrl = baseUrl + "/payment/return";

        // 2️⃣ Sinh orderCode (6 chữ số cuối của millis)
        Orders order = new Orders();
        order.setOrderDateCreated(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderTotalMoney(amount);
        order.setOrderCode(System.currentTimeMillis());
        order.setOrderInfo(orderInfo);
        order = orderService.save(order);

        // 3️⃣ Tạo ItemData và PaymentData
        String itemName = "";
        if (orderInfo.split(":")[0].equals("Product")) {

            String[] parts = orderInfo.split(":");

            if (parts.length > 1) {
                List<String> productIds = Arrays.asList(parts).subList(1, parts.length);

                itemName = productIds.stream()
                        .map(id -> {
                            try {
                                return productService.getById(id).getProductName();
                            } catch (Exception e) {
                                return "Không tìm thấy sản phẩm";
                            }
                        })
                        .collect(Collectors.joining(", "));
            }

        } else if (orderInfo.split(":")[0].equals("Ticket")) {
            itemName = ticketService.getById(orderInfo.split(":")[1]).getTicketTitle();
        }

        ItemData item = ItemData.builder()
                .name(itemName)
                .quantity(1)
                .price(amount.intValue())
                .build();

        PaymentData paymentData = PaymentData.builder()
                .orderCode(order.getOrderCode())
                .amount(amount.intValue())
                .description("THANH TOÁN ĐƠN HÀNG")
                .returnUrl(returnUrl)
                .cancelUrl(returnUrl)
                .item(item)
                .build();

        // 4️⃣ Gọi PayOS client để lấy link thanh toán
        CheckoutResponseData checkout = payOsClient.createPaymentLink(paymentData);
        String checkoutUrl = checkout.getCheckoutUrl();

        // 5️⃣ Redirect user
        response.sendRedirect(checkoutUrl);
    }

    private String getBaseUrl(HttpServletRequest req) {
        String scheme = req.getScheme();
        String host = req.getServerName();
        int port = req.getServerPort();
        boolean std = (scheme.equals("http") && port == 80)
                || (scheme.equals("https") && port == 443);
        return scheme + "://" + host + (std ? "" : ":" + port);
    }

    @GetMapping("/return")
    public String payosReturn(
            @RequestParam("code") String code,
            @RequestParam("id") String paymentLinkId,
            @RequestParam("status") String status,
            @RequestParam("cancel") boolean cancel,
            @RequestParam("orderCode") Long orderCode,
            // @RequestParam("amount") BigDecimal amount,
            Model model,
            Principal principal) {
        Orders orders = orderService.getByOrderCode(orderCode);
        StringBuilder infor = new StringBuilder();

        // 1️⃣ Parse orderInfo
        String[] parts = orders.getOrderInfo().split(":");
        String type = parts[0]; // "Product" hoặc "Ticket"
        String refIdStr = parts.length > 1 ? parts[1] : "";

        // 2️⃣ Nếu thanh toán thành công
        if ("00".equals(code) && "PAID".equals(status)) {
            // 2.1 Lấy user
            User user = userService.findByUsername(principal.getName());

            // 2.2 Tạo mới Order (chú ý: không lấy từ DB mà khởi tạo mới)
            Orders order = orderService.getById(orders.getOrderId().toString());
            order.setOrderStatus(OrderStatus.COMPLETED);
            order.setUser(user);
            // lưu lần đầu để JPA sinh UUID
            order = orderService.save(order);

            // 2.3 Giảm stock tùy type
            if ("Product".equalsIgnoreCase(type)) {
                Product product = productService.getById(refIdStr);
                product.setProductAmount(product.getProductAmount() - 1);
                product = productService.save(product);
                infor.append("Product:").append(product.toString());
            } else if ("Ticket".equalsIgnoreCase(type)) {
                Ticket ticket = ticketService.getById(refIdStr);
                ticket.setTicketAmount(ticket.getTicketAmount() - 1);
                ticket = ticketService.save(ticket);
                infor.append("Ticket:").append(ticket.toString());
            }

            // 2.4 Gửi email xác nhận
            byte[] qrCodeBytes = CommonFunc.genQRCode(infor.toString());
            String base64Image = Base64.getEncoder().encodeToString(qrCodeBytes);
            String subject = "Xác nhận đơn hàng #" + order.getOrderId().toString();
            String body = String.format(
                    "Chào %s,<br><br>" +
                            "Đơn hàng của bạn đã được thanh toán thành công.<br>" +
                            "Mã đơn (UUID): %s<br>" +
                            "Tổng tiền: %s VND<br>" +
                            "Chi tiết: <a href='https://localhost:8080/orders/%s'>Xem đơn hàng</a><br><br>" +
                            "<img src='cid:qrImage' /><br><br>" +
                            "Cảm ơn bạn!",
                    user.getFullname() != null ? user.getFullname() : "khách hàng",
                    order.getOrderId(),
                    order.getOrderTotalMoney(),
                    order.getOrderId(),
                    base64Image);
            emailService.sendMail(
                    "chumlu2102@gmail.com",
                    user.getEmail(),
                    subject,
                    body,
                    qrCodeBytes);
            model.addAttribute("qrCode", base64Image);
            return "PaymentSuccess";
        }

        // 3️⃣ Nếu user hủy hoặc lỗi
        return "PaymentFailure";

    }
}
