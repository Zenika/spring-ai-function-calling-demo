# Agentic AI Demo

Ce projet est une démonstration de l'utilisation de **Spring AI** avec des modèles d'IA agentiques, intégrant la mémoire de conversation et l'orchestration de modèles via **Spring Boot**. Il simule un assistant d'assurance santé capable de gérer des dossiers de remboursement via des interactions en langage naturel.

## 🎯 Fonctionnalités principales

- **Agent conversationnel intelligent** : Assistant spécialisé dans la gestion de dossiers de remboursement santé
- **Mémoire de conversation** : Maintien du contexte entre les échanges avec Spring AI Chat Memory
- **Function Calling** : Utilisation d'outils Spring AI pour interroger et mettre à jour la base de données
- **Interface web interactive** : Chat en temps réel avec streaming des réponses
- **Base de données PostgreSQL** : Gestion des dossiers de remboursement avec Liquibase
- **Support multilingue** : Prompts disponibles en français et anglais

## 🏗️ Architecture

### Structure du projet
```
src/main/java/com/zenika/demo/ai/functioncalling/
├── ai/                              # Composants IA
│   ├── DossierRemboursementPrompter.java    # Service principal d'interaction avec l'IA
│   ├── DossierRemboursementTools.java       # Outils (function calling) pour l'agent
│   └── DossierRemboursementSQLPrompter.java # Génération de requêtes SQL
├── config/                          # Configuration Spring
├── remboursements/                  # Domaine métier
│   ├── DossierRemboursement.java    # Entité principale
│   ├── DossiersRemboursements.java  # Repository
│   └── UseCases.java                # Logique métier
└── restapi/                         # API REST
    └── ConversationController.java  # Endpoints de conversation

src/main/resources/
├── application.yml                  # Configuration principale
├── prompts/                         # Prompts système pour l'IA
├── db/changelog/                    # Scripts Liquibase
├── static/js/chat.js                # Client JavaScript
└── templates/index.html             # Interface web
```

### Composants clés

#### 1. DossierRemboursementPrompter
Service principal orchestrant les interactions avec l'IA :
- Gère le contexte conversationnel via `ChatMemory`
- Configure les outils disponibles pour l'agent
- Support mode synchrone et streaming (asynchrone)

#### 2. DossierRemboursementTools
Outils Spring AI exposés à l'agent via `@Tool` :
- `getDossiers` : Recherche de dossiers avec requêtes SQL générées dynamiquement
- `internalActionGetDossier` : Récupération d'un dossier par ID
- `internalActionMetterAJourDossier` : Mise à jour (clôture/rejet) avec confirmation
- `lireReponseUtilisateur` : Interprétation des réponses utilisateur (oui/non)

#### 3. ConversationController
API REST exposant deux endpoints :
- `POST /conversations` : Réponse synchrone
- `POST /conversationsAsync` : Streaming des réponses

## 🚀 Démarrage rapide

### Prérequis
- **Java 21+**
- **Maven 3.9+**
- **Docker** (pour PostgreSQL)
- **Ollama** avec le modèle `llama3.1:8b` installé

### Installation d'Ollama et du modèle

```bash
# Installation d'Ollama (macOS)
brew install ollama

# Démarrage du service Ollama
ollama serve

# Installation du modèle (dans un autre terminal)
ollama pull llama3.1:8b
```

### Démarrage de l'application

1. **Cloner le dépôt** et naviguer dans le répertoire

2. **Démarrer PostgreSQL** :
   ```bash
   # Spring Boot Docker Compose démarre automatiquement PostgreSQL au lancement
   # Ou manuellement avec :
   docker compose -f src/main/docker/compose.yml up -d
   ```

3. **Compiler et lancer** :
   ```bash
   ./mvnw clean spring-boot:run
   ```

4. **Accéder à l'interface** : [http://localhost:8080](http://localhost:8080)

### Configuration

#### application.yml
```yaml
spring:
  ai:
    ollama:
      chat:
        model: llama3.1:8b           # Modèle utilisé
        options:
          temperature: 0.1           # Déterminisme élevé
  datasource:
    url: jdbc:postgresql://localhost:5432/dossiers_remboursements

dossier-remboursement:
  sql-system-prompt: classpath:prompts/dossier-remboursement-sql-query-system-prompt-en.md
  system-prompt: classpath:prompts/dossier-remboursement-system-prompt-en.md
```

Modèles alternatifs disponibles (commentés dans `application.yml`) :
- `llama3-groq-tool-use:8b`
- `llama3.2:3b`
- `hf.co/Salesforce/xLAM-1b-fc-r-gguf`

## 💡 Utilisation

### Exemples de commandes

**Consultation** :
- "Affiche tous les dossiers"
- "Liste les dossiers de Jean Dupont"
- "Montre-moi le dossier numéro 3"
- "Quels sont les dossiers en attente ?"

**Mise à jour** (avec confirmation) :
- "Clôture le dossier 5"
- "Rejette le dossier de Marie Martin"
- "Valide le dossier numéro 2"

### Flux de travail

1. L'utilisateur envoie une demande en langage naturel
2. L'agent analyse la demande et détermine les outils nécessaires
3. Pour les consultations : affichage direct des résultats
4. Pour les mises à jour : 
   - Affichage du dossier concerné
   - Demande de confirmation explicite
   - Exécution uniquement après validation

## 🛠️ Technologies utilisées

### Stack principale
- **Spring Boot 3.5.3** : Framework applicatif
- **Spring AI 1.0.3** : Intégration IA et function calling
- **Ollama** : Modèle LLM local (llama3.1:8b)
- **PostgreSQL 17** : Base de données
- **Liquibase** : Gestion des migrations de schéma

### Dépendances clés
- `spring-ai-starter-model-ollama` : Client Ollama
- `spring-ai-starter-model-chat-memory` : Mémoire conversationnelle
- `spring-boot-starter-thymeleaf` : Templates HTML
- `spring-boot-starter-data-jdbc` : Accès aux données
- `spring-boot-docker-compose` : Intégration Docker automatique
- `mapstruct` : Mapping objet
- `lombok` : Réduction du boilerplate

## 📊 Base de données

### Schéma
Table `dossier_remboursement` avec les colonnes :
- `id`, `nom_assure`, `prenom_assure`
- `montant_rembourse`, `statut`
- `date_demande`, etc.

### Données de test
Dossiers d'exemple chargés via Liquibase au démarrage :
- Fichier CSV : `src/main/resources/db/changelog/changes/data/dossiers-remboursement-init.csv`

## 🧪 Tests et développement

```bash
# Compilation
./mvnw clean compile

# Tests
./mvnw test

# Exécution en mode développement (avec devtools)
./mvnw spring-boot:run

# Package
./mvnw clean package
```

### Logs de débogage
Les logs détaillés des interactions IA sont activés :
```yaml
logging:
  level:
    org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor: debug
    com.zenika.demo.ai.agentic.agenticaidemo: debug
```

## 📝 Personnalisation

### Modifier les prompts système
Les fichiers dans `src/main/resources/prompts/` définissent le comportement de l'agent :
- `dossier-remboursement-system-prompt-en.md` : Instructions principales
- `dossier-remboursement-sql-query-system-prompt-en.md` : Génération SQL

### Changer de modèle LLM
Modifiez `spring.ai.ollama.chat.model` dans `application.yml` et assurez-vous que le modèle est installé via `ollama pull <model>`.

## 🎓 Contexte

Ce projet est une démonstration technique réalisée dans le cadre des **Matinales CNAM** par **Zenika** pour illustrer les capacités des agents IA avec Spring AI.

## 📄 Licence

Projet à but démonstratif et éducatif.

