# De AFND a AFD y Minimizacion

Proyecto educativo con arquitectura en dos capas:

- Backend: Spring Boot (Java 17) para construir y validar automatas.
- Frontend: Flask (Python) para la interfaz web y consumo del backend.

  <img width="1910" height="975" alt="image" src="https://github.com/user-attachments/assets/42be9538-f6ce-45ae-bf41-bf11f8ebec4b" />



- AFD de IOT
<img width="1910" height="975" alt="image" src="https://github.com/user-attachments/assets/a44568b2-9e1e-42f4-b975-66fa95735de2" />

- AFD de IDS
<img width="1910" height="975" alt="image" src="https://github.com/user-attachments/assets/e5feec26-412b-4763-800e-272f88b13203" />

- AFD de E_comerce
<img width="1910" height="975" alt="image" src="https://github.com/user-attachments/assets/bb5a6112-cfbf-43fe-9316-356a22613132" />





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
