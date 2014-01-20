ginco-diff
==========


Développement
-------------

### Environnement de développement Eclipse

1. Récupérez une version locale du répository et importez le projet dans Eclipse (version JEE)
2. Ajoutez les facets "Dynamic Web Module 2.4" et "Java 1.6". Configurez le répertoire "/web" comme racine du contenu web et "/web/WEB-INF/classes" comme répertoire de compilation.
3. Ajoutez le JAR "/lib/javax.mail-1.5.1.jar" au Classpath
4. Ajoutez la librairie JUnit 4 (ou le JAR "/lib/junit-4.10.jar") au Classpath
5. Ajoutez la librairie servlet-api.jar au Classpath
6. Ajoutez le répertoire test/java au Buildpath

NB : la webapp se déploie sur une serveur Tomcat dans Eclipse en respectant la configuration décrite au paragraphe "Déploiement".

### Compilation

Le projet comporte des tâches Ant pour réaliser la compilation en vue d'une installation :
- __clean__ : vide les répertoires de compilation
- __dist__ : complile l'application et génère le WAR de destination.

Vous pouvez personnaliser les paramètres de compilation en créant un fichier build.properties et en surchargeant les propriétés Ant désirées (cf. fichier build.xml du projet).

Déploiement
-----------

Pour déployer l'application, vous devez disposer d'un serveur Apache Tomcat 6.

1. Arrêter le serveur Tomcat (si besoin)
2. Copier le jar "/lib/javax.mail-1.5.1.jar" dans le répertoire "/lib" de Tomcat (cette librairie permet à l'application d'envoyer des emails).
3. Copier le war généré dans "/dist" (thesaurus.war par défaut) dans le repertoire "/webapps" de Tomcat.
4. Copier les 2 war "/lib/sesame/openrdf-sesame.war" et "/lib/sesame/openrdf-workbench.war" dans le repertoire "/webapps" de Tomcat.
5. Ajouter une __Resource__ de type __javax.mail.Session__ avec le nom JNDI __mail/thesaurus__ au context du war de l'application. Par exemple, créer le fichier "/conf/Catalina/localhost/thesaurus.xml" dans Tomcat avec le contenu suivant :

    <pre>
    &lt;?xml version="1.0" encoding="UTF-8"?&gt;
    &lt;Context&gt;
    	&lt;Resource name="mail/thesaurus" auth="Container"
                type="javax.mail.Session"
                mail.smtp.host="127.0.0.1"/&gt;
    &lt;/Context&gt;
    </pre>

NB : Si la Session email n'est pas trouvée au démarrage de l'application, un warning (non bloquant) apparait dans les logs. L'application fonctionne alors, à l'exception du formulaire de contact et des logs d'erreurs par email.

### Configurer SESAME

Avant de pouvoir utiliser l'application, il faut créer le répository sesame :

1. Accéder à l’URL http://localhost:8080/openrdf-workbench (remplacer "localhost" et "8080" si besoin en fonction de votre serveur).
2. Cliquer sur le lien __New repository__
3. Choisir le type __Native Java Store__, l'ID __thesaurus__ et un title (le contenu de ce champ est libre).
4. Cliquer sur __Next__, puis sur __Create__.
5. Vous pouvez maintenant utiliser l'application !

Paramétrage
-----------

L'application admet plusieurs paramétrages :
- Dans le _web.xml_
- Au travers des fichiers "*.properties"

### Paramètres du web.xml

- __thesaurus.entries.base.uri__ : Racine des URI des thésaurus, pour la traduction vers l’URL de base de l’application, afin de garantir la navigation des données dans le cas où les URI des entrées ne correspondraient pas à des URL valides pour l’application
Valeur par défaut : vide (pas de traduction)
- __thesaurus.rdf.repository.url__ : URL d’accès au triple store RDF
Valeur par défaut : http://localhost:8080/openrdf-sesame
- __thesaurus.rdf.repository.id__ : Fichier paramétrage du déploiement de l’application
Valeur par défaut : thesaurus
- __thesaurus.email.from__ : Adresse "from" par défaut des emails envoyés par l'application (logs d’erreur lors du chargement automatique d’un vocabulaire).
- __thesaurus.contact.nbAttachments__ : Nombre maximum de pièce-jointes dans le formulaire de contact.
Valeur par défaut : 3.
- __thesaurus.contact.attachmentsMaxSize__ : Taille maximum des pièce-jointes cumulées dans le formulaire de contact.
Valuer par défaut : 1 048 576 (= 1Mo).
- __thesaurus.contact.reCaptchaPrivateKey__ : Clé privée pour activer reCaptcha (https://www.google.com/recaptcha) dans le formulaire de contact.
- __thesaurus.contact.reCaptchaPublicKey__ : Clé publique pour activer reCaptcha (https://www.google.com/recaptcha) dans le formulaire de contact.

#### Activer le chargement automatique des vocabulaires

Pour activer le chargement automatique des vocabulaires depuis un répertoire, il faut décommenter la partie du fichier "web.xml" correspondant au filter * VocabularyAutoload *.
Ce filter admet plusieurs paramètres de configuration (init-param) :

- __lookupDirectory__ : Obligatoire.
Répertoire à scruter. 
Ce répertoire doit exister et être accessible en écriture à l’application (déplacement des fichiers traités). 
Seuls les fichiers « *.rdf » sont pris en compte, et les sous-répertoires ne sont pas pris en compte.
- __emailTo__ : Adresse email de destination des rapports d'erreurs. Laisser vide pour ne pas envoyer d’email.
- __refresh__ : Intervalle de scrutation du répertoire en millisecondes.
Valeur par défaut : 10 000 (10 secondes).
- __successDirectory__ : Répertoire de destination des vocabulaires chargés avec succès. 
Ce répertoire est créé s’il n’existe pas. Il doit être accessible en écriture.
Les fichiers déplacés sont renommés avec une extension correspondant à un numéro de lot (numéro identique pour tous les fichiers traités lors de la scrutation).
Valeur par défaut : "<lookupDirectory>/success"
- __failureDirectory__ : Répertoire de destination des vocabulaires dont le chargement n’a pas été effectué. 
Ce répertoire est créé s’il n’existe pas. Il doit être accessible en écriture.
Les fichiers déplacés sont renommés avec une extension correspondant à un numéro de lot (numéro identique pour tous les fichiers traités lors de la scrutation) et un fichier de log portant ce même numéro de lot est créé dans le répertoire.
Valeur par défaut : "<lookupDirectory>/failure"

##### Fonctionnement :

Lorsque qu'il est activé, le mécanisme de chargement automatique des vocabulaires scrute, à intervalle régulier (cf. paramètre _refresh_), le répertoire configuré (cf. paramètre _lookupDirectory_).
Les fichiers situés dans ce répertoire (sans tenir compte des sous-répertoires) et dont le nom se termine par ".rdf" (sans tenir compte de la casse) sont alors importés en tant que vocabulaires, de façon séquentielle.

Tant qu'un fichier "lock.txt" est présent dans le répertoire, l'import n'a pas lieu. Cela permet de "verrouiller" le mécanisme, le temps d'y déposer des fichiers par exemple.

Une fois traités, les fichiers sont renommés avec un numéro de traitement (à la fin du nom) et déplacés dans les répertoires de succès ou d'échec (cf. paramètres _successDirectory_ et _failureDirectory_). 
Dans le cas d'un échec, les logs du traitement sont également écrits dans un fichier portant le même numéro de traitement que le fichier dans le répertoire d'échec, et un email contenant ces logs est envoyé (cf. paramètre _emailTo_).

#### Recherches dans les corpus ouverts

Sur la page d'un concept, il est possible d'afficher des liens permettant d'effectuer des recherches sur ce concept dans des corpus ouverts.
Par défaut, l'application est configurée pour ces recherches dans _Wikimedia Commons_ et _Wiktionnaire_.

Le paramétrage de ces liens se fait dans "fr/gouv/culture/thesaurus/service/open-searches_fr.properties", sous la forme de 3 clés à compléter pour chaque lien :
- __search-x.name__ : le nom du corpus ouvert
- __search-x.icon__ : l'URL de l'image à afficher
- __search-x.url__ : l'URL du lien. Cette URL peut (doit!) être paramétrée avec :
	- __{0}__ : le libellé préférentiel du concept
 
_NB_ : le "x" dans le nom des clés doit être remplacé par un chiffre (search-1.name, search-2.name, etc.).

