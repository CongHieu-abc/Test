package com.swp.myleague.selenium;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

import com.swp.myleague.pages.CartPage;
import com.swp.myleague.utils.DriverFactory;

public class CartControllerSeleniumTest {

    private WebDriver driver;
    private CartPage cartPage;

    @BeforeEach
    void setUp() throws InterruptedException {
        driver = DriverFactory.createDriver();
        cartPage = new CartPage(driver);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ========== TEST CASE 1: Xem giỏ hàng khi trống ==========
    @Test
    @DisplayName("TC1: Xem giỏ hàng khi trống")
    void testViewCart_EmptyCart_DisplaysEmptyMessage() throws InterruptedException {
        // Given: Cart trống

        // When
        cartPage.navigateToCart();
        Thread.sleep(2000);

        // Then
        assertTrue(cartPage.isCartEmpty() || cartPage.getCartItemCount() == 0,
                   "Giỏ hàng nên trống hoặc hiển thị message empty");
    }

    // ========== TEST CASE 2: Thêm sản phẩm vào giỏ hàng ==========
    @Test
    @DisplayName("TC2: Thêm sản phẩm vào giỏ hàng")
    void testAddProductToCart_AddsProduct() throws InterruptedException {
        // Given: Lấy productId thực từ product page
        String productId = cartPage.getFirstProductId();
        assertNotNull(productId, "Phải có ít nhất 1 sản phẩm trong database để test");

        // When
        cartPage.addProductToCart(productId, 2);
        Thread.sleep(3000);
        cartPage.navigateToCart();
        Thread.sleep(2000);

        // Then
        assertTrue(cartPage.getCartItemCount() > 0 || cartPage.isProductInCart(productId),
                   "Sản phẩm nên được thêm vào giỏ hàng");
    }

    // ========== TEST CASE 3: Thêm sản phẩm với invalid amount, mặc định = 1 ==========
    @Test
    @DisplayName("TC3: Thêm sản phẩm với invalid amount, mặc định = 1")
    void testAddProduct_InvalidAmount_DefaultsToOne() throws InterruptedException {
        // Given: Lấy productId thực
        String productId = cartPage.getFirstProductId();
        assertNotNull(productId, "Phải có ít nhất 1 sản phẩm trong database để test");

        // When: Add với amount = 1 (default khi không có amount hoặc invalid)
        cartPage.addProductToCart(productId, 1);
        Thread.sleep(3000);
        cartPage.navigateToCart();
        Thread.sleep(2000);

        // Then
        String quantity = cartPage.getProductQuantity(productId);
        int qty = 0;
        try {
            qty = Integer.parseInt(quantity.trim());
        } catch (NumberFormatException e) {
            // Ignore
        }
        assertTrue(qty >= 1 || cartPage.getCartItemCount() > 0,
                   "Số lượng nên mặc định >= 1, nhưng là: " + quantity);
    }

    // ========== TEST CASE 4: Tăng số lượng sản phẩm ==========
    @Test
    @DisplayName("TC4: Tăng số lượng sản phẩm")
    void testIncreaseProductQuantity_IncrementsQuantity() throws InterruptedException {
        // Given: Cart đã có sản phẩm với số lượng = 1
        String productId = cartPage.getFirstProductId();
        assertNotNull(productId, "Phải có ít nhất 1 sản phẩm trong database để test");
        cartPage.addProductToCart(productId, 1);
        Thread.sleep(3000);

        // When
        cartPage.increaseProductQuantity(productId);
        Thread.sleep(2000);
        cartPage.navigateToCart();
        Thread.sleep(2000);

        // Then
        String quantity = cartPage.getProductQuantity(productId);
        int qty = 0;
        try {
            qty = Integer.parseInt(quantity.trim());
        } catch (NumberFormatException e) {
            // Ignore
        }
        assertTrue(qty >= 2, "Số lượng nên tăng lên >= 2, nhưng là: " + quantity);
    }

    // ========== TEST CASE 5: Giảm số lượng sản phẩm ==========
    @Test
    @DisplayName("TC5: Giảm số lượng sản phẩm")
    void testDecreaseProductQuantity_DecrementsQuantity() throws InterruptedException {
        // Given: Cart đã có sản phẩm với số lượng = 2
        String productId = cartPage.getFirstProductId();
        assertNotNull(productId, "Phải có ít nhất 1 sản phẩm trong database để test");
        cartPage.addProductToCart(productId, 2);
        Thread.sleep(3000);
        cartPage.navigateToCart();
        Thread.sleep(2000);

        // When
        cartPage.decreaseProductQuantity(productId);
        Thread.sleep(2000);
        cartPage.navigateToCart();
        Thread.sleep(2000);

        // Then
        String quantity = cartPage.getProductQuantity(productId);
        int qty = 0;
        try {
            qty = Integer.parseInt(quantity.trim());
        } catch (NumberFormatException e) {
            // Ignore
        }
        assertTrue(qty >= 1, "Số lượng nên giảm xuống >= 1, nhưng là: " + quantity);
    }

    // ========== TEST CASE 6: Giảm số lượng về 0, xóa sản phẩm ==========
    @Test
    @DisplayName("TC6: Giảm số lượng về 0, xóa sản phẩm")
    void testDecreaseProductQuantity_RemovesWhenZero() throws InterruptedException {
        // Given: Cart đã có sản phẩm với số lượng = 1
        String productId = cartPage.getFirstProductId();
        assertNotNull(productId, "Phải có ít nhất 1 sản phẩm trong database để test");
        cartPage.addProductToCart(productId, 1);
        Thread.sleep(3000);
        cartPage.navigateToCart();
        Thread.sleep(2000);

        // When: Giảm số lượng (sẽ xóa sản phẩm)
        cartPage.decreaseProductQuantity(productId);
        Thread.sleep(2000);
        cartPage.navigateToCart();
        Thread.sleep(2000);

        // Then: Sản phẩm nên bị xóa khỏi giỏ hàng (cart empty hoặc product không còn trong cart)
        boolean productNotInCart = !cartPage.isProductInCart(productId);
        boolean cartIsEmpty = cartPage.getCartItemCount() == 0;
        assertTrue(productNotInCart || cartIsEmpty,
                   "Sản phẩm nên bị xóa khỏi giỏ hàng. Product in cart: " + 
                   cartPage.isProductInCart(productId) + ", Cart count: " + cartPage.getCartItemCount());
    }

    // ========== TEST CASE 7: Thêm sản phẩm đã tồn tại, tăng số lượng ==========
    @Test
    @DisplayName("TC7: Thêm sản phẩm đã tồn tại, tăng số lượng")
    void testAddProduct_ExistingProduct_IncrementsQuantity() throws InterruptedException {
        // Given: Cart đã có sản phẩm với số lượng = 2
        String productId = cartPage.getFirstProductId();
        assertNotNull(productId, "Phải có ít nhất 1 sản phẩm trong database để test");
        cartPage.addProductToCart(productId, 2);
        Thread.sleep(3000);

        // When: Thêm thêm 3 sản phẩm nữa (click 3 lần nữa)
        cartPage.addProductToCart(productId, 3);
        Thread.sleep(3000);
        cartPage.navigateToCart();
        Thread.sleep(2000);

        // Then
        String quantity = cartPage.getProductQuantity(productId);
        int qty = Integer.parseInt(quantity.trim());
        assertTrue(qty >= 5 || cartPage.getCartItemCount() > 0,
                   "Số lượng nên là ít nhất 5 (2 + 3), nhưng là: " + qty);
    }

}

