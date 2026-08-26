# PSP · Juego de la Oca por sockets

Reimplementación desde cero del proyecto `Oca` conservado únicamente como JAR cliente/servidor en el material de PSP. Se conserva la idea y las casillas especiales observables del programa original, pero el nuevo servidor es autoritativo: **el dado se genera en el servidor** y el cliente no puede enviar un resultado arbitrario.

El tablero tiene 63 casillas (índices 0–62), rebote al exceder meta, ocas/puentes/dados, posada, pozo, laberinto, cárcel y muerte. Las penalizaciones se expresan de forma semántica (2 o 3 turnos completos) en lugar de reproducir los desfases de contador del binario antiguo.

Para hosts remotos el cliente reutiliza la comprobación de conectividad adaptada del Gist MIT: https://gist.github.com/rodrigosambadesaa/6f8e895de21c76b946bf70a1deffe05b; localhost queda deliberadamente exento.

```bash
mvn verify
java -cp target/classes dev.rodrigosambade.oca.GameServer 5000 2
java -cp target/classes dev.rodrigosambade.oca.GameClient localhost 5000 Rodrigo
```
