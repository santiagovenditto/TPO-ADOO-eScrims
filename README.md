TPO-ADOO-eScrims — Instrucciones de arranque

Proyecto demo (Java backend + frontend estático) que expone una API y sirve la carpeta `web/` en http://localhost:8080

Requisitos
- JDK 11+ (recomendado JDK 17)
- Clases compiladas en `out/` (este repo incluye clases compiladas en `out/`)

Arrancar el backend (forma rápida)

1. Desde la raíz del proyecto ejecutá el script de arranque:

   ./start_server.sh

El script intentará liberar el puerto 8080 si está ocupado y arrancará `java -cp out Main` en background.

Arrancar manualmente

1. Asegurate de estar en la raíz del proyecto y tener compiladas las clases en `out/`.
2. Ejecuta:

   java -cp out Main

```markdown
# TPO-ADOO-eScrims

Mini-proyecto demo: backend Java + frontend estático que permite crear y gestionar "scrims" (partidas), con persistencia simple en NDJSON y una UI ligera en `web/`.

Resumen rápido
- Servidor: `Main` (Java) sirve la carpeta `web/` y expone endpoints REST en `http://localhost:9090` (o 8080 según tu configuración/local).
- Frontend: `web/index.html`, `web/styles.css`, `web/app.js`.

Requisitos
- JDK 11+ (se recomienda JDK 17)
- Clases compiladas en `out/` (el repo incluye clases compiladas en `out/`, pero podés recompilar si necesitás).

Arrancar
- Rápido (script):

```bash
./start_server.sh
```

- Manual (si ya compilaste):

```bash
java -cp out Main
```

Frontend solo (sin backend):

```bash
python3 -m http.server 8080 --directory web
```

Endpoints principales
- GET /                -> sirve `web/index.html`
- GET /api/scrims      -> lista scrims (JSON)
- POST /api/scrims     -> crea/actualiza scrim (envía objeto JSON)
- POST /api/demo       -> (demo) crea scrims de ejemplo

Archivos de datos
- `data/scrims.ndjson`  (NDJSON: una entrada JSON por línea)
- `data/reports.ndjson`, `data/strikes.ndjson`, `data/users.csv`

Desarrollo y pruebas
- Compilar Java:

```bash
javac -d out $(find src -name "*.java")
```

- Crear scrim demo (desde consola):

```bash
curl -X POST http://localhost:9090/api/demo
```

- Verificar scrims guardados:

```bash
curl http://localhost:9090/api/scrims
tail -n 20 data/scrims.ndjson
```

Cambios recientes y notas importantes
- Se mejoró la persistencia NDJSON en `src/facade/ScrimAPIFacade.java` para generar líneas JSON válidas.
- Frontend: `web/app.js` ahora preserva scrims optimistas y mapea bots a nombres amigables por scrim (ej. "Jugador 01"). También se añadió soporte para mostrar "Ranking" junto al participante.
- Se eliminaron botones propietarios de prueba ("Simular confirmar", "Limpiar bots") de la UI y se añadió una defensa para ocultarlos si algún render cached los reintroduce.

Buenas prácticas y recomendaciones
- Ignorar archivos de runtime (añadir a `.gitignore`):
   - `server.log`, `data/*.ndjson`, `.DS_Store`
- Evitar editar `data/scrims.ndjson` mientras el servidor está corriendo.

Documentación adicional
- Diagramas de estado y documentación adicional están en `docs/state_diagram.md`.
- Notas históricas y cambios previos se archivaron en `docs/ARCHIVE_NOTAS.md`.

Contacto
- Abrí un issue en el repo para cambios mayores o para coordinar integraciones.

``` 
