Projet ARA 2017-2018
--------------------
### `src/Makefile`

Directives de `make`

- `make` compile le projet
- `make run` lance une instance de simulation telle que spécifiée dans
`src/manet/cfg_initial.txt`
- `make clean` nettoie le projet des compilés
- `make bench_clean` nettoie le dossier `src/` des benchmarks

Le Makefile admet deux variables:
- `DIR_PEERSIM=<chemin>`: le dossier d'installation de Peersim, qu'il faudra
soit modifier, soit spécifier dans `make` et `make run`.
- `CFG=<chemin>`: le chemin d'un fichier de configuration, initialisé à
`src/manet/cfg_initial.txt` par défaut.
