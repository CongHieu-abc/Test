package com.swp.myleague.controller;

import static org.junit.jupiter.api.Assertions.*;
// avoid Mockito inline mocking (Byte Buddy issues on latest JDK) by using a small test fake
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.swp.myleague.model.entities.saleproduct.CartItem;
import com.swp.myleague.model.entities.saleproduct.Product;
import com.swp.myleague.model.service.saleproductservice.ProductService;


public class CartControllerTest {

    private MockMvc mockMvc;
    private ProductService productService;
    private CartController controller;
    private MockHttpSession session;

    @BeforeEach
    void setup() {
    productService = new FakeProductService();
        controller = new CartController();
        // inject mock into controller
        ReflectionTestUtils.setField(controller, "productService", productService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        session = new MockHttpSession();
    }

    // Simple fake to avoid Mockito inline instrumentation issues on newer JDKs.
    static class FakeProductService extends ProductService {
        private Product last;

        void setLast(Product p) {
            this.last = p;
        }

        @Override
        public Product getById(String id) {
            return last;
        }
    }

    @Test
    void addProductToCart_createsCartInSession() throws Exception {
        UUID id = UUID.randomUUID();
        Product p = new Product(id, "Test Product", "desc", null, 100.0, 10, "/img.png", null);
    ((FakeProductService)productService).setLast(p);

    var mvcResult = mockMvc.perform(post("/cart").param("productId", id.toString()).param("productAmount", "1")
        .session(session)).andExpect(status().is3xxRedirection()).andReturn();

    @SuppressWarnings("unchecked")
    Map<String, CartItem> cart = (Map<String, CartItem>) mvcResult.getRequest().getSession().getAttribute("cart");
    assertNotNull(cart, "Cart should be present in session");
    assertTrue(cart.containsKey(id.toString()));
    assertEquals(1, cart.get(id.toString()).getProductAmount());
    }

    @Test
    void viewCart_showsCartAndListProductIds() throws Exception {
        UUID id = UUID.randomUUID();
        Product p = new Product(id, "View Product", "desc", null, 20.0, 10, "/img.png", null);
        ((FakeProductService)productService).setLast(p);

        CartItem ct = new CartItem();
        ct.setProduct(p);
        ct.setProductAmount(2);
        Map<String, CartItem> cart = new HashMap<>();
        cart.put(id.toString(), ct);
        session.setAttribute("cart", cart);

        var mvcResult = mockMvc.perform(get("/cart").session(session))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("cartProducts", "listProductIds"))
            .andReturn();

        Object listObj = mvcResult.getModelAndView().getModel().get("listProductIds");
        assertNotNull(listObj);
        assertTrue(listObj.toString().contains(id.toString()));
    }

    @Test
    void addProduct_invalidAmount_defaultsTo1() throws Exception {
        UUID id1 = UUID.randomUUID();
        Product p1 = new Product(id1, "Missing Amount", "desc", null, 5.0, 10, "/img5.png", null);
        ((FakeProductService)productService).setLast(p1);

        // Test missing amount
        var mvcResult1 = mockMvc.perform(post("/cart").param("productId", id1.toString()).session(session))
            .andExpect(status().is3xxRedirection()).andReturn();

        @SuppressWarnings("unchecked")
        Map<String, CartItem> cart1 = (Map<String, CartItem>) mvcResult1.getRequest().getSession().getAttribute("cart");
        assertNotNull(cart1);
        assertEquals(1, cart1.get(id1.toString()).getProductAmount());

        // Test negative amount
        UUID id2 = UUID.randomUUID();
        Product p2 = new Product(id2, "Neg Amount", "desc", null, 8.0, 10, "/img6.png", null);
        ((FakeProductService)productService).setLast(p2);

        var mvcResult2 = mockMvc.perform(post("/cart").param("productId", id2.toString()).param("productAmount", "-5").session(session))
            .andExpect(status().is3xxRedirection()).andReturn();

        @SuppressWarnings("unchecked")
        Map<String, CartItem> cart2 = (Map<String, CartItem>) mvcResult2.getRequest().getSession().getAttribute("cart");
        assertNotNull(cart2);
        assertEquals(1, cart2.get(id2.toString()).getProductAmount());

        // Test zero amount
        UUID id3 = UUID.randomUUID();
        Product p3 = new Product(id3, "Zero Amount", "desc", null, 3.0, 10, "/img.png", null);
        ((FakeProductService)productService).setLast(p3);

        var mvcResult3 = mockMvc.perform(post("/cart")
                .param("productId", id3.toString())
                .param("productAmount", "0")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andReturn();

        @SuppressWarnings("unchecked")
        Map<String, CartItem> cart3 = (Map<String, CartItem>) mvcResult3.getRequest().getSession().getAttribute("cart");
        assertNotNull(cart3);
        assertEquals(1, cart3.get(id3.toString()).getProductAmount());

        // Test invalid string amount
        UUID id4 = UUID.randomUUID();
        Product p4 = new Product(id4, "Bad Amount", "desc", null, 10.0, 10, "/img4.png", null);
        ((FakeProductService)productService).setLast(p4);

        var mvcResult4 = mockMvc.perform(post("/cart").param("productId", id4.toString()).param("productAmount", "not-a-number")
            .session(session)).andExpect(status().is3xxRedirection()).andReturn();

        @SuppressWarnings("unchecked")
        Map<String, CartItem> cart4 = (Map<String, CartItem>) mvcResult4.getRequest().getSession().getAttribute("cart");
        assertNotNull(cart4);
        assertTrue(cart4.containsKey(id4.toString()));
        assertEquals(1, cart4.get(id4.toString()).getProductAmount());
    }

    @Test
    void decrease_nonexistentProduct_noError_and_noAdd() throws Exception {
        UUID id = UUID.randomUUID();
        Product p = new Product(id, "No Exist", "desc", null, 12.0, 10, "/img7.png", null);
        ((FakeProductService)productService).setLast(p);

        var mvcResult = mockMvc.perform(get("/cart/dp").param("productId", id.toString()).session(session))
            .andExpect(status().is3xxRedirection()).andReturn();

        @SuppressWarnings("unchecked")
        Map<String, CartItem> cart = (Map<String, CartItem>) mvcResult.getRequest().getSession().getAttribute("cart");
        // cart should exist (controller creates if missing) but should NOT contain the product
        assertNotNull(cart);
        assertFalse(cart.containsKey(id.toString()));
    }

    @Test
    void increase_when_no_cart_createsCartAndAddsProduct() throws Exception {
        UUID id = UUID.randomUUID();
        Product p = new Product(id, "Inc New", "desc", null, 15.0, 10, "/img8.png", null);
        ((FakeProductService)productService).setLast(p);

        var mvcResult = mockMvc.perform(get("/cart/ip").param("productId", id.toString()).session(session))
            .andExpect(status().is3xxRedirection()).andReturn();

        @SuppressWarnings("unchecked")
        Map<String, CartItem> cart = (Map<String, CartItem>) mvcResult.getRequest().getSession().getAttribute("cart");
        assertNotNull(cart);
        assertTrue(cart.containsKey(id.toString()));
        assertEquals(1, cart.get(id.toString()).getProductAmount());
    }

    @Test
    void addProduct_existingIncrements() throws Exception {
        UUID id = UUID.randomUUID();
        Product p = new Product(id, "Inc Existing", "desc", null, 30.0, 10, "/img9.png", null);
        ((FakeProductService)productService).setLast(p);

        // add 2
        mockMvc.perform(post("/cart").param("productId", id.toString()).param("productAmount", "2").session(session))
            .andExpect(status().is3xxRedirection());
        // add 3 more
        var mvcResult = mockMvc.perform(post("/cart").param("productId", id.toString()).param("productAmount", "3").session(session))
            .andExpect(status().is3xxRedirection()).andReturn();

        @SuppressWarnings("unchecked")
        Map<String, CartItem> cart = (Map<String, CartItem>) mvcResult.getRequest().getSession().getAttribute("cart");
        assertNotNull(cart);
        assertEquals(5, cart.get(id.toString()).getProductAmount());
    }

    @Test
    void increaseAmount_incrementsQuantity() throws Exception {
        UUID id = UUID.randomUUID();
        Product p = new Product(id, "Inc Product", "desc", null, 50.0, 10, "/img2.png", null);
    ((FakeProductService)productService).setLast(p);

        CartItem ct = new CartItem();
        ct.setProduct(p);
        ct.setProductAmount(1);
        Map<String, CartItem> cart = new HashMap<>();
        cart.put(id.toString(), ct);
        session.setAttribute("cart", cart);

    var mvcResult = mockMvc
        .perform(get("/cart/ip").param("productId", id.toString()).session(session))
        .andExpect(status().is3xxRedirection()).andReturn();

    @SuppressWarnings("unchecked")
    Map<String, CartItem> result = (Map<String, CartItem>) mvcResult.getRequest().getSession().getAttribute("cart");
    assertNotNull(result);
    assertEquals(2, result.get(id.toString()).getProductAmount());
    }

    @Test
    void decreaseAmount_removesWhenZero() throws Exception {
        UUID id = UUID.randomUUID();
        Product p = new Product(id, "Dec Product", "desc", null, 75.0, 10, "/img3.png", null);
    ((FakeProductService)productService).setLast(p);

        CartItem ct = new CartItem();
        ct.setProduct(p);
        ct.setProductAmount(1);
        Map<String, CartItem> cart = new HashMap<>();
        cart.put(id.toString(), ct);
        session.setAttribute("cart", cart);

    var mvcResult = mockMvc.perform(get("/cart/dp").param("productId", id.toString()).session(session))
        .andExpect(status().is3xxRedirection()).andReturn();

    @SuppressWarnings("unchecked")
    Map<String, CartItem> result = (Map<String, CartItem>) mvcResult.getRequest().getSession().getAttribute("cart");
    assertNotNull(result);
    assertFalse(result.containsKey(id.toString()));
    }

    @Test
    void getCart_withNoCart_returnsCheckoutViewAndModel() throws Exception {
        var mvcResult = mockMvc.perform(get("/cart").session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("Checkout"))
            .andExpect(model().attributeExists("cartProducts", "listProductIds"))
            .andReturn();

        // model should contain an empty listProductIds string
        Object listObj = mvcResult.getModelAndView().getModel().get("listProductIds");
        assertNotNull(listObj);
    }

    @Test
    void addProduct_withSizeAndUrl_storesAmountAndRedirects() throws Exception {
        UUID id = UUID.randomUUID();
        Product p = new Product(id, "With Params", "desc", null, 9.0, 10, "/img.png", null);
        ((FakeProductService)productService).setLast(p);

        var mvcResult = mockMvc.perform(post("/cart")
                .param("productId", id.toString())
                .param("productAmount", "2")
                .param("size", "L")
                .param("url", "/products")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andReturn();

        @SuppressWarnings("unchecked")
        Map<String, CartItem> cart = (Map<String, CartItem>) mvcResult.getRequest().getSession().getAttribute("cart");
        assertNotNull(cart);
        assertEquals(2, cart.get(id.toString()).getProductAmount());
    }


    @Test
    void decreaseAmount_decrementsWhenGreaterThanOne() throws Exception {
        UUID id = UUID.randomUUID();
        Product p = new Product(id, "Dec More", "desc", null, 6.0, 10, "/img.png", null);
        ((FakeProductService)productService).setLast(p);

        CartItem ct = new CartItem();
        ct.setProduct(p);
        ct.setProductAmount(3);
        Map<String, CartItem> cart = new HashMap<>();
        cart.put(id.toString(), ct);
        session.setAttribute("cart", cart);

        var mvcResult = mockMvc.perform(get("/cart/dp").param("productId", id.toString()).session(session))
            .andExpect(status().is3xxRedirection()).andReturn();

        @SuppressWarnings("unchecked")
        Map<String, CartItem> result = (Map<String, CartItem>) mvcResult.getRequest().getSession().getAttribute("cart");
        assertNotNull(result);
        assertEquals(2, result.get(id.toString()).getProductAmount());
    }

    @Test
    void increase_whenExistingItemHasNullAmount_setsToOne() throws Exception {
        UUID id = UUID.randomUUID();
        Product p = new Product(id, "Null Amount", "desc", null, 11.0, 10, "/img.png", null);
        ((FakeProductService)productService).setLast(p);

        // create CartItem with null productAmount
        CartItem ct = new CartItem();
        ct.setProduct(p);
        // intentionally do NOT set productAmount -> null
        Map<String, CartItem> cart = new HashMap<>();
        cart.put(id.toString(), ct);
        session.setAttribute("cart", cart);

        var mvcResult = mockMvc.perform(get("/cart/ip").param("productId", id.toString()).session(session))
            .andExpect(status().is3xxRedirection()).andReturn();

        @SuppressWarnings("unchecked")
        Map<String, CartItem> result = (Map<String, CartItem>) mvcResult.getRequest().getSession().getAttribute("cart");
        assertNotNull(result);
        assertEquals(1, result.get(id.toString()).getProductAmount());
    }

    @Test
    void addProduct_whenProductServiceReturnsNull_storesNullProduct() throws Exception {
        UUID id = UUID.randomUUID();
        // Do NOT set FakeProductService.last -> returns null

        var mvcResult = mockMvc.perform(post("/cart").param("productId", id.toString()).session(session))
            .andExpect(status().is3xxRedirection()).andReturn();

        @SuppressWarnings("unchecked")
        Map<String, CartItem> cart = (Map<String, CartItem>) mvcResult.getRequest().getSession().getAttribute("cart");
        assertNotNull(cart);
        assertTrue(cart.containsKey(id.toString()));
        assertNull(cart.get(id.toString()).getProduct());
        assertEquals(1, cart.get(id.toString()).getProductAmount());
    }

    @Test
    void increase_whenProductServiceReturnsNull_createsItemWithNullProductAndAmountOne() throws Exception {
        UUID id = UUID.randomUUID();
        // productService returns null by default

        var mvcResult = mockMvc.perform(get("/cart/ip").param("productId", id.toString()).session(session))
            .andExpect(status().is3xxRedirection()).andReturn();

        @SuppressWarnings("unchecked")
        Map<String, CartItem> cart = (Map<String, CartItem>) mvcResult.getRequest().getSession().getAttribute("cart");
        assertNotNull(cart);
        assertTrue(cart.containsKey(id.toString()));
        assertNull(cart.get(id.toString()).getProduct());
        assertEquals(1, cart.get(id.toString()).getProductAmount());
    }

}
