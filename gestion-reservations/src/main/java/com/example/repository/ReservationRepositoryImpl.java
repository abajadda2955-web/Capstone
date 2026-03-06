package com.example.repository;

import com.example.model.Reservation;
import com.example.model.StatutReservation;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationRepositoryImpl implements ReservationRepository {

    private final EntityManager em;

    public ReservationRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public Reservation findById(Long id) {
        return em.find(Reservation.class, id);
    }

    @Override
    public List<Reservation> findAll() {
        return em.createQuery(
                        "SELECT r FROM Reservation r ORDER BY r.dateDebut DESC",
                        Reservation.class)
                .getResultList();
    }

    @Override
    public List<Reservation> findByUtilisateur(Long utilisateurId) {
        return em.createQuery(
                        "SELECT r FROM Reservation r WHERE r.utilisateur.id = :utilisateurId " +
                                "ORDER BY r.dateDebut DESC", Reservation.class)
                .setParameter("utilisateurId", utilisateurId)
                .getResultList();
    }

    @Override
    public List<Reservation> findBySalle(Long salleId) {
        return em.createQuery(
                        "SELECT r FROM Reservation r WHERE r.salle.id = :salleId " +
                                "ORDER BY r.dateDebut DESC", Reservation.class)
                .setParameter("salleId", salleId)
                .getResultList();
    }

    @Override
    public List<Reservation> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return em.createQuery(
                        "SELECT r FROM Reservation r WHERE " +
                                "(r.dateDebut BETWEEN :start AND :end OR r.dateFin BETWEEN :start AND :end) " +
                                "ORDER BY r.dateDebut", Reservation.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
    }

    @Override
    public List<Reservation> findByStatut(StatutReservation statut) {
        return em.createQuery(
                        "SELECT r FROM Reservation r WHERE r.statut = :statut " +
                                "ORDER BY r.dateDebut DESC", Reservation.class)
                .setParameter("statut", statut)
                .getResultList();
    }

    @Override
    public Reservation save(Reservation reservation) {
        em.persist(reservation);
        return reservation;
    }

    @Override
    public Reservation update(Reservation reservation) {
        return em.merge(reservation);
    }

    @Override
    public void delete(Long id) {
        Reservation reservation = findById(id);
        if (reservation != null) {
            em.remove(reservation);
        }
    }

    @Override
    public boolean isSalleDisponible(Long salleId, LocalDateTime start, LocalDateTime end) {
        Long count = em.createQuery(
                        "SELECT COUNT(r) FROM Reservation r WHERE r.salle.id = :salleId " +
                                "AND ((r.dateDebut <= :end AND r.dateFin >= :start)) " +
                                "AND r.statut != 'ANNULEE'", Long.class)
                .setParameter("salleId", salleId)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
        return count == 0;
    }

    @Override
    public List<Reservation> findPaginated(int page, int pageSize) {
        return em.createQuery(
                        "SELECT r FROM Reservation r ORDER BY r.dateDebut DESC",
                        Reservation.class)
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    @Override
    public long count() {
        return em.createQuery("SELECT COUNT(r) FROM Reservation r", Long.class)
                .getSingleResult();
    }
}