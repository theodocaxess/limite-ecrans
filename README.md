# Limite ecrans - Solal

Application Android qui affiche une notification "Attention les ecrans devant Solal, on LIMITE AU MAX !" a chaque deverrouillage du telephone, si l'instant present tombe dans un creneau coche dans la grille.

## Creneaux par defaut (modifiables dans l'app)

1. Matin en semaine (lundi a vendredi) : 7h30 - 8h00
2. Soir, tous les jours : 18h00 - 20h00
3. Mercredi apres-midi : 13h00 - 20h00
4. Week-end (samedi et dimanche) : 7h30 - 20h00

Chaque case de la grille (jour x creneau) peut etre activee ou desactivee independamment. Les reglages sont sauvegardes sur le telephone.

## Fonctionnement

1. Ouvre l'app et appuie sur "Activer la surveillance" (une fois). L'app demande l'autorisation d'envoyer des notifications : accepte-la.
2. Un service tourne alors en continu et garde une icone de notification permanente et discrete ("Limite ecrans : surveillance active"). C'est une obligation d'Android pour ce type de fonctionnement, impossible a masquer.
3. A chaque deverrouillage du telephone, l'app verifie l'heure et le jour : si ca tombe dans un creneau coche, la notification de rappel s'affiche immediatement au premier plan.
4. Le service redemarre automatiquement si le telephone est eteint puis rallume, tant que la surveillance n'a pas ete desactivee depuis l'app.

Pour arreter completement, rouvre l'app et appuie sur "Desactiver la surveillance".

## Pourquoi ce n'est pas une simple appli web (PWA), et pourquoi Expo Go ne suffit pas

Detecter le deverrouillage du telephone (meme appli fermee) demande un service Android natif qui reste actif en arriere-plan et un composant qui ecoute l'evenement systeme de deverrouillage. Une page web (PWA) ne peut pas faire ca de facon fiable sur Android. Expo Go ne le peut pas non plus : c'est une application generique qui n'embarque que les modules standards d'Expo, elle ne peut pas executer le code natif specifique ecrit pour cette appli (le service et la reception de l'evenement de deverrouillage). Il faut donc un vrai projet Android compile avec ce code natif inclus, ce qui est fait ici.

## Fiabilite sur la duree

Sur certains telephones (Xiaomi, Huawei, Samsung avec economie de batterie agressive...), le systeme peut malgre tout arreter le service au bout d'un moment pour economiser la batterie. Pour la fiabilite maximale : dans les reglages Android de l'app, desactive "Optimisation de la batterie" / "Mise en veille automatique" pour cette appli, et si ton telephone a une liste d'applications "protegees" ou en "demarrage automatique", ajoute l'appli dedans.

## Comment obtenir le fichier .apk installable

Cet environnement ne peut pas compiler l'APK lui-meme : la compilation Android a besoin des depots Google/Maven, qui ne sont pas accessibles depuis ce bac a sable. Il faut donc finaliser la compilation depuis ton propre ordinateur, avec Android Studio (gratuit). C'est une etape unique d'environ 15 minutes.

1. Installe Android Studio si ce n'est pas deja fait : https://developer.android.com/studio
2. Decompresse ce zip, puis dans Android Studio choisis "Open" et selectionne le dossier "android" (pas le dossier racine).
3. Laisse Android Studio synchroniser Gradle (premiere fois : telechargement automatique du SDK et des dependances, ca peut prendre quelques minutes).
4. Une fois la synchronisation terminee, va dans le menu Build puis "Build Bundle(s) / APK(s)" puis "Build APK(s)".
5. Le fichier .apk se trouve ensuite dans android/app/build/outputs/apk/debug/app-debug.apk. Transfere-le sur ton telephone (mail, drive, cable USB...).
6. Sur le telephone, autorise l'installation d'applications hors Play Store pour la source utilisee, puis ouvre le fichier .apk pour installer l'app.
7. Ouvre l'app et appuie sur "Activer la surveillance" : accepte l'autorisation de notifications quand elle est demandee.

## Modifier les creneaux ou le texte du message

Deux fichiers doivent rester coherents entre eux :

1. android/app/src/main/java/com/theo/limitesolal/PeriodSchedule.java : la logique reelle qui decide si l'instant present declenche un rappel (tableau PERIODS, heures de debut et de fin, jours concernes).
2. www/app.js et www/index.html : l'affichage de la grille que l'utilisateur coche/decoche, et le texte du message dans ScreenLimitService.java.

Apres une modification de www/, lance `npx cap sync android` depuis la racine du projet pour repercuter les changements dans le projet Android, puis reconstruis l'APK depuis Android Studio. Une modification des fichiers .java demande juste de relancer Build > Build APK(s) dans Android Studio.
