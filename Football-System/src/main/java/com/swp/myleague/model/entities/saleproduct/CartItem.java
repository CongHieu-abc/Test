package com.swp.myleague.model.entities.saleproduct;

public class CartItem {

    private Product product;
    private Integer productAmount;

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getProductAmount() {
        return productAmount;
    }

    public void setProductAmount(Integer productAmount) {
        this.productAmount = productAmount;
    }

    public Double getTotalMoney() {
        return this.product.getProductPrice() * this.productAmount;
    }

}
