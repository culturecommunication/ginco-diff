ginco-diff
==========


Développement
-------------

### Environnement de développement Eclipse

1. Récupérez une version locale du répository et importez le projet dans Eclipse (version JEE)
2. Ajoutez les facets "Dynamic Web Module 2.4" et "Java 1.6". Configurez le répertoire "/web" comme racine du contenu web et "/web/WEB-INF/classes" comme répertoire de compilation.
3. Ajoutez le JAR "/lib/javax.mail-1.5.1.jar" au Classpath
4. Ajoutez la librairie JUnit 4 (ou le JAR "/lib/junit-4.10.jar") au Classpath

NB : la webapp se déploie sur une serveur Tomcat dans Eclipse en respectant la configuration décrite au paragraphe "Déploiement".

### Compilation

Le projet comporte des tâches Ant pour réaliser la compilation en vue d'une installation :
- * clean * : vide les répertoires de compilation
- * dist * : complile l'application et génère le WAR de destination.

Vous pouvez personnaliser les paramètres de compilation en créant un fichier build.properties et en surchargeant les propriétés Ant désirées (cf. fichier build.xml du projet).

Déploiement
-----------

Pour déployer l'application, vous devez disposer d'un serveur Apache Tomcat 6.

1. Arrêter le serveur Tomcat (si besoin)
2. Copier le jar "/lib/javax.mail-1.5.1.jar" dans le répertoire "/lib" de Tomcat (cette librairie permet à l'application d'envoyer des emails).
3. Copier le war généré dans "/dist" (thesaurus.war par défaut) dans le repertoire "/webapps" de Tomcat.
4. Copier les 2 war "/lib/sesame/openrdf-sesame.war" et "/lib/sesame/openrdf-workbench.war" dans le repertoire "/webapps" de Tomcat.
5. Ajouter une * Resource * de type * javax.mail.Session * avec le nom JNDI * mail/thesaurus * au context du war de l'application. Par exemple, créer le fichier "/conf/Catalina/localhost/thesaurus.xml" dans Tomcat avec le contenu suivant :

> <?xml version="1.0" encoding="UTF-8"?>
> <Context>
> 	<Resource name="mail/thesaurus" auth="Container"
>             type="javax.mail.Session"
>             mail.smtp.host="127.0.0.1"/>
> </Context>

### Configurer SESAME

Avant de pouvoir utiliser l'application, il faut créer le répository sesame :
1. Accéder à l’URL http://localhost:8080/openrdf-workbench (remplacer "localhost" et "8080" si besoin en fonction de votre serveur).
2. Cliquer sur le lien * New repository *
3. Choisir le type * Native Java Store *, l'ID * thesaurus * et un title (champ est libre).
4. Cliquer sur * Next *, puis sur * Create *.
5. Vous pouvez maintenant utiliser l'application !

Paramétrage
-----------

L'application admet plusieurs paramétrages :
- Dans le * web.xml *
- Au travers des fichiers "*.properties"

### Paramètres du web.xml

- thesaurus.entries.base.uri : Racine des URI des thésaurus, pour la traduction vers l’URL de base de l’application, afin de garantir la navigation des données dans le cas où les URI des entrées ne correspondraient pas à des URL valides pour l’application
Valeur par défaut : vide (pas de traduction)
- thesaurus.rdf.repository.url : URL d’accès au triple store RDF
Valeur par défaut : http://localhost:8080/openrdf-sesame
- thesaurus.rdf.repository.id : Fichier paramétrage du déploiement de l’application
Valeur par défaut : thesaurus
- thesaurus.email.from : Adresse "from" par défaut des emails envoyés par l'application (logs d’erreur lors du chargement automatique d’un vocabulaire).
- thesaurus.contact.nbAttachments : Nombre maximum de pièce-jointes dans le formulaire de contact.
Valeur par défaut : 3.
- thesaurus.contact.attachmentsMaxSize : Taille maximum des pièce-jointes cumulées dans le formulaire de contact.
Valuer par défaut : 1 048 576 (= 1Mo).
- thesaurus.contact.reCaptchaPrivateKey : Clé privée pour activer reCaptcha (https://www.google.com/recaptcha) dans le formulaire de contact.
- thesaurus.contact.reCaptchaPublicKey : Clé publique pour activer reCaptcha (https://www.google.com/recaptcha) dans le formulaire de contact.

#### Activer le chargement automatique des vocabulaires

Pour activer le chargement automatique des vocabulaires depuis un répertoire, il faut décommenter la partie du fichier "web.xml" correspondant au filter * VocabularyAutoload *.
Ce filter admet plusieurs paramètres de configuration (init-param) :
- lookupDirectory : Obligatoire.
Répertoire à scruter. 
Ce répertoire doit exister et être accessible en écriture à l’application (déplacement des fichiers traités). 
Seuls les fichiers « *.rdf » sont pris en compte, et les sous-répertoires ne sont pas pris en compte.
- emailTo : Adresse email de destination des rapports d'erreurs. Laisser vide pour ne pas envoyer d’email.
- refresh : Intervalle de scrutation du répertoire en millisecondes.
Valeur par défaut : 10 000 (10 secondes).
- successDirectory : Répertoire de destination des vocabulaires chargés avec succès. 
Ce répertoire est créé s’il n’existe pas. Il doit être accessible en écriture.
Les fichiers déplacés sont renommés avec une extension correspondant à un numéro de lot (numéro identique pour tous les fichiers traités lors de la scrutation).
Valeur par défaut : <lookupDirectory>/success
- failureDirectory : Répertoire de destination des vocabulaires dont le chargement n’a pas été effectué. 
Ce répertoire est créé s’il n’existe pas. Il doit être accessible en écriture.
Les fichiers déplacés sont renommés avec une extension correspondant à un numéro de lot (numéro identique pour tous les fichiers traités lors de la scrutation) et un fichier de log portant ce même numéro de lot est créé dans le répertoire.
Valeur par défaut : <lookupDirectory>/failure

