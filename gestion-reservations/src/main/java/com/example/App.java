package com.example;

import com.example.model.*;
import com.example.repository.*;
import com.example.service.*;
import com.example.test.TestScenarios;
import com.example.util.DataInitializer;
import com.example.util.DatabaseMigrationTool;
import com.example.util.PerformanceReport;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Scanner;

public class App {

    private static EntityManagerFactory emf;
    private static EntityManager em;
    private static SalleService salleService;
    private static ReservationService reservationService;
    private static Scanner scanner;

    public static void main(String[] args) {

        System.out.println("║   APPLICATION DE RÉSERVATION DE SALLES v2.0     ║");


        try {
            // Initialisation
            initializeApplication();

            // Menu principal
            showMainMenu();

        } catch (Exception e) {
            System.err.println(" Erreur fatale: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Nettoyage
            cleanup();
        }
    }

    private static void initializeApplication() {
        System.out.println("\n Initialisation de l'application...");

        // Création de l'EntityManagerFactory
        emf = Persistence.createEntityManagerFactory("gestion-reservations");
        em = emf.createEntityManager();

        // Initialisation des repositories
        SalleRepository salleRepository = new SalleRepositoryImpl(em);
        ReservationRepository reservationRepository = new ReservationRepositoryImpl(em);

        // Initialisation des services
        salleService = new SalleServiceImpl(em, salleRepository);
        reservationService = new ReservationServiceImpl(em, reservationRepository);

        scanner = new Scanner(System.in);

        System.out.println(" Application initialisée avec succès !");
    }

    private static void showMainMenu() {
        boolean exit = false;

        while (!exit) {
            System.out.println("\n╔════════════════════════════════════════════╗");
            System.out.println("║               MENU PRINCIPAL               ║");
            System.out.println("╠════════════════════════════════════════════╣");
            System.out.println("║ 1. Initialiser les données de test         ║");
            System.out.println("║ 2. Exécuter les scénarios de test          ║");
            System.out.println("║ 3. Exécuter le script de migration         ║");
            System.out.println("║ 4. Générer un rapport de performance       ║");
            System.out.println("║ 5. Afficher les statistiques               ║");
            System.out.println("║ 6. Quitter                                 ║");
            System.out.println("╚════════════════════════════════════════════╝");
            System.out.print(" Votre choix: ");

            int choice = -1;
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consommer la nouvelle ligne
            } catch (Exception e) {
                scanner.nextLine(); // Vider le buffer
                System.out.println(" Veuillez entrer un nombre valide.");
                continue;
            }

            switch (choice) {
                case 1:
                    initializeTestData();
                    break;

                case 2:
                    runTestScenarios();
                    break;

                case 3:
                    runMigration();
                    break;

                case 4:
                    generatePerformanceReport();
                    break;

                case 5:
                    showStatistics();
                    break;

                case 6:
                    exit = true;
                    System.out.println(" Au revoir !");
                    break;

                default:
                    System.out.println(" Choix invalide. Veuillez réessayer.");
            }
        }
    }

    private static void initializeTestData() {
        System.out.println("\n Initialisation des données de test...");
        DataInitializer dataInitializer = new DataInitializer(emf);
        dataInitializer.initializeData();
    }

    private static void runTestScenarios() {
        System.out.println("\n Exécution des scénarios de test...");
        TestScenarios testScenarios = new TestScenarios(emf, salleService, reservationService);
        testScenarios.runAllTests();
    }

    private static void runMigration() {
        System.out.println("\n Exécution du script de migration...");
        System.out.print(" Voulez-vous utiliser H2 (test) ou MySQL (production)? (h2/mysql): ");
        String dbChoice = scanner.nextLine();

        if (dbChoice.equalsIgnoreCase("h2")) {
            DatabaseMigrationTool migrationTool = new DatabaseMigrationTool(
                    "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
                    "sa",
                    ""
            );
            migrationTool.executeMigration();
        } else if (dbChoice.equalsIgnoreCase("mysql")) {
            System.out.print(" JDBC URL (ex: jdbc:mysql://localhost:3306/reservation_salles): ");
            String jdbcUrl = scanner.nextLine();
            System.out.print(" Utilisateur: ");
            String user = scanner.nextLine();
            System.out.print(" Mot de passe: ");
            String password = scanner.nextLine();

            DatabaseMigrationTool migrationTool = new DatabaseMigrationTool(jdbcUrl, user, password);
            migrationTool.executeMigration();
        } else {
            System.out.println(" Choix invalide. Migration annulée.");
        }
    }

    private static void generatePerformanceReport() {
        System.out.println("\n Génération du rapport de performance...");
        PerformanceReport performanceReport = new PerformanceReport(emf);
        performanceReport.runPerformanceTests();
    }

    private static void showStatistics() {
        System.out.println("\n STATISTIQUES DE L'APPLICATION");
        System.out.println("----------------------------------------");

        try {
            long nbSalles = salleService.countRooms();
            long nbReservations = reservationService.countReservations();

            System.out.println(" Nombre de salles: " + nbSalles);
            System.out.println(" Nombre de réservations: " + nbReservations);

            if (nbSalles > 0 && nbReservations > 0) {
                System.out.println(" Moyenne réservations/salle: " +
                        String.format("%.2f", (double) nbReservations / nbSalles));
            }

        } catch (Exception e) {
            System.out.println(" Erreur lors de la récupération des statistiques");
            System.out.println("   Assurez-vous d'avoir initialisé les données d'abord.");
        }
    }

    private static void cleanup() {
        if (em != null && em.isOpen()) {
            em.close();
        }
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        if (scanner != null) {
            scanner.close();
        }
        System.out.println(" Nettoyage terminé.");
    }
}