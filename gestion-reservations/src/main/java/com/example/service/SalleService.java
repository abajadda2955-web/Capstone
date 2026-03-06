package com.example.service;

import com.example.model.Salle;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface SalleService {
    List<Salle> findAvailableRooms(LocalDateTime start, LocalDateTime end);
    List<Salle> searchRooms(Map<String, Object> criteria);
    List<Salle> getPaginatedRooms(int page, int pageSize);
    int getTotalPages(int pageSize);
    long countRooms();
    Salle getSalleById(Long id);
    Salle createSalle(Salle salle);
    Salle updateSalle(Salle salle);
    void deleteSalle(Long id);
    List<Salle> getAllSalles();
    List<Salle> findSallesWithEquipements();
}