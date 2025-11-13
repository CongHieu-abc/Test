package com.swp.myleague.common;

import java.util.List;

public interface IService<E> {
    
    public List<E> getAll();

    public E getById(String id);

    public E save(E e);

    public E delete(String id);

}
