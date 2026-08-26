# Material de partida — Juego de la Oca por sockets

Fuente: proyecto `Oca.zip` conservado en Google Drive dentro del tema de sockets de PSP.

El material original implementa una versión en red del **Juego de la Oca**, con cliente y servidor y un tablero de 63 casillas con reglas especiales.

El objetivo de la recuperación es mantener el juego multijugador cliente/servidor, el avance por tiradas, la llegada exacta con rebote cuando procede y las casillas especiales observables en el proyecto: ocas, puentes, dados, posada, pozo, laberinto, cárcel y muerte.

La reimplementación actual hace que el servidor sea autoritativo y gestione las tiradas y el estado de la partida para evitar que un cliente pueda alterar el resultado.

No existe una base de datos asociada.

> El material de Drive se conserva como proyecto compilado/comprimido y no incluye un PDF separado de enunciado; por ello este archivo documenta el comportamiento verificable del ejercicio original.
