# Diagrama de estados

Texto extraído (versión legible):

- Buscando -> (cupo completo) -> LobbyArmado
- LobbyArmado -> (todos confirman) -> Confirmado
- Confirmado -> (fechaHora alcanzada) -> EnJuego
- EnJuego -> (fin) -> Finalizado
- Cualquier estado antes de EnJuego -> (cancelar) -> Cancelado

Mermaid diagram:

```mermaid
stateDiagram-v2
    [*] --> Buscando
    Buscando --> LobbyArmado: cupo completo
    LobbyArmado --> Confirmado: todos confirman
    Confirmado --> EnJuego: fechaHora alcanzada
    EnJuego --> Finalizado: fin

    Buscando --> Cancelado: cancelar
    LobbyArmado --> Cancelado: cancelar
    Confirmado --> Cancelado: cancelar

    Cancelado --> [*]
    Finalizado --> [*]
```

Notas:
- `LobbyArmado` es el estado en que el scrim tiene el cupo completo y espera confirmaciones.
- `Confirmado` debe transicionar a `EnJuego` automáticamente cuando la fecha/hora del scrim es alcanzada.
- `Cancelado` puede originarse desde cualquier estado anterior a `EnJuego`.
