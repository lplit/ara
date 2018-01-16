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
soit modifier, soit spécifier dans `make` et `make run`. Parce qu'il est
initalisé au Dropbox de Michal. Genre.
- `CFG=<chemin>`: le chemin d'un fichier de configuration, initialisé à
`src/manet/cfg_initial.txt` par défaut.

### `src/bench.pl`

exemple: `./bench.pl <chemin_peersim>`

Le script crée un dossier
`bench_<date>` où seront stockés les résultats pour la question
8. Le dossier contiendra les fichiers de configuration pour les
espériences sous le nom `cfg_bench_<scope>_<SPI>_<SD>`, les résultats
dans un fichier de même nom avec l'extension `.result`. Ces derniers
contiennent les résultats de 10 expériences avec autant de différentes
graines aléatoires.

### `src/bench.py`

exemple: `bench.py <chemin_results>`

Dans le dossier `<chemin_results>` doivent se trouver les fichiers de result crees par `bench.pl`. Utilise pour les resultats de bench, pour remplir les tableaux.

exemple output:

```
/ara/src/results/cfg_bench_500_3_3.result
| 2.3830 +- 0.0684 | 0.4320 +- 0.0426 | 0.2300 +- 0.0335 |


/ara/src/results/cfg_bench_875_3_3.result
| 2.3390 +- 0.1023 | 0.4400 +- 0.0369 | 0.2140 +- 0.0201 |


/ara/src/results/cfg_bench_750_1_1.result
| 2.2740 +- 0.1165 | 0.4630 +- 0.0518 | 0.2320 +- 0.0325 |


/ara/src/results/cfg_bench_375_1_1.result
| 0.7130 +- 0.0447 | 0.6550 +- 0.0457 | 0.1790 +- 0.0230 |

```

`| 2.2740 +- 0.1165 | 0.4630 +- 0.0518 | 0.2320 +- 0.0325 |` se lit colonne1 moyenne `2.2740` et ecart type `0.1165`

### todo (peut-être?)
- bench.pl -> bench1q10.pl
- bench2q1.pl
- 2 ou 3 Makefile différents (Ex1, Ex2)
