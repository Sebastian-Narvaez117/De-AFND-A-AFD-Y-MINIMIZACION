/**
 * diagramas.js
 * Lógica del frontend:
 *  - Dibujo de diagramas de estados con D3.js
 *  - Validación de cadenas contra los tres autómatas (fetch al backend vía Flask)
 */

// ── Colores por tipo de autómata ─────────────────────────────────────────────
const COLORES = {
  afnd:   '#a855f7',
  afd:    '#a855f7',
  afdMin: '#a855f7'
};

// ── Dibujar diagrama de estados con D3 ───────────────────────────────────────

/**
 * Dibuja el diagrama de estados de un autómata dentro del contenedor indicado.
 * @param {string} tipo      - Clave del autómata: 'afnd', 'afd' o 'afdMin'
 * @param {object} automata  - Objeto con estados, transiciones y estadosAceptacion
 */
function dibujarDiagrama(tipo, automata) {
  const contenedor = document.getElementById('diagrama-' + tipo);
  if (!contenedor || !automata) return;

  const W = Math.max(contenedor.offsetWidth || 340, 400);
  const H = tipo === 'afdMin' ? 300 : 260;
  const R = 28;

  // Calcular posición de cada estado en fila horizontal
  const estados  = automata.estados;
  const n        = estados.length;
  const espacioX = Math.min((W - 100) / Math.max(n - 1, 1), 140);
  const startX   = Math.max(30, (W - espacioX * (n - 1)) / 2);

  const posicion = {};
  estados.forEach((estado, i) => {
    posicion[estado] = { x: startX + i * espacioX, y: H / 2 };
  });

  // Agrupar símbolos de transiciones paralelas en una sola arista
  const aristasMap = {};
  for (const [clave, destinos] of Object.entries(automata.transiciones || {})) {
    const partes  = clave.split('|');
    const origen  = partes[0];
    const simbolo = partes[1];
    destinos.forEach(destino => {
      const key = origen + '→' + destino;
      if (!aristasMap[key]) aristasMap[key] = { origen, destino, simbolos: [] };
      aristasMap[key].simbolos.push(simbolo);
    });
  }
  const aristas = Object.values(aristasMap);

  // Crear SVG
  const svg = d3.select(contenedor).append('svg')
    .attr('width', W)
    .attr('height', H);

  // Definir marcador de flecha
  svg.append('defs').append('marker')
    .attr('id',          'arrow-' + tipo)
    .attr('viewBox',     '0 0 10 10')
    .attr('refX',        8)
    .attr('refY',        5)
    .attr('markerWidth', 6)
    .attr('markerHeight',6)
    .attr('orient',      'auto-start-reverse')
    .append('path')
      .attr('d',              'M2 1L8 5L2 9')
      .attr('fill',           'none')
      .attr('stroke',         '#a855f7')
      .attr('stroke-width',   1.5)
      .attr('stroke-linecap', 'round');

  // Flecha de entrada al estado inicial
  const ini = posicion[automata.estadoInicial];
  svg.append('line')
    .attr('x1', ini.x - R - 22)
    .attr('y1', ini.y)
    .attr('x2', ini.x - R - 2)
    .attr('y2', ini.y)
    .attr('stroke',      '#fbbf24')
    .attr('stroke-width', 2)
    .attr('marker-end',  'url(#arrow-' + tipo + ')');

  // Dibujar aristas
  aristas.forEach(({ origen, destino, simbolos }) => {
    const o  = posicion[origen];
    const d_ = posicion[destino];
    if (!o || !d_) return;

    const etiqueta = simbolos.join(', ');
    const clipId = tipo + '-clip-' + origen + '-' + destino;

    if (origen === destino) {
      // Auto-lazo
      const cx = o.x;
      const cy = o.y - R - 20;
      
      svg.append('path')
        .attr('class', 'link-path')
        .attr('data-from', origen)
        .attr('data-to', destino)
        .attr('d', `M${cx - 12},${o.y - R} Q${cx - 30},${cy - 20} ${cx + 12},${o.y - R}`)
        .attr('fill',         'none')
        .attr('stroke',       '#a855f7')
        .attr('stroke-width', 1.5)
        .attr('marker-end',   'url(#arrow-' + tipo + ')');

      svg.append('text')
        .attr('class',        'link-label')
        .attr('data-from', origen)
        .attr('data-to', destino)
        .attr('x',            cx)
        .attr('y',            cy - 14)
        .attr('text-anchor',  'middle')
        .attr('fill',         '#a855f7')
        .text(etiqueta);

    } else {
      // Arista entre dos estados distintos
      const dx   = d_.x - o.x;
      const dy   = d_.y - o.y;
      const dist = Math.sqrt(dx * dx + dy * dy);
      const ux   = dx / dist;
      const uy   = dy / dist;
      const x1   = o.x  + ux * R;
      const y1   = o.y  + uy * R;
      const x2   = d_.x - ux * R;
      const y2   = d_.y - uy * R;

      svg.append('line')
        .attr('class', 'link-path')
        .attr('data-from', origen)
        .attr('data-to', destino)
        .attr('x1',          x1)
        .attr('y1',          y1)
        .attr('x2',          x2)
        .attr('y2',          y2)
        .attr('stroke',      '#a855f7')
        .attr('stroke-width', 1.5)
        .attr('marker-end',  'url(#arrow-' + tipo + ')');

      svg.append('text')
        .attr('class',       'link-label')
        .attr('data-from', origen)
        .attr('data-to', destino)
        .attr('x',           (x1 + x2) / 2)
        .attr('y',           (y1 + y2) / 2 - 6)
        .attr('text-anchor', 'middle')
        .attr('fill',        '#a855f7')
        .text(etiqueta);
    }
  });

  // Dibujar nodos
  estados.forEach(estado => {
    const { x, y } = posicion[estado];
    const esAceptacion = (automata.estadosAceptacion || []).includes(estado);
    const color        = COLORES[tipo] || '#a855f7';

    const g = svg.append('g').attr('class', 'node');

    // Círculo principal
    g.append('circle')
      .attr('cx',     x)
      .attr('cy',     y)
      .attr('r',      R)
      .attr('fill',   color)
      .attr('stroke', esAceptacion ? '#fbbf24' : '#fbbf24')
      .attr('stroke-width', esAceptacion ? 2.5 : 1.5);

    // Círculo interior para estado de aceptación
    if (esAceptacion) {
      g.append('circle')
        .attr('cx',           x)
        .attr('cy',           y)
        .attr('r',            R - 5)
        .attr('fill',         'none')
        .attr('stroke',       '#fbbf24')
        .attr('stroke-width', 2);
    }

    // Etiqueta del estado
    g.append('text')
      .attr('x',                  x)
      .attr('y',                  y + 1)
      .attr('text-anchor',        'middle')
      .attr('dominant-baseline',  'central')
      .attr('fill',               '#1a1a1a')
      .attr('font-weight',        '900')
      .attr('font-size',          estado.length > 5 ? '7px' : '10px')
      .text(estado);
  });
}

// ── Configuración de animación ──────────────────────────────────────────────
let animacionActiva = null;
const VELOCIDAD_ANIMACION = 800; // ms por transición

// ── Inicializar diagramas al cargar la página ────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  if (typeof DATA === 'undefined') return;
  if (DATA.afnd)          dibujarDiagrama('afnd',   DATA.afnd);
  if (DATA.afd)           dibujarDiagrama('afd',    DATA.afd);
  if (DATA.afdMinimizado) dibujarDiagrama('afdMin', DATA.afdMinimizado);
});

// ── Validador de cadenas ──────────────────────────────────────────────────────

/**
 * Lee la cadena del input, la envía al backend vía Flask y muestra el resultado.
 */
async function validarCadena() {
  const input = document.getElementById('cadena-input').value.trim();
  if (!input) return;

  const ejemploId = (typeof EJEMPLO_ID !== 'undefined' && EJEMPLO_ID) ? EJEMPLO_ID : 'iot';

  try {
    const res = await fetch('/validar/' + encodeURIComponent(ejemploId), {
      method:  'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body:    'cadena=' + encodeURIComponent(input)
    });
    const data = await res.json();

    if (data.error) {
      alert('Error al validar: ' + data.error);
      return;
    }

    mostrarResultado(data);

  } catch (err) {
    alert('No se pudo conectar con el servidor: ' + err.message);
  }
}

/**
 * Renderiza los badges de resultado (acepta / rechaza) para los tres autómatas.
 * Inicia la animación de las transiciones en cada diagrama.
 * @param {object} data - Respuesta del backend con campos afnd, afd, afdMin
 */
function mostrarResultado(data) {
  const cadena = data.cadena || [];

  document.getElementById('resultado').style.display = 'block';
  document.getElementById('resultado-cadena').textContent =
    'Cadena: [ ' + cadena.join(', ') + ' ]';

  const grid = document.getElementById('result-grid');
  grid.innerHTML = '';

  const resultados = [
    { label: 'AFND',      valor: data.afnd, tipo: 'afnd' },
    { label: 'AFD',       valor: data.afd,  tipo: 'afd' },
    { label: 'AFD Mín.', valor: data.afdMin, tipo: 'afdMin' }
  ];

  resultados.forEach(({ label, valor, tipo }) => {
    const div = document.createElement('div');
    div.className = 'result-badge ' + (valor ? 'acepta' : 'rechaza');
    div.innerHTML = `
      <span class="label">${label}</span>
      <span class="value">${valor ? '✓ Acepta' : '✗ Rechaza'}</span>
    `;
    grid.appendChild(div);
  });

  // Detener animación anterior si existe
  if (animacionActiva) {
    clearTimeout(animacionActiva);
    limpiarDiagramas();
  }

  // Iniciar animaciones en los diagramas
  if (data.pasos) {
    animarTransiciones(data.pasos, cadena);
  }
}

/**
 * Limpia todos los diagramas de clases de animación
 */
function limpiarDiagramas() {
  document.querySelectorAll('.node').forEach(node => {
    node.classList.remove('active');
  });
  document.querySelectorAll('.link-path').forEach(path => {
    path.classList.remove('active');
  });
  document.querySelectorAll('.link-label').forEach(label => {
    label.classList.remove('active');
  });
  document.querySelectorAll('circle').forEach(circle => {
    circle.classList.remove('state-rejected');
  });
}

/**
 * Anima las transiciones paso a paso
 * @param {object} pasos - Objeto con las secuencias de pasos {afnd: [...], afd: [...], afdMin: [...]}
 * @param {array} cadena - La cadena validada
 */
function animarTransiciones(pasos, cadena) {
  const tipos = ['afnd', 'afd', 'afdMin'];
  const maxPasos = Math.max(
    (pasos.afnd || []).length,
    (pasos.afd || []).length,
    (pasos.afdMin || []).length
  );

  let pasoActual = 0;

  function animarPaso() {
    if (pasoActual >= maxPasos) {
      // Termina la animación - dejar resaltado
      return;
    }

    limpiarDiagramas();

    tipos.forEach(tipo => {
      const pasosDelTipo = pasos[tipo] || [];
      if (pasoActual < pasosDelTipo.length) {
        const paso = pasosDelTipo[pasoActual];
        resaltarEstadoYTransicion(tipo, paso);
      }
    });

    pasoActual++;
    animacionActiva = setTimeout(animarPaso, VELOCIDAD_ANIMACION);
  }

  animarPaso();
}

/**
 * Resalta un estado y la transición en un diagrama
 * @param {string} tipo - 'afnd', 'afd' o 'afdMin'
 * @param {object} paso - {estado_actual, transicion, proximo_estado} o {estado: ..., es_aceptacion: ...}
 */
function resaltarEstadoYTransicion(tipo, paso) {
  const diagramaEl = document.getElementById('diagrama-' + tipo);
  if (!diagramaEl) return;

  const svg = diagramaEl.querySelector('svg');
  if (!svg) return;

  // Resaltar estado actual
  if (paso.estado_actual || paso.estado) {
    const estado = paso.estado_actual || paso.estado;
    const nodeCircles = svg.querySelectorAll('.node circle:first-child');
    
    nodeCircles.forEach(circle => {
      const text = circle.parentElement.querySelector('text');
      if (text && text.textContent === estado) {
        circle.parentElement.classList.add('active');
      }
    });
  }

  // Resaltar transición si existe
  if (paso.proximo_estado && paso.estado_actual) {
    const linkPaths = svg.querySelectorAll('.link-path');
    const linkLabels = svg.querySelectorAll('.link-label');

    linkPaths.forEach(path => {
      if (path.getAttribute('data-from') === paso.estado_actual &&
          path.getAttribute('data-to') === paso.proximo_estado) {
        path.classList.add('active');
      }
    });

    linkLabels.forEach(label => {
      if (label.getAttribute('data-from') === paso.estado_actual &&
          label.getAttribute('data-to') === paso.proximo_estado) {
        label.classList.add('active');
      }
    });
  }

  // Cambiar opacidad de estados de rechazo si no fue aceptado
  if (paso.es_aceptacion === false) {
    const nodeCircles = svg.querySelectorAll('.node circle:first-child');
    nodeCircles.forEach(circle => {
      const parent = circle.parentElement;
      const text = parent.querySelector('text');
      if (text && text.textContent !== paso.estado) {
        circle.classList.add('state-rejected');
      }
    });
  }
}