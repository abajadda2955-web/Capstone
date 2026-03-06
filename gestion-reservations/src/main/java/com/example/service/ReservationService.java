package com.example.service;

import com.example.model.Reservation;
import com.example.model.StatutReservation;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservationService {
    Reservation createReservation(Reservation reservation);
    Reservation updateReservation(Reservation reservation);
    void cancelReservation(Long id);
    Reservation getReservationById(Long id);
    List<Reservation> getReservationsByUtilisateur(Long utilisateurId);
    List<Reservation> getReservationsBySalle(Long salleId);
    List<Reservation> getReservationsByDateRange(LocalDateTime start, LocalDateTime end);
    List<Reservation> getReservationsByStatut(StatutReservation statut);
    List<Reservation> getPaginatedReservations(int page, int pageSize);
    boolean isSalleDisponible(Long salleId, LocalDateTime start, LocalDateTime end);
    int getTotalPages(int pageSize);
    long countReservations();
}