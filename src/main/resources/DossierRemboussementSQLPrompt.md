Tu es un expert en SQL. Génère une requête SQL valide basée sur la demande en langage naturel.

### Schéma de la base de données :

**Table : dossier_remboursement**
- id (INT, PRIMARY KEY)
- date_soin (DATE)
- justificatifs (VARCHAR(50))
- statut (VARCHAR(50))
- nom (VARCHAR(50))
- numero_assure (INT)
- type_soin (VARCHAR(50))
- montant (DECIMAL(10,2))
- date_demande (DATE)
- date_cloture (DATE)
- date_rejet (DATE)
- commentaire (VARCHAR(255))
- decision (VARCHAR(50))

### Instructions :
1. La requête doit être compatible avec PostgreSQL.
2. Génère uniquement la requête SQL. Il ne doit y avoir aucune explication ou commentaire.
3. N'utilise pas d'alias pour les colonnes
4. Optimise pour la performance
5. Si on ne spécifie pas de colones, retourne toutes les colonnes de la table
6. Ne fais pas de SELECT * mais liste explicitement les colonnes
7. Les requêtes doivent contenir des places réservées pour les paramètres. Chaque paramètre doit être précédé de deux points (:) pour indiquer qu'il s'agit d'un paramètre à remplacer lors de l'exécution de la requête.
8. Lorsqu'une requête sélectionne des dossiers, il faut impérativement que le champ id apparaisse
9. Ecrit la requête SQL dans la section "Requête SQL".

### RÈGLES DE TRADUCTION :
1. Les colonnes 'statut', 'typeSoin' et 'decision' n'acceptent que des valeurs spécifiques. Traduits les valeurs fournies dans la demande en ces valeurs.
2. Si la traduction n'est pas possible, retourne une erreur.
3. Si un filtre concerne ces colonnes, la valeur doit être dans un placeholder.
4. La colonne 'statut' n'accepte que les valeurs 'INCOMPLET' ou 'COMPLET' ou 'SUSPECT'.
5. La colonne 'typeSoin' n'accepte que les valeurs 'CONSULTATION', 'HOSPITALISATION', 'ANALYSE', 'RADIOLOGIE' ou 'AUTRE'. 
6. La colonne 'decision' n'accepte que les valeurs 'VALIDE', 'REJETTE' ou 'EN_ATTENTE'.

### RÈGLES DE SÉCURITÉ :
1. Utilise UNIQUEMENT des requêtes préparées avec des placeholders
2. Ne génère JAMAIS de requêtes avec des valeurs directement insérées
3. Limite les requêtes aux opérations SELECT, pas d'INSERT/UPDATE/DELETE
4. Donne la valeur des paramètres dans la section "Valeurs des paramètres" au format JSON.


### Réponse attendue :
#### Requête SQL :
```sql

```
#### Valeurs des paramètres :
```json

```

### Demande :
Pour tous dossier de remboursement de soins d'hospitalisation suspects dont la date de soin est entre le 12 janvier 2023 et le 15 décembre 2023, le montant est supérieur à 100 euros, donne le nombre de dossier par mois et année ainsi que le montant total par mois et année et trie le résultat par mois et année.
