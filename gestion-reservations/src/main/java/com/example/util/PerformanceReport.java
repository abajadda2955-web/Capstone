package com.example.util;

import com.example.model.Salle;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PerformanceReport {

    private final EntityManagerFactory emf;
    private final Map<String, TestResult> results = new HashMap<>();
    private long startTime;

    public PerformanceReport(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void runPerformanceTests() {
        System.out.println("\n📊 Exécution des tests de performance...");
        System.out.println("======================================");

        // Réinitialiser les statistiques Hibernate
        resetStatistics();

        // Test 1: Recherche de salles disponibles
        testPerformance("Recherche de salles disponibles", () -> {
            EntityManager em = emf.createEntityManager();
            try {
                LocalDateTime start = LocalDateTime.now().plusDays(1).with(LocalTime.of(9, 0));
                LocalDateTime end = start.plusHours(2);

                return em.createQuery(
                                "SELECT DISTINCT s FROM Salle s WHERE s.id NOT IN " +
                                        "(SELECT r.salle.id FROM Reservation r " +
                                        "WHERE (r.dateDebut <= :end AND r.dateFin >= :start))")
                        .setParameter("start", start)
                        .setParameter("end", end)
                        .getResultList();
            } finally {
                em.close();
            }
        });

        // Test 2: Recherche multi-critères
        testPerformance("Recherche multi-critères", () -> {
            EntityManager em = emf.createEntityManager();
            try {
                return em.createQuery(
                                "SELECT DISTINCT s FROM Salle s JOIN s.equipements e " +
                                        "WHERE s.capacite >= :capacite AND s.batiment = :batiment AND e.id = :equipementId")
                        .setParameter("capacite", 30)
                        .setParameter("batiment", "Bâtiment B")
                        .setParameter("equipementId", 1L)
                        .getResultList();
            } finally {
                em.close();
            }
        });

        // Test 3: Pagination
        testPerformance("Pagination", () -> {
            EntityManager em = emf.createEntityManager();
            try {
                return em.createQuery("SELECT s FROM Salle s ORDER BY s.id", Salle.class)
                        .setFirstResult(0)
                        .setMaxResults(10)
                        .getResultList();
            } finally {
                em.close();
            }
        });

        // Test 4: Accès répété avec cache
        testPerformance("Accès répété avec cache", () -> {
            Object result = null;
            for (int i = 0; i < 100; i++) {
                EntityManager em = emf.createEntityManager();
                try {
                    result = em.find(Salle.class, 1L);
                    // Forcer le chargement des équipements
                    if (result instanceof Salle) {
                        ((Salle) result).getEquipements().size();
                    }
                } finally {
                    em.close();
                }
            }
            return result;
        });

        // Test 5: Requête avec JOIN FETCH
        testPerformance("Requête avec JOIN FETCH", () -> {
            EntityManager em = emf.createEntityManager();
            try {
                return em.createQuery(
                                "SELECT DISTINCT s FROM Salle s LEFT JOIN FETCH s.equipements WHERE s.capacite > 20",
                                Salle.class)
                        .getResultList();
            } finally {
                em.close();
            }
        });

        // Générer le rapport
        generateReport();
    }

    private void testPerformance(String testName, Supplier<?> testFunction) {
        System.out.print(" Test: " + testName + "... ");

        // Réinitialiser les statistiques avant le test
        resetStatistics();

        // Mesurer le temps d'exécution
        long startTime = System.currentTimeMillis();
        Object result = testFunction.get();
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Récupérer les statistiques Hibernate
        EntityManager em = emf.createEntityManager();
        Session session = em.unwrap(Session.class);
        Statistics stats = session.getSessionFactory().getStatistics();
        em.close();

        // Enregistrer les résultats
        TestResult testResult = new TestResult();
        testResult.executionTime = executionTime;
        testResult.queryCount = stats.getQueryExecutionCount();
        testResult.entityLoadCount = stats.getEntityLoadCount();
        testResult.cacheHitCount = stats.getSecondLevelCacheHitCount();
        testResult.cacheMissCount = stats.getSecondLevelCacheMissCount();
        testResult.resultSize = (result instanceof java.util.Collection) ?
                ((java.util.Collection<?>) result).size() : (result != null ? 1 : 0);

        results.put(testName, testResult);

        System.out.println(" terminé en " + executionTime + "ms");
    }

    private void resetStatistics() {
        EntityManager em = emf.createEntityManager();
        Session session = em.unwrap(Session.class);
        Statistics stats = session.getSessionFactory().getStatistics();
        stats.clear();
        em.close();
    }

    private void generateReport() {
        System.out.println("\n  Génération du rapport de performance...");

        String fileName = "performance_report_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("╔══════════════════════════════════════════════════════════════╗");
            writer.println("║                    RAPPORT DE PERFORMANCE                    ║");
            writer.println("╚══════════════════════════════════════════════════════════════╝");
            writer.println();
            writer.println("  Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.println("================================================================\n");

            for (Map.Entry<String, TestResult> entry : results.entrySet()) {
                writer.println("🔹 TEST: " + entry.getKey());
                writer.println("     Temps d'exécution: " + entry.getValue().executionTime + " ms");
                writer.println("     Nombre de requêtes: " + entry.getValue().queryCount);
                writer.println("     Entités chargées: " + entry.getValue().entityLoadCount);
                writer.println("     Hits du cache: " + entry.getValue().cacheHitCount);
                writer.println("     Miss du cache: " + entry.getValue().cacheMissCount);
                writer.println("     Taille du résultat: " + entry.getValue().resultSize);

                // Calculer le ratio de hit du cache
                long totalCacheAccess = entry.getValue().cacheHitCount + entry.getValue().cacheMissCount;
                double cacheHitRatio = totalCacheAccess > 0 ?
                        (double) entry.getValue().cacheHitCount / totalCacheAccess : 0;
                writer.println("   📊 Ratio de hit du cache: " + String.format("%.2f", cacheHitRatio * 100) + "%");

                writer.println("   ----------------------------------\n");
            }

            writer.println("\n╔══════════════════════════════════════════════════════════════╗");
            writer.println("║                    RECOMMANDATIONS                            ║");
            writer.println("╚══════════════════════════════════════════════════════════════╝\n");

            // Analyser les résultats et fournir des recommandations
            boolean needsCacheOptimization = results.values().stream()
                    .anyMatch(r -> r.cacheHitCount < r.cacheMissCount && r.queryCount > 5);

            boolean needsQueryOptimization = results.values().stream()
                    .anyMatch(r -> r.queryCount > 10 || r.executionTime > 500);

            if (needsCacheOptimization) {
                writer.println(" 1. OPTIMISATION DU CACHE RECOMMANDÉE:");
                writer.println("   • Vérifier la configuration du cache de second niveau dans ehcache.xml");
                writer.println("   • Ajuster les paramètres timeToLive et maxElementsInMemory");
                writer.println("   • Activer le cache de requête pour les requêtes fréquentes");
                writer.println("   • Utiliser @Cache sur les associations fréquentes\n");
            }

            if (needsQueryOptimization) {
                writer.println(" 2. OPTIMISATION DES REQUÊTES RECOMMANDÉE:");
                writer.println("   • Utiliser JOIN FETCH pour éviter les problèmes N+1");
                writer.println("   • Créer des index sur les colonnes fréquemment utilisées");
                writer.println("   • Optimiser les requêtes qui prennent plus de 500ms");
                writer.println("   • Utiliser la pagination pour les gros volumes\n");
            }

            writer.println("    3. RECOMMANDATIONS GÉNÉRALES:");
            writer.println("   • Surveiller régulièrement les performances avec Hibernate Statistics");
            writer.println("   • Mettre en place un monitoring en production");
            writer.println("   • Considérer l'utilisation d'un pool de connexions (HikariCP)");
            writer.println("   • Tester avec différentes configurations de cache");

            System.out.println(" Rapport généré: " + fileName);

        } catch (IOException e) {
            System.err.println(" Erreur lors de la génération du rapport: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static class TestResult {
        long executionTime;
        long queryCount;
        long entityLoadCount;
        long cacheHitCount;
        long cacheMissCount;
        int resultSize;
    }
}