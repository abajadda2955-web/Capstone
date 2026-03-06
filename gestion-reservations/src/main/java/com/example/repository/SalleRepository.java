package com.example.repository;

import com.example.model.Salle;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface SalleRepository {
    Salle findById(Long id);
    List<Salle> findAll();
    List<Salle> findAvailableRooms(LocalDateTime start, LocalDateTime end);
    List<Salle> searchRooms(Map<String, Object> criteria);
    List<Salle> findPaginated(int page, int pageSize);
    long count();
    Salle save(Salle salle);
    Salle update(Salle salle);
    void delete(Long id);
    List<Salle> findWithEquipements();
    List<Salle> findByCapaciteMin(int capacite);
}