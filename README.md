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

Arrancar solo frontend

Si no querés ejecutar el backend, podés servir `web/` con cualquier servidor estático, por ejemplo (Python 3):

   python3 -m http.server 8080 --directory web

Endpoints útiles
- GET / -> sirve `web/index.html`
- POST /api/register -> registro (JSON: {username,email,password})
- POST /api/login -> login (JSON: {email,password})
- GET /api/session?token=... -> valida token
- POST /api/run -> ejecuta la simulación (necesita token)
- GET /api/scrims -> lista scrims
- POST /api/scrims -> guarda un scrim (envía objeto JSON)

Archivos de datos
- `data/users.csv`, `data/scrims.ndjson`, `data/reports.ndjson`, `data/strikes.ndjson`.

Notas
- El servidor está implementado con `com.sun.net.httpserver.HttpServer` y sirve la carpeta `web/` desde el working directory.
- Si necesitás compilar código Java, podés usar:

   javac -d out $(find src -name "*.java")

State diagram
See `docs/state_diagram.md` for the scrim state diagram (text + mermaid).

Contacto
Para cambios mayores o preguntas, abrí un issue en el repo.
