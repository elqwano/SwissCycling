# SwissCycling

SwissCycling est un planificateur d'itinéraires vélo en Suisse.

Le classe main de l'application est /src/ch/epfl/javelo/gui/JaVelo.java

L'interface de JaVelo est similaire à celle de planificateurs en ligne comme Google Maps. JaVelo n'est toutefois pas une application Web — qui s'exécute en partie dans le navigateur et en partie sur un serveur distant —, mais bien une application Java qui s'exécute exclusivement sur l'ordinateur de la personne qui l'utilise.

Comme d'habitude dans ce genre de programmes, il est possible de déplacer, agrandir ou réduire la carte au moyen de la souris.

La planification d'un itinéraire se fait en plaçant au moins deux points de passage — le point de départ et celui d'arrivée — en cliquant sur la carte. Dès que deux de ces points ont été placés, JaVelo détermine l'itinéraire reliant ces deux points qu'il considère comme idéal pour une personne se déplaçant à vélo. Pour cela, il tient compte non seulement du type des routes empruntées — en favorisant les routes peu importantes, pistes cyclables et autres — mais aussi du relief — en évitant les montées raides.

Dès qu'un itinéraire a été calculé, son profil en long est affiché dans le bas de l'interface, accompagné de quelques statistiques : longueur totale, dénivelés positifs et négatifs, etc. Lorsque le pointeur de la souris se trouve sur un point du profil, le point correspondant de l'itinéraire est mis en évidence sur la carte, et inversement.

Finalement, il est possible de modifier un itinéraire existant, en ajoutant, supprimant ou déplaçant des points de passage. Chaque changement provoque le recalcul de l'itinéraire idéal et de son profil en long.

JaVelo est limité au territoire suisse, car il n'existe actuellement pas de modèle altimétrique numérique couvrant la Terre entière qui soit assez précis pour nos besoins et disponible gratuitement. Pour la Suisse, un tel modèle existe puisque l'office fédéral de topographie (swisstopo) offre depuis peu un accès libre à la totalité de ses données, y compris le très précis modèle altimétrique SwissALTI3D, que nous utiliserons.
