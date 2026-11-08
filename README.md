# TP5 — Invocation de méthodes sur des objets distants avec Java RMI

**Module :** Développement d'applications réparties  
**Filière :** LSI 3 — Faculté des Sciences de Sfax  
**Année universitaire :** 2026 – 2027  
**Enseignante des TPs :** Zeineb DAOUD  
**Réalisé par :** *Nom Prénom*  
**Langage :** Java (JDK 8 ou supérieur)  
**Technologies :** Java RMI, JNDI, Maven  
**Port du registre RMI :** `1099`

- **Activité 5-1 :** Service de conversion de devise (Euro → Dinar)
- **Activité 5-2 :** Service de banque avec RMI et JNDI

---

## 1. Objectifs

Ce TP a pour but de mettre en œuvre une communication client/serveur avec **Java RMI** (Remote Method Invocation) :

- définir une interface distante (`Remote`) ;
- implémenter le service côté serveur (`UnicastRemoteObject`) ;
- enregistrer l'objet distant dans un annuaire (**RMI Registry**) ;
- invoquer les méthodes distantes depuis un client à l'aide d'un *stub* ;
- comprendre l'intégration entre **JNDI** et RMI pour l'enregistrement (`bind`) et la recherche (`lookup`) d'objets distants.

## 2. Environnement utilisé

- Java JDK 8 ou supérieur
- IntelliJ IDEA (projets Maven)
- Packages Java : `java.rmi`, `java.rmi.registry`, `java.rmi.server`, `javax.naming`

## 3. Organisation du projet

```text
TP5/
├── Activite5-1_Conversion/
│   ├── TP5_RMIConversion_Serveur/
│   │   ├── src/main/java/
│   │   │   ├── rmiService/
│   │   │   │   ├── IConversion.java
│   │   │   │   └── ConversionImpl.java
│   │   │   └── rmiServer/
│   │   │       └── ConversionServer.java
│   │   ├── conversionClient.jar
│   │   └── pom.xml
│   └── TP5_RMIConversion_Client/
│       ├── src/main/java/rmiClient/ConversionClient.java
│       └── pom.xml
├── Activite5-2_Banque/
│   ├── RMIBanque_Server/
│   │   ├── src/main/
│   │   │   ├── java/
│   │   │   │   ├── metier/Compte.java
│   │   │   │   ├── rmiService/
│   │   │   │   │   ├── IBanque.java
│   │   │   │   │   └── BanqueImpl.java
│   │   │   │   └── rmiServer/BanqueServer.java
│   │   │   └── resources/jndi.properties
│   │   └── pom.xml
│   └── RMIBanque_Client/
│       ├── src/main/
│       │   ├── java/rmiClient/BanqueClient.java
│       │   └── resources/jndi.properties
│       └── pom.xml
├── captures/
└── README.md
```

> **Captures d'écran :** les images référencées dans ce document sont des emplacements à compléter. Après exécution, placez vos captures dans le dossier `captures/` avec les noms indiqués.

## 4. Principe général de RMI

```text
┌──────────────┐   1. lookup("nom")    ┌────────────────┐
│    Client    │ ────────────────────► │  RMI Registry  │
│ (stub local) │ ◄──────────────────── │   (port 1099)  │
└──────┬───────┘   2. référence/stub   └────────▲───────┘
       │                                        │ 0. rebind("nom", objet)
       │ 3. appel de méthode distante           │
       ▼                                ┌───────┴────────┐
 (sérialisation des paramètres) ──────► │    Serveur     │
                                        │ (objet distant)│
                                        └────────────────┘
```

Cycle de vie d'un service distant :

1. **Définition de l'interface** : elle étend `Remote` et chaque méthode déclare `RemoteException`.
2. **Implémentation** : la classe étend `UnicastRemoteObject`, ce qui exporte automatiquement l'objet.
3. **Enregistrement** : le serveur démarre le registre et publie l'objet sous un nom.
4. **Invocation** : le client recherche l'objet par son nom, puis appelle ses méthodes comme s'il était local.

Le client n'a besoin que de l'**interface** (et des classes métier échangées) : c'est pourquoi un fichier `.jar` contenant uniquement ces éléments lui est fourni, sans la classe d'implémentation.

---

# Activité 5-1 : Service de conversion de devise

## 5. Énoncé résumé

Le serveur offre aux clients un service distant qui convertit un montant en **euros** vers le **dinar**. Le client invoque la méthode distante `convertirMontant(500)`.

## 6. Étapes réalisées

### Étape 1 — Préparation de l'espace de travail

Création du répertoire `votreNomPrenom_LSI3_DevAppReparties`.

![Espace de travail](captures/5-1_etape1_workspace.png)

### Étape 2 — Développement du serveur (`RMIConversion_Server`)

**Interface distante `IConversion`** (package `rmiService`) :

```java
package rmiService;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IConversion extends Remote {
    double convertirMontant(double mt) throws RemoteException;
}
```

**Implémentation `ConversionImpl`** (package `rmiService`) :

```java
package rmiService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ConversionImpl extends UnicastRemoteObject implements IConversion {

    public ConversionImpl() throws RemoteException {
        super(); // exportation automatique de l'objet
    }

    @Override
    public double convertirMontant(double mt) throws RemoteException {
        return mt * 3; // taux de conversion Euro -> Dinar utilisé dans ce TP
    }
}
```

**Application serveur `ConversionServer`** (package `rmiServer`) :

```java
package rmiServer;

import rmiService.ConversionImpl;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class ConversionServer {

    public static void main(String[] args) {
        try {
            // i. Activer l'annuaire RMIRegistry (port 1099)
            LocateRegistry.createRegistry(1099);

            // ii. Créer une instance de ConversionImpl
            ConversionImpl od = new ConversionImpl();

            // iii. Publier la référence dans l'annuaire
            Naming.rebind("rmi://localhost:1099/BK", od);
            System.out.println("Serveur RMI de conversion démarré...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

![Projet serveur](captures/5-1_etape2_serveur.png)

### Étape 3 — Développement du client (`RMIConversion_Client`)

1. Création du projet `RMIConversion_Client`.
2. Export du package `rmiService` du projet serveur **en sélectionnant uniquement `IConversion`** (sans `ConversionImpl`) dans le fichier `conversionClient.jar`.
3. Ajout du `.jar` au classpath du projet client.

![Export du JAR](captures/5-1_etape3_export_jar.png)

![Ajout du JAR au classpath](captures/5-1_etape3_classpath.png)

**Application client `ConversionClient`** (package `rmiClient`) :

```java
package rmiClient;

import rmiService.IConversion;
import java.rmi.Naming;

public class ConversionClient {

    public static void main(String[] args) {
        try {
            // Chercher la référence de l'objet distant dans le RMIRegistry
            IConversion stub = (IConversion) Naming.lookup("rmi://localhost:1099/BK");

            // Appel de la méthode distante
            double result = stub.convertirMontant(500);

            System.out.println("Résultat de la conversion : " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### Étape 4 — Exécution

1. Lancer d'abord `ConversionServer`.
2. Lancer ensuite `ConversionClient`.

**Serveur démarré :**

![Serveur de conversion démarré](captures/5-1_etape4_serveur.png)

**Résultat côté client :**

![Résultat de la conversion](captures/5-1_etape4_client.png)

Résultat attendu pour `convertirMontant(500)` avec un taux de 3 : `1500.0`.

**Résultat d'exécution (console) :**

Serveur :

```text
ConversionImpl[UnicastServerRef [liveRef: [endpoint:[127.0.0.1:46197](local),objID:[-4e8e7cdd:1a1bcae70fc:-7fff, -5706872708945581138]]]]
Serveur RMI de conversion démarré...
```

Client :

```text
Résultat de la conversion : 1500.0
```

Client lancé alors que le serveur est arrêté :

```text
java.rmi.ConnectException: Connection refused to host: localhost; nested exception is:
	java.net.ConnectException: Connection refused
```


## 7. Tests — Activité 5-1

| Appel | Résultat attendu |
|---|---|
| `convertirMontant(500)` | `1500.0` |
| `convertirMontant(100)` | `300.0` |
| `convertirMontant(0)` | `0.0` |
| Client lancé sans serveur | `ConnectException` (registre introuvable) |

## 8. Synthèse — Activité 5-1

Cette activité a permis de parcourir le cycle complet d'un service RMI : définition de l'interface `IConversion`, implémentation dans `ConversionImpl`, publication dans le registre via `LocateRegistry.createRegistry()` et `Naming.rebind()`, puis invocation depuis le client après `Naming.lookup()`.

Le client ne possède que l'interface distante grâce au fichier `conversionClient.jar` : il ne connaît pas l'implémentation, ce qui illustre la séparation entre contrat et réalisation. L'appel `stub.convertirMontant(500)` se comporte comme un appel local, alors que le calcul est effectué dans la JVM du serveur.

---

# Activité 5-2 : Service de banque avec RMI et JNDI

## 9. Énoncé résumé

Une application répartie gère des comptes bancaires. Un compte est défini par un **code** (`int`), un **solde** (`double`) et une **date de création** (`Date`). Le serveur RMI central offre deux services distants :

- **créer un compte** avec un code et une somme initiale (si le code n'existe pas déjà) ;
- **consulter un compte** à partir de son code.

## 10. Étapes réalisées

### Étape 1 — Préparation de l'espace de travail

![Espace de travail](captures/5-2_etape1_workspace.png)

### Étape 2 — Développement du serveur (`RMIBanque_Server`)

**Classe métier `Compte`** (package `metier`) — sérialisable, car elle est transmise du client vers le serveur :

```java
package metier;

import java.io.Serializable;
import java.util.Date;

public class Compte implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;
    private double solde;
    private Date dateCreation;

    public Compte(int code, double solde) {
        this.code = code;
        this.solde = solde;
        this.dateCreation = new Date();
    }

    public int getCode() { return code; }
    public double getSolde() { return solde; }
    public Date getDateCreation() { return dateCreation; }
    public void setSolde(double solde) { this.solde = solde; }

    @Override
    public String toString() {
        return "Compte{code=" + code + ", solde=" + solde
                + ", dateCreation=" + dateCreation + '}';
    }
}
```

**Interface distante `IBanque`** (package `rmiService`) :

```java
package rmiService;

import metier.Compte;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IBanque extends Remote {
    String creerCompte(Compte c) throws RemoteException;
    String getInfoCompte(int code) throws RemoteException;
}
```

**Implémentation `BanqueImpl`** (package `rmiService`) :

```java
package rmiService;

import metier.Compte;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class BanqueImpl extends UnicastRemoteObject implements IBanque {

    private Map<Integer, Compte> comptes;

    public BanqueImpl() throws RemoteException {
        super();
        comptes = new HashMap<>();
    }

    @Override
    public String creerCompte(Compte c) throws RemoteException {
        if (comptes.containsKey(c.getCode())) {
            return "Erreur : ce compte existe déjà.";
        }
        comptes.put(c.getCode(), c);
        return "Compte créé avec succès.";
    }

    @Override
    public String getInfoCompte(int code) throws RemoteException {
        Compte c = comptes.get(code);
        if (c == null) {
            return "Compte introuvable.";
        }
        return c.toString();
    }
}
```

Les comptes sont stockés dans une `HashMap<Integer, Compte>` dont la clé est le code du compte, ce qui permet de vérifier l'unicité du code avec `containsKey()`.

![Projet serveur Banque](captures/5-2_etape2_serveur.png)

### Étape 3 — Développement du client (`RMIBanque_Client`)

1. Création du projet `RMIBanque_Client`.
2. Génération d'un JAR contenant **uniquement** `metier/Compte` et `rmiService/IBanque` (sans `BanqueImpl`). Dans ce projet Maven, le JAR est produit avec le classifier `api` grâce au `maven-jar-plugin` :

```xml
<configuration>
    <classifier>api</classifier>
    <includes>
        <include>metier/Compte.class</include>
        <include>rmiService/IBanque.class</include>
    </includes>
</configuration>
```

3. Ajout de ce JAR comme dépendance du projet client :

```xml
<dependency>
    <groupId>com.exemple</groupId>
    <artifactId>RMIBanque_Server</artifactId>
    <version>1.0-SNAPSHOT</version>
    <classifier>api</classifier>
</dependency>
```

![Génération du JAR](captures/5-2_etape3_jar.png)

![Dépendance du client](captures/5-2_etape3_classpath.png)

**Client `BanqueClient`** (version avec le registre RMI, étapes 3 et 4) :

```java
package rmiClient;

import metier.Compte;
import rmiService.IBanque;
import java.rmi.Naming;

public class BanqueClient {

    public static void main(String[] args) {
        try {
            IBanque banque = (IBanque) Naming.lookup("rmi://localhost:1099/Banque");

            // Création d'un compte
            Compte c1 = new Compte(1001, 5000);
            System.out.println(banque.creerCompte(c1));

            // Consultation du compte
            System.out.println(banque.getInfoCompte(1001));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

**Serveur `BanqueServer`** (version avec le registre RMI) :

```java
package rmiServer;

import rmiService.BanqueImpl;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class BanqueServer {

    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);
            BanqueImpl banque = new BanqueImpl();
            Naming.rebind("rmi://localhost:1099/Banque", banque);
            System.out.println("Serveur Banque démarré avec succès !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### Étape 4 — Exécution de l'application

1. Démarrer le serveur RMI (`BanqueServer`).
2. Lancer le client (`BanqueClient`).

**Serveur démarré :**

![Serveur Banque démarré](captures/5-2_etape4_serveur.png)

**Création et consultation d'un compte (client) :**

![Résultat du client](captures/5-2_etape4_client.png)

**Résultat d'exécution (console) :**

Serveur :

```text
Serveur Banque démarré avec succès !
```

Client :

```text
Compte créé avec succès.
Compte{code=1001, solde=5000.0, dateCreation=Sun Nov 08 19:41:57 UTC 2026}
```

**Cas particuliers (code déjà existant, compte introuvable) :**

```text
Compte créé avec succès.
Erreur : ce compte existe déjà.
Compte{code=1001, solde=5000.0, dateCreation=Sun Nov 08 19:42:11 UTC 2026}
Compte introuvable.
```

### Étape 5 — Utilisation de JNDI pour l'enregistrement RMI

**JNDI** (Java Naming and Directory Interface) est une API générique d'accès aux services de nommage. Avec le fournisseur `RegistryContextFactory`, elle permet d'utiliser le registre RMI à travers l'interface `InitialContext`, sans appeler directement `Naming` ni `LocateRegistry` pour le `bind` et le `lookup`.

**Fichier `jndi.properties`** placé dans `src/main/resources` (racine du classpath), côté serveur **et** côté client :

```properties
java.naming.factory.initial=com.sun.jndi.rmi.registry.RegistryContextFactory
java.naming.provider.url=rmi://localhost:1099
```

| Propriété | Rôle |
|---|---|
| `java.naming.factory.initial` | Fabrique de contexte : ici, celle du registre RMI |
| `java.naming.provider.url` | Adresse du registre RMI (hôte et port) |

**`BanqueServer` modifié (JNDI) :**

```java
package rmiServer;

import rmiService.BanqueImpl;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.rmi.registry.LocateRegistry;

public class BanqueServer {

    public static void main(String[] args) {
        try {
            // Le registre RMI doit exister avant le bind JNDI
            LocateRegistry.createRegistry(1099);

            BanqueImpl banque = new BanqueImpl();

            // Configuration lue automatiquement depuis jndi.properties
            Context ctx = new InitialContext();
            ctx.rebind("Banque", banque);

            System.out.println("Serveur Banque démarré avec succès (JNDI) !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

**`BanqueClient` modifié (JNDI) :**

```java
package rmiClient;

import metier.Compte;
import rmiService.IBanque;
import javax.naming.Context;
import javax.naming.InitialContext;

public class BanqueClient {

    public static void main(String[] args) {
        try {
            Context ctx = new InitialContext();
            IBanque banque = (IBanque) ctx.lookup("Banque");

            Compte c1 = new Compte(1001, 5000);
            System.out.println(banque.creerCompte(c1));
            System.out.println(banque.getInfoCompte(1001));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

![Fichier jndi.properties](captures/5-2_etape5_jndi_properties.png)

![BanqueServer avec JNDI](captures/5-2_etape5_serveur_jndi.png)

![BanqueClient avec JNDI](captures/5-2_etape5_client_jndi.png)

**Relance de l'application avec JNDI :**

![Exécution serveur JNDI](captures/5-2_etape5_run_serveur.png)

![Exécution client JNDI](captures/5-2_etape5_run_client.png)

**Résultat d'exécution avec JNDI (console) :**

Serveur :

```text
Serveur Banque démarré avec succès (JNDI) !
```

Client :

```text
Compte créé avec succès.
Compte{code=1001, solde=5000.0, dateCreation=Sun Nov 08 19:42:02 UTC 2026}
```

Les résultats sont identiques à ceux obtenus avec `Naming` : la communication RMI fonctionne toujours, seule la méthode d'enregistrement et de recherche a changé.

## 11. Tests — Activité 5-2

| Test | Action | Résultat attendu |
|---|---|---|
| Création d'un compte | `creerCompte(new Compte(1001, 5000))` | `Compte créé avec succès.` |
| Code déjà existant | Second appel avec le code `1001` | `Erreur : ce compte existe déjà.` |
| Consultation | `getInfoCompte(1001)` | `Compte{code=1001, solde=5000.0, dateCreation=...}` |
| Compte inexistant | `getInfoCompte(9999)` | `Compte introuvable.` |
| Avec JNDI | Relancer serveur et client | Mêmes résultats qu'avec `Naming` |

## 12. Comparaison : `Naming` / `LocateRegistry` et JNDI

| Élément | RMI direct | JNDI |
|---|---|---|
| Enregistrement | `Naming.rebind("rmi://localhost:1099/Banque", obj)` | `ctx.rebind("Banque", obj)` |
| Recherche | `Naming.lookup("rmi://localhost:1099/Banque")` | `ctx.lookup("Banque")` |
| Adresse du registre | Écrite dans le code (URL) | Externalisée dans `jndi.properties` |
| Dépendance au registre RMI | Forte (API RMI) | Faible (API JNDI générique) |
| Changement d'annuaire | Modification du code | Modification de la configuration |
| Démarrage du registre | `LocateRegistry.createRegistry(1099)` | Identique (le registre doit exister) |

## 13. Synthèse — Activité 5-2

Cette activité a appliqué RMI à un cas plus réaliste : la classe `Compte`, sérialisable, est passée **par valeur** du client au serveur, tandis que l'objet `BanqueImpl` reste sur le serveur et est manipulé **par référence** à travers son stub. Le serveur centralise la gestion de tous les comptes dans une `HashMap` et refuse la création d'un compte dont le code existe déjà.

Le JAR distribué au client contient seulement `IBanque` et `Compte`, jamais `BanqueImpl`. L'utilisation de JNDI a ensuite permis de déplacer l'adresse du registre dans `jndi.properties` : le code utilise `InitialContext` avec des noms simples (`"Banque"`), et le service d'annuaire peut changer sans modifier les classes Java. Le fonctionnement est identique à celui de la version avec `Naming`.

## 14. Exécution des programmes

**Activité 5-1**

1. Lancer `ConversionServer`.
2. Lancer `ConversionClient`.

**Activité 5-2**

1. Vérifier que `jndi.properties` est présent dans `src/main/resources` du serveur et du client.
2. Générer le JAR `api` du serveur : `mvn clean install` dans `RMIBanque_Server`.
3. Lancer `BanqueServer`.
4. Lancer `BanqueClient`.

> **Remarque :** pour utiliser deux ordinateurs, remplacer `localhost` par l'adresse IP du serveur côté client (URL RMI ou `java.naming.provider.url`) et vérifier que le port `1099` est autorisé par le pare-feu. Si le serveur est sur une machine multi-interfaces, il peut être nécessaire de définir `-Djava.rmi.server.hostname=<IP du serveur>`.

## 15. Conclusion générale

Ce TP a montré comment construire des applications réparties avec Java RMI : interface distante, implémentation, publication dans un registre et invocation à distance. L'activité 5-1 a présenté le cycle de vie complet d'un service avec un cas simple. L'activité 5-2 y a ajouté un objet métier sérialisable, la gestion d'état côté serveur et l'abstraction apportée par JNDI pour l'enregistrement et la recherche des objets distants.
