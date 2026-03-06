package com.example.util;

import com.example.model.*;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.Random;

public class DataInitializer {

    private final EntityManagerFactory emf;
    private final Random random = new Random();

    public DataInitializer(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void initializeData() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            // Création des équipements
            Equipement[] equipements = createEquipements(em);

            // Création des utilisateurs
            Utilisateur[] utilisateurs = createUtilisateurs(em);

            // Création des salles
            Salle[] salles = createSalles(em, equipements);

            // Création des réservations
            createReservations(em, utilisateurs, salles);

            em.getTransaction().commit();
            System.out.println("✅ Jeu de données initialisé avec succès !");
            System.out.println("   - " + equipements.length + " équipements");
            System.out.println("   - " + utilisateurs.length + " utilisateurs");
            System.out.println("   - " + salles.length + " salles");
            System.out.println("   - 100 réservations");

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ Erreur lors de l'initialisation des données:");
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    private Equipement[] createEquipements(EntityManager em) {
        System.out.println("Création des équipements...");

        Equipement[] equipements = new Equipement[10];

        String[][] equipData = {
                {"Projecteur HD", "Projecteur haute définition 4K", "PROJ-4K-001"},
                {"Écran interactif", "Écran tactile 65 pouces", "ECRAN-T-65"},
                {"Système de visioconférence", "Système complet avec caméra HD", "VISIO-HD-100"},
                {"Tableau blanc", "Tableau blanc magnétique 2m x 1m", "TB-MAG-2X1"},
                {"Système audio", "Système audio avec 4 haut-parleurs", "AUDIO-4HP"},
                {"Microphones sans fil", "Set de 4 microphones sans fil", "MIC-SF-4"},
                {"Ordinateur fixe", "PC avec Windows 11 et suite Office", "PC-W11-OFF"},
                {"Connexion WiFi haut débit", "WiFi 6 avec débit jusqu'à 1 Gbps", "WIFI-6-1G"},
                {"Système de climatisation", "Climatisation réglable", "CLIM-REG"},
                {"Prises électriques multiples", "10 prises électriques réparties", "PRISES-10"}
        };

        for (int i = 0; i < 10; i++) {
            equipements[i] = new Equipement(equipData[i][0], equipData[i][1]);
            equipements[i].setReference(equipData[i][2]);
            em.persist(equipements[i]);
        }

        return equipements;
    }

    private Utilisateur[] createUtilisateurs(EntityManager em) {
        System.out.println("Création des utilisateurs...");

        Utilisateur[] utilisateurs = new Utilisateur[20];

        String[] noms = {"Martin", "Bernard", "Dubois", "Thomas", "Robert", "Richard", "Petit", "Durand", "Leroy", "Moreau",
                "Simon", "Laurent", "Lefebvre", "Michel", "Garcia", "David", "Bertrand", "Roux", "Vincent", "Fournier"};

        String[] prenoms = {"Jean", "Marie", "Pierre", "Sophie", "Thomas", "Catherine", "Nicolas", "Isabelle", "Philippe", "Nathalie",
                "Michel", "Françoise", "Patrick", "Monique", "René", "Sylvie", "Louis", "Anne", "Daniel", "Christine"};

        String[] departements = {"Ressources Humaines", "Informatique", "Finance", "Marketing", "Commercial",
                "Production", "Recherche et Développement", "Juridique", "Communication", "Direction"};

        for (int i = 0; i < 20; i++) {
            utilisateurs[i] = new Utilisateur(noms[i], prenoms[i],
                    prenoms[i].toLowerCase() + "." + noms[i].toLowerCase() + "@example.com");
            utilisateurs[i].setTelephone("06" + (10000000 + random.nextInt(90000000)));
            utilisateurs[i].setDepartement(departements[i % 10]);
            em.persist(utilisateurs[i]);
        }

        return utilisateurs;
    }

    private Salle[] createSalles(EntityManager em, Equipement[] equipements) {
        System.out.println("Création des salles...");

        Salle[] salles = new Salle[15];

        // Bâtiment A - Salles de réunion standard
        for (int i = 0; i < 5; i++) {
            salles[i] = new Salle("Salle A" + (i+1), 10 + i*2);
            salles[i].setDescription("Salle de réunion standard");
            salles[i].setBatiment("Bâtiment A");
            salles[i].setEtage(i % 3 + 1);
            salles[i].setNumero("A" + (i+1));

            // Équipements de base pour toutes les salles
            salles[i].addEquipement(equipements[3]); // Tableau blanc
            salles[i].addEquipement(equipements[7]); // WiFi
            salles[i].addEquipement(equipements[9]); // Prises électriques

            // Équipements supplémentaires selon la salle
            if (i % 2 == 0) {
                salles[i].addEquipement(equipements[0]); // Projecteur
            }
            if (i % 3 == 0) {
                salles[i].addEquipement(equipements[4]); // Système audio
            }

            em.persist(salles[i]);
        }

        // Bâtiment B - Salles de formation
        for (int i = 5; i < 10; i++) {
            salles[i] = new Salle("Salle B" + (i-4), 20 + (i-5)*5);
            salles[i].setDescription("Salle de formation équipée");
            salles[i].setBatiment("Bâtiment B");
            salles[i].setEtage(i % 4 + 1);
            salles[i].setNumero("B" + (i-4));

            // Équipements pour salles de formation
            salles[i].addEquipement(equipements[0]); // Projecteur
            salles[i].addEquipement(equipements[3]); // Tableau blanc
            salles[i].addEquipement(equipements[6]); // Ordinateur fixe
            salles[i].addEquipement(equipements[7]); // WiFi
            salles[i].addEquipement(equipements[9]); // Prises électriques

            if (i % 2 == 0) {
                salles[i].addEquipement(equipements[1]); // Écran interactif
            }

            em.persist(salles[i]);
        }

        // Bâtiment C - Salles de conférence
        for (int i = 10; i < 15; i++) {
            salles[i] = new Salle("Salle C" + (i-9), 50 + (i-10)*20);
            salles[i].setDescription("Salle de conférence haut de gamme");
            salles[i].setBatiment("Bâtiment C");
            salles[i].setEtage(i % 3 + 1);
            salles[i].setNumero("C" + (i-9));

            // Équipements pour salles de conférence
            salles[i].addEquipement(equipements[0]); // Projecteur
            salles[i].addEquipement(equipements[2]); // Visioconférence
            salles[i].addEquipement(equipements[4]); // Système audio
            salles[i].addEquipement(equipements[5]); // Microphones
            salles[i].addEquipement(equipements[7]); // WiFi
            salles[i].addEquipement(equipements[8]); // Climatisation
            salles[i].addEquipement(equipements[9]); // Prises électriques

            em.persist(salles[i]);
        }

        return salles;
    }

    private void createReservations(EntityManager em, Utilisateur[] utilisateurs, Salle[] salles) {
        System.out.println("Création des réservations...");

        LocalDateTime now = LocalDateTime.now();
        String[] motifs = {
                "Réunion d'équipe", "Entretien", "Formation", "Présentation client",
                "Brainstorming", "Réunion de projet", "Conférence", "Atelier",
                "Séminaire", "Réunion de direction", "Démonstration produit"
        };

        // Créer 100 réservations réparties sur les 3 prochains mois
        for (int i = 0; i < 100; i++) {
            int jourOffset = random.nextInt(90); // 0-89 jours à partir d'aujourd'hui
            int heureDebut = 8 + random.nextInt(9); // 8h-16h
            int duree = 1 + random.nextInt(3); // 1-3 heures

            LocalDateTime dateDebut = now.plusDays(jourOffset)
                    .withHour(heureDebut)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);
            LocalDateTime dateFin = dateDebut.plusHours(duree);

            // Sélectionner un utilisateur et une salle aléatoires
            Utilisateur utilisateur = utilisateurs[random.nextInt(utilisateurs.length)];
            Salle salle = salles[random.nextInt(salles.length)];

            // Créer la réservation
            Reservation reservation = new Reservation(dateDebut, dateFin,
                    motifs[random.nextInt(motifs.length)]);

            // Définir le statut (80% confirmées, 10% en attente, 10% annulées)
            int statutRandom = random.nextInt(10);
            if (statutRandom < 8) {
                reservation.setStatut(StatutReservation.CONFIRMEE);
            } else if (statutRandom < 9) {
                reservation.setStatut(StatutReservation.EN_ATTENTE);
            } else {
                reservation.setStatut(StatutReservation.ANNULEE);
            }

            // Établir les relations
            reservation.setUtilisateur(utilisateur);
            reservation.setSalle(salle);

            // Ajouter aux listes des entités parentes
            utilisateur.getReservations().add(reservation);
            salle.getReservations().add(reservation);

            em.persist(reservation);
        }
    }
}