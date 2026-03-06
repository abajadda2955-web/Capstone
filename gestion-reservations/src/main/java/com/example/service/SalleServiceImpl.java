package com.example.service;

import com.example.model.Salle;
import com.example.repository.SalleRepository;
import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SalleServiceImpl implements SalleService {

    private final EntityManager em;
    private final SalleRepository salleRepository;

    public SalleServiceImpl(EntityManager em, SalleRepository salleRepository) {
        this.em = em;
        this.salleRepository = salleRepository;
    }

    @Override
    public List<Salle> findAvailableRooms(LocalDateTime start, LocalDateTime end) {
        return salleRepository.findAvailableRooms(start, end);
    }

    @Override
    public List<Salle> searchRooms(Map<String, Object> criteria) {
        return salleRepository.searchRooms(criteria);
    }

    @Override
    public List<Salle> getPaginatedRooms(int page, int pageSize) {
        return salleRepository.findPaginated(page, pageSize);
    }

    @Override
    public int getTotalPages(int pageSize) {
        long total = salleRepository.count();
        return (int) Math.ceil((double) total / pageSize);
    }

    @Override
    public long countRooms() {
        return salleRepository.count();
    }

    @Override
    public Salle getSalleById(Long id) {
        return salleRepository.findById(id);
    }

    @Override
    public Salle createSalle(Salle salle) {
        try {
            em.getTransaction().begin();
            Salle saved = salleRepository.save(salle);
            em.getTransaction().commit();
            return saved;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    @Override
    public Salle updateSalle(Salle salle) {
        try {
            em.getTransaction().begin();
            Salle updated = salleRepository.update(salle);
            em.getTransaction().commit();
            return updated;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    @Override
    public void deleteSalle(Long id) {
        try {
            em.getTransaction().begin();
            salleRepository.delete(id);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    @Override
    public List<Salle> getAllSalles() {
        return salleRepository.findAll();
    }

    @Override
    public List<Salle> findSallesWithEquipements() {
        return salleRepository.findWithEquipements();
    }
}