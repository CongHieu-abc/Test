package com.swp.myleague.model.service.saleproductservice;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swp.myleague.common.CommonFunc;
import com.swp.myleague.common.IService;
import com.swp.myleague.model.entities.saleproduct.Orders;
import com.swp.myleague.model.repo.OrderRepo;

@Service
public class OrderService implements IService<Orders> {

    @Autowired
    private OrderRepo orderRepo;


    @Override
    public List<Orders> getAll() {
        return orderRepo.findAll();
    }

    public List<Orders> getByUserId(String userId) {
        return orderRepo.findAllByUserUserId(UUID.fromString(userId));
    }

    public Orders getByOrderCode(Long orderCode) {
        return orderRepo.findByOrderCode(orderCode).orElseThrow();
    }

    @Override
    public Orders getById(String id) {
        return orderRepo.findById(CommonFunc.convertStringToUUID(id)).orElseThrow();
    }

    @Override
    public Orders save(Orders o) {
        return orderRepo.save(o);
    }

    @Override
    public Orders delete(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

}
