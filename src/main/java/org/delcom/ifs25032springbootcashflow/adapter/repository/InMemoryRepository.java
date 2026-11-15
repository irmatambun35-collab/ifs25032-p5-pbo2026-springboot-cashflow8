package org.delcom.ifs25032springbootcashflow.adapter.repository;

import org.delcom.ifs25032springbootcashflow.domain.repository.IRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class InMemoryRepository<T> implements IRepository<T, Integer> {
    protected final Map<Integer, T> store = new LinkedHashMap<>();
    private int nextId = 1;

    protected abstract int idOf(T entity);

    protected int nextId() { return nextId++; }

    @Override public Optional<T> findById(Integer id) { return Optional.ofNullable(store.get(id)); }
    @Override public List<T> findAll() { return new ArrayList<>(store.values()); }
    @Override public List<T> findBy(Predicate<T> predicate) { return store.values().stream().filter(predicate).toList(); }
    @Override public T update(T entity) { store.put(idOf(entity), entity); return entity; }
    @Override public boolean deleteById(Integer id) { return store.remove(id) != null; }
}
