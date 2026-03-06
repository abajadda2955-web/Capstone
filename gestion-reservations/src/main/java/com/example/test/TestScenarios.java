package com.example.test;

import com.example.model.*;
import com.example.service.ReservationService;
import com.example.service.SalleService;
import com.example.util.PaginationResult;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.OptimisticLockException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestScenarios {

    private final EntityManagerFactory emf;
    private final SalleService salleService;
    private final ReservationService reservationService;

    public TestScenarios(EntityManagerFactory emf, SalleService salleService, ReservationService reservationService) {
        this.emf = emf;
        this.salleService = salleService;
        this.reservationService = reservationService;
    }

    public void runAllTests() {
        System.out.println("\n === EXÉCUTION DES SCÉNARIOS DE TEST ===\n");

        testRechercheDisponibilite();
        testRechercheMultiCriteres();
        testPagination();
        testOptimisticLocking();
        testCachePerformance();

        System.out.println("\n === TOUS LES TESTS TERMINÉS ===\n");
    }

    private void testRechercheDisponibilite() {
        System.out.println("\n🔍 TEST 1: RECHERCHE DE DISPONIBILITÉ");
        System.out.println("----------------------------------------");

        // Test 1: Recherche de salles disponibles pour demain matin
        LocalDateTime demainMatin = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
        LocalDateTime demainMidi = demainMatin.plusHours(3);

        System.out.println(" Recherche de salles disponibles entre " +
                demainMatin.toLocalDate() + " " + demainMatin.toLocalTime() +
                " et " + demainMidi.toLocalTime());
        List<Salle> sallesDisponibles = salleService.findAvailableRooms(demainMatin, demainMidi);

        System.out.println("🔢 Nombre de salles disponibles: " + sallesDisponibles.size());
        for (int i = 0; i < Math.min(5, sallesDisponibles.size()); i++) {
            Salle salle = sallesDisponibles.get(i);
            System.out.println("   • " + salle.getNom() + " (Capacité: " + salle.getCapacite() +
                    ", Bâtiment: " + salle.getBatiment() + ")");
        }
        if (sallesDisponibles.size() > 5) {
            System.out.println("   • ... et " + (sallesDisponibles.size() - 5) + " autres salles");
        }

        // Test 2: Recherche de salles disponibles pour un créneau déjà réservé
        // Récupérer une réservation existante
        EntityManager em = emf.createEntityManager();
        try {
            Reservation reservation = em.createQuery(
                            "SELECT r FROM Reservation r WHERE r.statut = :statut", Reservation.class)
                    .setParameter("statut", StatutReservation.CONFIRMEE)
                    .setMaxResults(1)
                    .getSingleResult();

            System.out.println("\n Recherche de salles disponibles pendant une réservation existante:");
            System.out.println("   Créneau: " + reservation.getDateDebut() + " à " + reservation.getDateFin());
            System.out.println("   Salle déjà réservée: " + reservation.getSalle().getNom());

            List<Salle> sallesDispoCreneauReserve = salleService.findAvailableRooms(
                    reservation.getDateDebut(), reservation.getDateFin());

            System.out.println("   Nombre de salles disponibles: " + sallesDispoCreneauReserve.size());
            System.out.println("   La salle réservée est-elle exclue des résultats? " +
                    !sallesDispoCreneauReserve.contains(reservation.getSalle()));

        } catch (Exception e) {
            System.out.println("   ⚠ Aucune réservation trouvée pour ce test");
        } finally {
            em.close();
        }
    }

    private void testRechercheMultiCriteres() {
        System.out.println("\n TEST 2: RECHERCHE MULTI-CRITÈRES");
        System.out.println("----------------------------------------");

        // Test 1: Recherche par capacité et équipement
        Map<String, Object> criteres1 = new HashMap<>();
        criteres1.put("capaciteMin", 30);
        criteres1.put("equipement", 1L); // ID de l'équipement "Écran interactif"

        System.out.println(" Recherche: capacité >= 30 ET écran interactif");
        List<Salle> resultat1 = salleService.searchRooms(criteres1);

        System.out.println(" Nombre de salles trouvées: " + resultat1.size());
        for (Salle salle : resultat1) {
            System.out.println("   • " + salle.getNom() + " (Capacité: " + salle.getCapacite() + ")");
        }

        // Test 2: Recherche par bâtiment et étage
        Map<String, Object> criteres2 = new HashMap<>();
        criteres2.put("batiment", "Bâtiment C");
        criteres2.put("etage", 2);

        System.out.println("\n Recherche: Bâtiment C, Étage 2");
        List<Salle> resultat2 = salleService.searchRooms(criteres2);

        System.out.println(" Nombre de salles trouvées: " + resultat2.size());
        for (Salle salle : resultat2) {
            System.out.println("   • " + salle.getNom());
        }

        // Test 3: Recherche combinée complexe
        Map<String, Object> criteres3 = new HashMap<>();
        criteres3.put("capaciteMin", 20);
        criteres3.put("capaciteMax", 50);
        criteres3.put("batiment", "Bâtiment B");
        criteres3.put("equipement", 6L); // ID de l'équipement "Ordinateur fixe"

        System.out.println("\n Recherche complexe: capacité 20-50, Bâtiment B, ordinateur fixe");
        List<Salle> resultat3 = salleService.searchRooms(criteres3);

        System.out.println(" Nombre de salles trouvées: " + resultat3.size());
        for (Salle salle : resultat3) {
            System.out.println("   • " + salle.getNom() + " (Capacité: " + salle.getCapacite() + ")");
        }
    }

    private void testPagination() {
        System.out.println("\n TEST 3: PAGINATION");
        System.out.println("----------------------------------------");

        int pageSize = 5;

        // Test de pagination sur les salles
        System.out.println(" Pagination des salles (5 par page):");

        int totalPages = salleService.getTotalPages(pageSize);
        System.out.println(" Nombre total de pages: " + totalPages);

        for (int page = 1; page <= Math.min(3, totalPages); page++) {
            System.out.println("\n Page " + page + ":");

            List<Salle> sallesPage = salleService.getPaginatedRooms(page, pageSize);

            for (Salle salle : sallesPage) {
                System.out.println("   • " + salle.getNom() + " (ID: " + salle.getId() + ")");
            }
        }

        if (totalPages > 3) {
            System.out.println("\n   ... et " + (totalPages - 3) + " autres pages");
        }

        // Test avec PaginationResult
        System.out.println("\n Test avec PaginationResult:");

        long totalItems = salleService.countRooms();
        List<Salle> firstPageItems = salleService.getPaginatedRooms(1, pageSize);

        PaginationResult<Salle> paginationResult = new PaginationResult<>(
                firstPageItems, 1, pageSize, totalItems
        );

        System.out.println("   Page courante: " + paginationResult.getCurrentPage());
        System.out.println("   Taille de la page: " + paginationResult.getPageSize());
        System.out.println("   Nombre total de pages: " + paginationResult.getTotalPages());
        System.out.println("   Nombre total d'éléments: " + paginationResult.getTotalItems());
        System.out.println("   Page suivante disponible: " + paginationResult.hasNext());
        System.out.println("   Page précédente disponible: " + paginationResult.hasPrevious());
    }

    private void testOptimisticLocking() {
        System.out.println("\n TEST 4: OPTIMISTIC LOCKING");
        System.out.println("----------------------------------------");

        // Récupérer une réservation existante
        EntityManager em = emf.createEntityManager();
        Reservation reservation = null;

        try {
            reservation = em.createQuery(
                            "SELECT r FROM Reservation r WHERE r.statut = :statut", Reservation.class)
                    .setParameter("statut", StatutReservation.CONFIRMEE)
                    .setMaxResults(1)
                    .getSingleResult();

            System.out.println(" Réservation sélectionnée: ID=" + reservation.getId() +
                    ", Version=" + reservation.getVersion());

        } catch (Exception e) {
            System.out.println(" Aucune réservation trouvée pour le test d'optimistic locking");
            return;
        } finally {
            em.close();
        }

        final Long reservationId = reservation.getId();
        final Long initialVersion = reservation.getVersion();

        // Simuler deux utilisateurs modifiant la même réservation simultanément
        CountDownLatch latch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        System.out.println("\n Simulation de deux modifications simultanées...");

        // Premier thread: modification du motif
        executor.submit(() -> {
            try {
                latch.await();

                EntityManager em1 = emf.createEntityManager();
                try {
                    em1.getTransaction().begin();

                    Reservation r1 = em1.find(Reservation.class, reservationId);
                    System.out.println("👤 Thread 1: Réservation récupérée, version = " + r1.getVersion());

                    // Simuler un traitement long
                    Thread.sleep(500);

                    r1.setMotif("Motif modifié par Thread 1");

                    em1.merge(r1);
                    em1.getTransaction().commit();

                    System.out.println(" Thread 1: Réservation mise à jour avec succès !");

                } catch (OptimisticLockException e) {
                    System.out.println(" Thread 1: Conflit de verrouillage optimiste détecté !");
                    if (em1.getTransaction().isActive()) {
                        em1.getTransaction().rollback();
                    }
                } finally {
                    em1.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Deuxième thread: modification des dates
        executor.submit(() -> {
            try {
                latch.await();

                // Petit délai pour s'assurer que le Thread 1 commence en premier
                Thread.sleep(100);

                EntityManager em2 = emf.createEntityManager();
                try {
                    em2.getTransaction().begin();

                    Reservation r2 = em2.find(Reservation.class, reservationId);
                    System.out.println(" Thread 2: Réservation récupérée, version = " + r2.getVersion());

                    r2.setDateFin(r2.getDateFin().plusHours(1));

                    em2.merge(r2);
                    em2.getTransaction().commit();

                    System.out.println(" Thread 2: Réservation mise à jour avec succès !");

                } catch (OptimisticLockException e) {
                    System.out.println(" Thread 2: Conflit de verrouillage optimiste détecté !");
                    if (em2.getTransaction().isActive()) {
                        em2.getTransaction().rollback();
                    }
                } finally {
                    em2.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Démarrer les threads simultanément
        latch.countDown();

        // Attendre la fin des threads
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Vérifier l'état final de la réservation
        em = emf.createEntityManager();
        try {
            Reservation finalReservation = em.find(Reservation.class, reservationId);
            System.out.println("\n État final de la réservation:");
            System.out.println("   ID: " + finalReservation.getId());
            System.out.println("   Version initiale: " + initialVersion);
            System.out.println("   Version finale: " + finalReservation.getVersion());
            System.out.println("   Motif: " + finalReservation.getMotif());
            System.out.println("   Date fin: " + finalReservation.getDateFin());

            if (finalReservation.getVersion() > initialVersion) {
                System.out.println("    Une modification a réussi (version incrémentée)");
            } else {
                System.out.println("    Aucune modification n'a réussi");
            }

        } finally {
            em.close();
        }
    }

    private void testCachePerformance() {
        System.out.println("\n⚡ TEST 5: PERFORMANCE DU CACHE");
        System.out.println("----------------------------------------");

        // Test 1: Accès répété à la même entité sans cache
        System.out.println("\n Test d'accès répété sans cache:");

        // Vider le cache
        emf.getCache().evictAll();
        System.gc();

        // Mesurer le temps d'accès sans cache
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 50; i++) {
            EntityManager em = emf.createEntityManager();
            try {
                Salle salle = em.find(Salle.class, 1L);
                if (salle != null) {
                    salle.getEquipements().size();
                }
            } finally {
                em.close();
            }
        }

        long endTime = System.currentTimeMillis();
        long sansCacheTime = endTime - startTime;
        System.out.println("     Temps sans cache: " + sansCacheTime + "ms");

        // Test 2: Accès répété à la même entité avec cache
        System.out.println("\n Test d'accès répété avec cache:");

        startTime = System.currentTimeMillis();

        for (int i = 0; i < 50; i++) {
            EntityManager em = emf.createEntityManager();
            try {
                Salle salle = em.find(Salle.class, 1L);
                if (salle != null) {
                    salle.getEquipements().size();
                }
            } finally {
                em.close();
            }
        }

        endTime = System.currentTimeMillis();
        long avecCacheTime = endTime - startTime;
        System.out.println("     Temps avec cache: " + avecCacheTime + "ms");

        // Calculer le gain
        if (sansCacheTime > 0) {
            double gain = ((double)(sansCacheTime - avecCacheTime) / sansCacheTime) * 100;
            System.out.println("    Gain de performance: " + String.format("%.1f", gain) + "%");
        }
    }
}