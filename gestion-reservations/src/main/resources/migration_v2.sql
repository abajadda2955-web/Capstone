

CREATE TABLE IF NOT EXISTS backup_utilisateurs AS SELECT * FROM utilisateurs;
CREATE TABLE IF NOT EXISTS backup_salles AS SELECT * FROM salles;
CREATE TABLE IF NOT EXISTS backup_reservations AS SELECT * FROM reservations;
CREATE TABLE IF NOT EXISTS backup_equipements AS SELECT * FROM equipements;
CREATE TABLE IF NOT EXISTS backup_salle_equipement AS SELECT * FROM salle_equipement;



-- Table utilisateurs: ajout de la colonne departement
ALTER TABLE utilisateurs ADD COLUMN IF NOT EXISTS departement VARCHAR(100);

-- Table salles: ajout des colonnes numero et version
ALTER TABLE salles ADD COLUMN IF NOT EXISTS numero VARCHAR(20);
ALTER TABLE salles ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

-- Table equipements: ajout des colonnes reference et version
ALTER TABLE equipements ADD COLUMN IF NOT EXISTS reference VARCHAR(50);
ALTER TABLE equipements ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

-- Table reservations: ajout des colonnes statut et version
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS statut VARCHAR(20) DEFAULT 'CONFIRMEE';
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;



-- Mise à jour des statuts de réservation
UPDATE reservations SET statut = 'CONFIRMEE' WHERE statut IS NULL;



-- Index sur les dates de réservation
CREATE INDEX IF NOT EXISTS idx_reservation_dates ON reservations(date_debut, date_fin);

-- Index sur le statut des réservations
CREATE INDEX IF NOT EXISTS idx_reservation_statut ON reservations(statut);

-- Index sur la capacité des salles
CREATE INDEX IF NOT EXISTS idx_salle_capacite ON salles(capacite);

-- Index sur le bâtiment et l'étage
CREATE INDEX IF NOT EXISTS idx_salle_batiment_etage ON salles(batiment, etage);



-- Contrainte pour s'assurer que la date de fin est après la date de début
ALTER TABLE reservations ADD CONSTRAINT IF NOT EXISTS check_dates_coherentes
    CHECK (date_fin > date_debut);

-- Contrainte pour limiter les valeurs possibles du statut
ALTER TABLE reservations ADD CONSTRAINT IF NOT EXISTS check_statut_valide
    CHECK (statut IN ('CONFIRMEE', 'ANNULEE', 'EN_ATTENTE'));

-- ===========================================
-- 6. CRÉATION D'UNE VUE
-- ===========================================

CREATE OR REPLACE VIEW vue_reservations_completes AS
SELECT
    r.id,
    r.date_debut,
    r.date_fin,
    r.motif,
    r.statut,
    u.nom AS nom_utilisateur,
    u.prenom AS prenom_utilisateur,
    u.email,
    s.nom AS nom_salle,
    s.capacite,
    s.batiment,
    s.etage,
    s.numero
FROM
    reservations r
        JOIN utilisateurs u ON r.utilisateur_id = u.id
        JOIN salles s ON r.salle_id = s.id;



CREATE TABLE IF NOT EXISTS db_version (
                                          id INT PRIMARY KEY,
                                          version VARCHAR(10),
                                          date_mise_a_jour TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- H2 utilise MERGE au lieu de ON DUPLICATE KEY UPDATE
MERGE INTO db_version KEY(id) VALUES (1, '2.0', CURRENT_TIMESTAMP);



-- Afficher un message de confirmation
SELECT 'Migration vers version 2.0 terminée avec succès !' as message;

-- Fin du script de migration