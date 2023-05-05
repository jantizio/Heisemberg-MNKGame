# Heisenberg - MNKGame player

## Breve descrizione del progetto

Ci sono 3 versioni del giocatore Heisemberg:

- `player/NostroPlayer.java`: metodo del "cutoff"
- `player/NostroPlayer2.java`: realizzato tramite sorting
- `player/NostroPlayer3.java`: fusione dei due player precedenti e giocatore "migliore"

È spiegato tutto più nel dettaglio nella relazione `Relazione_MNKGame.pdf`

Nella cartella player ci sono altri giocatori che sono l'evoluzione del nostro progetto, sono stati eliminati nel branch consegna.

## Istruzioni per la compilazione

Per compilare tutto il progetto

```
$ make full-build
```

Per compilare solo la cartella `player`

```
$ make build
```

Variabili del Makefile che sono importanti.

- `PLAYER_CLASS`: player da testare
- `OPPONENT_CLASS`: player avversario
- `MNK`: dimensione della board, default è "3 3 3"
- `rep`: numero di partite, defaul è 10

sono sempre modificabili così:

```
$ make vshuma MNK="4 4 3" PLAYER_CLASS="dummyPlayer.java"
```

Per giocare umano contro player nella board 3x3x3

```
$ make vshuman MNK="4 4 3"
```

Per far giocare 2 player e `PLAYER_CLASS` gioca per primo

- non verbose:

```
$ make test1move MNK="4 4 3" rep="5"
```

- verbose

```
$ make test1moveV MNK="4 4 3" rep="5"
```

Per far giocare 2 player e `PLAYER_CLASS` gioca per secondo

- non verbose:

```
$ make test2move MNK="4 4 3" rep="5"
```

- verbose

```
$ make test2moveV MNK="4 4 3" rep="5"
```

Per testare in tutte le configurazione, il codice è nel file `player/Tester.java`

```
$ make complete-test
```
