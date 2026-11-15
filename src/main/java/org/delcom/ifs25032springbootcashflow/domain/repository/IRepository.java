package org.delcom.ifs25032springbootcashflow.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface IRepository<T, ID> {
    Optional<T> findById(ID id);
    List<T> findAll();
    List<T> findBy(Predicate<T> predicate);
    T update(T entity);
    boolean deleteById(ID id);
}
