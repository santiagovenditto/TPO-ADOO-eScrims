Notas de la última corrección
=============================

Se realizaron cambios para corregir la persistencia y la experiencia al crear scrims:

- Backend (`src/facade/ScrimAPIFacade.java`): se corrigió la serialización JSON del campo `start` y se añadió una representación mínima consistente del scrim al persistir en `data/scrims.ndjson` (campos: id, title, format, region, owner, state, start, date, created, playersPerSide, participants, confirmations, strategy, mode, latency).

- Frontend (`web/app.js`): se mejoró la reconciliación entre la lista local y la lista del servidor para evitar que scrims optimistas desaparezcan; se hicieron y revirtieron pruebas de agrupación de acciones y se mantuvo el comportamiento estable.

Cómo validar localmente
-----------------------

1. Compilar (si es necesario):

   javac -d out $(find src -name "*.java")

2. Iniciar el servidor (usa puerto 9090 por defecto en este repo):

   ./start_server.sh

3. Crear un scrim de prueba (demo):

   curl -X POST http://localhost:9090/api/demo

4. Verificar persistencia en disco:

   tail -n 20 data/scrims.ndjson

5. Verificar la API:

   curl http://localhost:9090/api/scrims

Notas y recomendaciones
----------------------

- `data/scrims.ndjson` es un almacenamiento NDJSON simple. Evitá editar a mano si el servidor está corriendo.
- Si querés que la UI sea aún más robusta, propongo: 1) devolver siempre el objeto creado en la respuesta a POST `/api/scrims`, y 2) añadir una deduplicación simple por `id` en `renderScrims()` .
