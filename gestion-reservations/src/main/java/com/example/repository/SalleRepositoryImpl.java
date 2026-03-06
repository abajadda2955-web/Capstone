package com.example.repository;

import com.example.model.Salle;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SalleRepositoryImpl implements SalleRepository {

    private final EntityManager em;

    public SalleRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public Salle findById(Long id) {
        return em.find(Salle.class, id);
    }

    @Override
    public List<Salle> findAll() {
        return em.createQuery("SELECT s FROM Salle s ORDER BY s.nom", Salle.class)
                .getResultList();
    }

    @Override
    public List<Salle> findAvailableRooms(LocalDateTime start, LocalDateTime end) {
        String jpql = "SELECT DISTINCT s FROM Salle s WHERE s.id NOT IN " +
                "(SELECT r.salle.id FROM Reservation r " +
                "WHERE (r.dateDebut <= :end AND r.dateFin >= :start) " +
                "AND r.statut != 'ANNULEE') " +
                "ORDER BY s.nom";

        return em.createQuery(jpql, Salle.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
    }

    @Override
    public List<Salle> searchRooms(Map<String, Object> criteria) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Salle> cq = cb.createQuery(Salle.class);
        Root<Salle> salle = cq.from(Salle.class);

        List<Predicate> predicates = new ArrayList<>();

        // Filtre par capacité minimum
        if (criteria.containsKey("capaciteMin")) {
            int capaciteMin = (int) criteria.get("capaciteMin");
            predicates.add(cb.ge(salle.get("capacite"), capaciteMin));
        }

        // Filtre par capacité maximum
        if (criteria.containsKey("capaciteMax")) {
            int capaciteMax = (int) criteria.get("capaciteMax");
            predicates.add(cb.le(salle.get("capacite"), capaciteMax));
        }

        // Filtre par bâtiment
        if (criteria.containsKey("batiment")) {
            String batiment = (String) criteria.get("batiment");
            predicates.add(cb.equal(salle.get("batiment"), batiment));
        }

        // Filtre par étage
        if (criteria.containsKey("etage")) {
            int etage = (int) criteria.get("etage");
            predicates.add(cb.equal(salle.get("etage"), etage));
        }

        // Filtre par équipement
        if (criteria.containsKey("equipement")) {
            Long equipementId = (Long) criteria.get("equipement");
            Join<?, ?> equipementJoin = salle.join("equipements");
            predicates.add(cb.equal(equipementJoin.get("id"), equipementId));
            cq.distinct(true);
        }

        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(salle.get("nom")));

        return em.createQuery(cq).getResultList();
    }

    @Override
    public List<Salle> findPaginated(int page, int pageSize) {
        return em.createQuery("SELECT s FROM Salle s ORDER BY s.id", Salle.class)
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    @Override
    public long count() {
        return em.createQuery("SELECT COUNT(s) FROM Salle s", Long.class)
                .getSingleResult();
    }

    @Override
    public Salle save(Salle salle) {
        em.persist(salle);
        return salle;
    }

    @Override
    public Salle update(Salle salle) {
        return em.merge(salle);
    }

    @Override
    public void delete(Long id) {
        Salle salle = findById(id);
        if (salle != null) {
            em.remove(salle);
        }
    }

    @Override
    public List<Salle> findWithEquipements() {
        return em.createQuery(
                        "SELECT DISTINCT s FROM Salle s LEFT JOIN FETCH s.equipements",
                        Salle.class)
                .getResultList();
    }

    @Override
    public List<Salle> findByCapaciteMin(int capacite) {
        return em.createQuery(
                        "SELECT s FROM Salle s WHERE s.capacite >= :capacite ORDER BY s.capacite",
                        Salle.class)
                .setParameter("capacite", capacite)
                .getResultList();
    }
}