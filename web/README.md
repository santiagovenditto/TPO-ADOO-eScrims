# eScrims - Frontend local

Pequeña app que consume la API Java incluida en el repositorio.

Archivos:
- `index.html` - UI principal
- `styles.css` - Estilos
- `app.js` - Lógica frontend que consume la API Java en `:8080`

Cómo usar:

1. Compilar y ejecutar la aplicación Java desde la raíz del repo (usa el WebServer embebido que sirve `web/` y expone la API):

   ```bash
   # desde la raíz del repo
   javac -d out src/*.java src/auth/*.java src/event/*.java src/domain/*.java src/state/*.java src/strategy/*.java src/notification/*.java
   java -cp out Main
   # el servidor web atenderá en http://localhost:8080/
   ```

2. Abrir http://localhost:8080 en el navegador.

Notas:
- Esta implementación expone una API y ejecuta la simulación del `Main` en el servidor; la salida de consola se captura y se muestra en el frontend al ejecutar la acción "Ejecutar simulación (server)".
- Sigue siendo para uso local/educativo. No usar en producción.
