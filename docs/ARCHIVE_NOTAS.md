```markdown
ARCHIVO: Notas y documentación archivada

Este archivo contiene las notas antiguas que fueron consolidadas en el `README.md` raíz.

-- NOTAS.md (resumen antiguo)

Se realizaron cambios para corregir la persistencia y la experiencia al crear scrims:

- Backend (`src/facade/ScrimAPIFacade.java`): se corrigió la serialización JSON del campo `start` y se añadió una representación mínima consistente del scrim al persistir en `data/scrims.ndjson`.

- Frontend (`web/app.js`): se mejoró la reconciliación entre la lista local y la lista del servidor para evitar que scrims optimistas desaparezcan.

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

-- web/README.md (resumen antiguo)

Frontend: archivos principales: `index.html`, `styles.css`, `app.js`.

Uso básico:

1. Compilar y ejecutar la app Java desde la raíz del repo (WebServer embebido que sirve `web/`):

   javac -d out src/*.java src/auth/*.java src/event/*.java src/domain/*.java src/state/*.java src/strategy/*.java src/notification/*.java
   java -cp out Main

2. Abrir http://localhost:8080 en el navegador.

``` 
