package com.swp.myleague.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class CartPage extends BasePage {
    
    // Locators - Updated based on Checkout.html structure
    private static final By CART_ITEMS = By.cssSelector(".right-section .card-custom");
    private static final By PRODUCT_QUANTITY = By.cssSelector(".quantity-control span");
    private static final By PRODUCT_NAME = By.cssSelector(".card-custom strong");
    private static final By INCREASE_BUTTON = By.cssSelector(".quantity-control a[title='Increase qty'], .quantity-control a[href*='/cart/ip']");
    
    public CartPage(WebDriver driver) {
        super(driver);
    }
    
    public void navigateToCart() {
        navigateTo("http://localhost:8080/cart");
    }
    
    public void navigateToProducts() {
        navigateTo("http://localhost:8080/product");
    }
    
    /**
     * Add product to cart using POST request (required by controller)
     * Uses JavaScript to create and submit a form with POST method
     */
    public void addProductToCart(String productId, int amount) {
        // Navigate to products page first to ensure we have the page loaded
        navigateToProducts();
        try {
            Thread.sleep(2000); // Wait for products to load
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Use JavaScript to create and submit POST form
        // This is more reliable than clicking buttons which may trigger page reloads
        if (driver instanceof JavascriptExecutor) {
            try {
                // Ensure amount is at least 1 (controller will handle negative values)
                int actualAmount = Math.max(1, amount);
                String currentUrl = driver.getCurrentUrl();
                
                String script = String.format(
                    "(function() {" +
                    "  var form = document.createElement('form');" +
                    "  form.method = 'POST';" +
                    "  form.action = '/cart';" +
                    "  " +
                    "  var productIdInput = document.createElement('input');" +
                    "  productIdInput.type = 'hidden';" +
                    "  productIdInput.name = 'productId';" +
                    "  productIdInput.value = '%s';" +
                    "  " +
                    "  var amountInput = document.createElement('input');" +
                    "  amountInput.type = 'hidden';" +
                    "  amountInput.name = 'productAmount';" +
                    "  amountInput.value = '%d';" +
                    "  " +
                    "  form.appendChild(productIdInput);" +
                    "  form.appendChild(amountInput);" +
                    "  document.body.appendChild(form);" +
                    "  form.submit();" +
                    "})();",
                    productId, actualAmount
                );
                ((JavascriptExecutor) driver).executeScript(script);
                
                // Wait for redirect to /product (controller redirects there after adding to cart)
                try {
                    // Wait up to 10 seconds for URL to change
                    int maxWait = 10;
                    int waited = 0;
                    while (driver.getCurrentUrl().equals(currentUrl) && waited < maxWait) {
                        Thread.sleep(1000);
                        waited++;
                    }
                    // Additional wait to ensure page is loaded
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } catch (Exception e) {
                System.out.println("Error adding product to cart via JavaScript: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Get first available product ID from products page
     */
    public String getFirstProductId() {
        navigateToProducts();
        try {
            // Wait for products to load
            Thread.sleep(2000);
            // Find first product's add to cart button with data-id attribute
            List<WebElement> addButtons = driver.findElements(By.cssSelector(
                "button.add-to-cart-btn[data-id], button[data-id].add-to-cart-btn"
            ));
            if (!addButtons.isEmpty()) {
                WebElement firstButton = addButtons.get(0);
                String dataId = firstButton.getAttribute("data-id");
                if (dataId != null && !dataId.isEmpty()) {
                    return dataId;
                }
            }
            // Fallback: try to find any button with data-id
            List<WebElement> allButtons = driver.findElements(By.cssSelector("button[data-id]"));
            if (!allButtons.isEmpty()) {
                String dataId = allButtons.get(0).getAttribute("data-id");
                if (dataId != null && !dataId.isEmpty()) {
                    return dataId;
                }
            }
            // Fallback: try to extract from product links
            List<WebElement> productLinks = driver.findElements(By.cssSelector("a[href*='/product/']"));
            if (!productLinks.isEmpty()) {
                String href = productLinks.get(0).getAttribute("href");
                if (href != null && href.contains("/product/")) {
                    String[] parts = href.split("/product/");
                    if (parts.length > 1) {
                        String productId = parts[1].split("\\?")[0].split("/")[0].trim();
                        if (!productId.isEmpty()) {
                            return productId;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Could not get product ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    public void increaseProductQuantity(String productId) {
        // Navigate directly to increase endpoint (simpler and more reliable)
        navigateTo("http://localhost:8080/cart/ip?productId=" + productId);
        try {
            Thread.sleep(2000); // Wait for redirect back to cart
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public void decreaseProductQuantity(String productId) {
        // Navigate directly to decrease endpoint (simpler and more reliable)
        navigateTo("http://localhost:8080/cart/dp?productId=" + productId);
        try {
            Thread.sleep(2000); // Wait for redirect back to cart
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public int getCartItemCount() {
        navigateToCart();
        try {
            Thread.sleep(1000); // Wait for page to load
            List<WebElement> items = driver.findElements(CART_ITEMS);
            // Filter out non-product items (like coupon accordion, summary card)
            int count = 0;
            for (WebElement item : items) {
                try {
                    // Check if item has product name (indicating it's a product item)
                    item.findElement(PRODUCT_NAME);
                    count++;
                } catch (Exception e) {
                    // Not a product item, skip
                }
            }
            return count;
        } catch (Exception e) {
            return 0;
        }
    }
    
    public boolean isCartEmpty() {
        return getCartItemCount() == 0;
    }
    
    public String getProductQuantity(String productId) {
        navigateToCart();
        try {
            Thread.sleep(1000);
            // Find increase/decrease links that contain this productId
            List<WebElement> increaseLinks = driver.findElements(INCREASE_BUTTON);
            for (WebElement link : increaseLinks) {
                try {
                    String href = link.getAttribute("href");
                    if (href != null && href.contains("productId=" + productId)) {
                        // Find the quantity span in the same quantity-control div
                        WebElement quantityControl = link.findElement(By.xpath("./ancestor::div[contains(@class, 'quantity-control')]"));
                        WebElement quantityElement = quantityControl.findElement(PRODUCT_QUANTITY);
                        String qtyText = quantityElement.getText().trim();
                        // Extract just the number
                        qtyText = qtyText.replaceAll("[^0-9]", "");
                        return qtyText.isEmpty() ? "0" : qtyText;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        } catch (Exception e) {
            System.out.println("Could not get product quantity for productId " + productId + ": " + e.getMessage());
        }
        return "0";
    }
    
    public boolean isProductInCart(String productId) {
        navigateToCart();
        try {
            Thread.sleep(1000);
            // Check if any increase/decrease link contains this productId
            List<WebElement> increaseLinks = driver.findElements(INCREASE_BUTTON);
            for (WebElement link : increaseLinks) {
                try {
                    String href = link.getAttribute("href");
                    if (href != null && href.contains("productId=" + productId)) {
                        return true;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }
    
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}

