package com.example.service;

import com.example.model.Reservation;
import com.example.model.StatutReservation;
import com.example.repository.ReservationRepository;
import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationServiceImpl implements ReservationService {

    private final EntityManager em;
    private final ReservationRepository reservationRepository;

    public ReservationServiceImpl(EntityManager em, ReservationRepository reservationRepository) {
        this.em = em;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public Reservation createReservation(Reservation reservation) {
        // Vérifier la disponibilité avant de créer
        if (!isSalleDisponible(reservation.getSalle().getId(),
                reservation.getDateDebut(),
                reservation.getDateFin())) {
            throw new RuntimeException("La salle n'est pas disponible pour ce créneau");
        }

        try {
            em.getTransaction().begin();
            Reservation saved = reservationRepository.save(reservation);
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
    public Reservation updateReservation(Reservation reservation) {
        try {
            em.getTransaction().begin();
            Reservation updated = reservationRepository.update(reservation);
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
    public void cancelReservation(Long id) {
        try {
            em.getTransaction().begin();
            Reservation reservation = reservationRepository.findById(id);
            if (reservation != null) {
                reservation.setStatut(StatutReservation.ANNULEE);
                reservationRepository.update(reservation);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    @Override
    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id);
    }

    @Override
    public List<Reservation> getReservationsByUtilisateur(Long utilisateurId) {
        return reservationRepository.findByUtilisateur(utilisateurId);
    }

    @Override
    public List<Reservation> getReservationsBySalle(Long salleId) {
        return reservationRepository.findBySalle(salleId);
    }

    @Override
    public List<Reservation> getReservationsByDateRange(LocalDateTime start, LocalDateTime end) {
        return reservationRepository.findByDateRange(start, end);
    }

    @Override
    public List<Reservation> getReservationsByStatut(StatutReservation statut) {
        return reservationRepository.findByStatut(statut);
    }

    @Override
    public List<Reservation> getPaginatedReservations(int page, int pageSize) {
        return reservationRepository.findPaginated(page, pageSize);
    }

    @Override
    public boolean isSalleDisponible(Long salleId, LocalDateTime start, LocalDateTime end) {
        return reservationRepository.isSalleDisponible(salleId, start, end);
    }

    @Override
    public int getTotalPages(int pageSize) {
        long total = reservationRepository.count();
        return (int) Math.ceil((double) total / pageSize);
    }

    @Override
    public long countReservations() {
        return reservationRepository.count();
    }
}