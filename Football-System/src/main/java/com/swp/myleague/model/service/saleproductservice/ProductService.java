package com.swp.myleague.model.service.saleproductservice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swp.myleague.common.CommonFunc;
import com.swp.myleague.common.IService;
import com.swp.myleague.model.entities.saleproduct.Orders;
import com.swp.myleague.model.entities.saleproduct.Product;
import com.swp.myleague.model.repo.ProductRepo;

@Service
public class ProductService implements IService<Product> {

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    OrderService orderService;

    @Override
    public List<Product> getAll() {
        return productRepo.findAll();
    }

    @Override
    public Product getById(String id) {
        return productRepo.findById(CommonFunc.convertStringToUUID(id)).orElseThrow();
    }

    @Override
    public Product save(Product e) {
        return productRepo.save(e);
    }

    @Override
    public Product delete(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    public Map<String, List<Product>> getRelatedProduct(Product product) {
        List<Product> products = getAll();
        Map<String, List<Product>> relatedProducts = new HashMap<>();
        relatedProducts.put("categoryProduct",
                products.stream().filter(p -> p.getCategoryProduct().equals(product.getCategoryProduct())).toList());
        List<Orders> orders = orderService.getAll();
        Map<Product, Integer> totalSale = new HashMap<>();
        for (Orders order : orders) {
            try {
                String info = order.getOrderInfo();
                if (info != null && info.startsWith("Product:")) {
                    String productIdStr = info.split(":")[1].trim();
                    Product p = getById(productIdStr);

                    totalSale.put(p, totalSale.getOrDefault(p, 0) + 1);
                }
            } catch (Exception e) {
                // Bỏ qua các đơn không hợp lệ
                continue;
            }
        }

        // Sắp xếp và lấy top 10
        List<Product> top10 = totalSale.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .limit(10)
                .map(Map.Entry::getKey)
                .toList();

        relatedProducts.put("top10MostSale", top10);
        return relatedProducts;
    }

}
