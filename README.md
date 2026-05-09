# De AFND a AFD y Minimizacion

Proyecto educativo con arquitectura en dos capas:

- Backend: Spring Boot (Java 17) para construir y validar automatas.
- Frontend: Flask (Python) para la interfaz web y consumo del backend.

## Estructura

- `backend/`: API y logica de automatas.
- `frontend/`: interfaz web y vistas.

## Requisitos

- Java 17
- Python 3.10+ (recomendado 3.12)
- Gradle Wrapper (incluido en `backend/`)

## Ejecutar el backend (Spring Boot)

Desde la raiz del proyecto:

```bash
cd backend
./gradlew bootRun
```

Backend disponible en `http://localhost:8080`.

## Ejecutar el frontend (Flask)

En otra terminal, desde la raiz del proyecto:

```bash
cd frontend
python3 -m venv venv
source venv/bin/activate
pip install flask requests
python app.py
```

Frontend disponible en `http://localhost:5000`.

## Flujo de uso

1. Levantar backend en `:8080`.
2. Levantar frontend en `:5000`.
3. Abrir la pagina principal y probar ejemplos (IoT, IDS, E-commerce).

## Rutas principales

- Frontend:
  - `/`
  - `/iot`
  - `/ids`
  - `/ecommerce`
- Backend base:
  - `/api/automatas/{tipo}`
  - `/api/automatas/{tipo}/validar`

## Notas

- No se versionan artefactos generados (`build/`, `bin/`) ni el entorno virtual (`frontend/venv/`).
- Si cambias puertos, actualiza `BACKEND_BASE_URL` en `frontend/app.py`.
