"""
Frontend Flask — Arquitectura MVC
  Modelo:      Peticiones HTTP al backend Spring Boot
  Vista:       Templates Jinja2
  Controlador: Rutas Flask (este archivo)
"""

from flask import Flask, render_template, request, jsonify
import requests

app = Flask(__name__)

BACKEND_BASE_URL = "http://localhost:8080/api/automatas"

EXAMPLES = {
    "iot": {
        "id": "iot",
        "nombre": "IoT Telemetria",
        "subtitulo": "Tramas con cabecera HDR y cierre CRC",
        "lenguaje": "HDR (H | T)* CRC",
        "placeholder": "HDR, T, H, CRC",
        "simbolos": ["HDR", "H", "T", "CRC"],
    },
    "ids": {
        "id": "ids",
        "nombre": "IDS Eventos",
        "subtitulo": "Secuencia valida de deteccion y cierre",
        "lenguaje": "S A R",
        "placeholder": "S, A, R",
        "simbolos": ["S", "A", "R"],
    },
    "ecommerce": {
        "id": "ecommerce",
        "nombre": "E-commerce Flujo",
        "subtitulo": "Flujo de pedido con inicio y confirmacion",
        "lenguaje": "H S C",
        "placeholder": "H, S, C",
        "simbolos": ["H", "S", "C"],
    },
}


# ── Controlador ──────────────────────────────────────────────────────────────

def obtener_data_automatas(ejemplo_id):
    """Obtiene los tres automatas desde el backend Spring segun el tipo de ejemplo."""
    try:
        response = requests.get(f"{BACKEND_BASE_URL}/{ejemplo_id}", timeout=5)
        return response.json()
    except Exception as e:
        return {"error": str(e)}


@app.route("/")
def index():
    """Portada: explica el proceso y enlaza los tres ejemplos."""
    return render_template("index.html", ejemplos=list(EXAMPLES.values()))


def render_ejemplo(ejemplo_id):
    """Renderiza la vista interactiva para un ejemplo concreto."""
    ejemplo = EXAMPLES[ejemplo_id]
    data = obtener_data_automatas(ejemplo_id)
    return render_template("IOT.html", data=data, ejemplo=ejemplo)


@app.route("/iot")
def iot():
    return render_ejemplo("iot")


@app.route("/ids")
def ids():
    return render_ejemplo("ids")


@app.route("/ecommerce")
def ecommerce():
    return render_ejemplo("ecommerce")


@app.route("/e-comerce")
def ecommerce_legacy():
    """Compatibilidad con ruta antigua escrita con guion."""
    return render_ejemplo("ecommerce")


@app.route("/validar", methods=["POST"])
def validar_legacy():
    """Compatibilidad: valida usando IoT por defecto."""
    return validar_por_tipo("iot")


@app.route("/validar/<ejemplo_id>", methods=["POST"])
def validar(ejemplo_id):
    """Recibe una cadena del formulario, la envía al backend por tipo y devuelve JSON."""
    return validar_por_tipo(ejemplo_id)


def validar_por_tipo(ejemplo_id):
    cadena_raw = request.form.get("cadena", "")
    # Parsear la cadena separada por comas: "HDR,T,H,CRC" → ["HDR","T","H","CRC"]
    cadena = [s.strip() for s in cadena_raw.split(",") if s.strip()]

    try:
        response = requests.post(
            f"{BACKEND_BASE_URL}/{ejemplo_id}/validar",
            json={"cadena": cadena},
            timeout=5
        )
        resultado = response.json()
    except Exception as e:
        resultado = {"error": str(e)}

    return jsonify(resultado)


if __name__ == "__main__":
    app.run(debug=True, port=5000)