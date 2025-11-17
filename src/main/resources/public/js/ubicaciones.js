// ubicaciones.js
// Pantalla de Ubicaciones: solo muestra información.
// No crea ni modifica ubicaciones.

document.addEventListener("DOMContentLoaded", () => {

    const contenedor = document.getElementById("listaUbicaciones");

    let equiposGlobal = [];

    // Cargar todos los equipos una sola vez
    async function cargarEquipos() {
        try {
            const resp = await fetch("/api/equipos");
            if (!resp.ok) {
                console.error("No se pudieron cargar los equipos");
                equiposGlobal = [];
                return;
            }
            equiposGlobal = await resp.json();
        } catch (err) {
            console.error("Error cargando equipos:", err);
            equiposGlobal = [];
        }
    }

    // Cargar recursos de una ubicación
    async function cargarRecursosUbicacion(nombreUbicacion) {
        try {
            const resp = await fetch(`/api/recursos?ubicacion=${encodeURIComponent(nombreUbicacion)}`);
            if (!resp.ok) {
                return [];
            }
            return await resp.json(); // [{tipo, cantidad}, ...]
        } catch (err) {
            console.error("Error cargando recursos de", nombreUbicacion, err);
            return [];
        }
    }

    // Cargar personas de una ubicación
    async function cargarPersonasUbicacion(nombreUbicacion) {
        try {
            const resp = await fetch(`/api/personas?ubicacion=${encodeURIComponent(nombreUbicacion)}`);
            if (!resp.ok) {
                return [];
            }
            return await resp.json(); // [{nombre}, ...]
        } catch (err) {
            console.error("Error cargando personas de", nombreUbicacion, err);
            return [];
        }
    }

    // Crear tarjeta HTML para una ubicación
    function crearTarjetaUbicacion(ubicacion, recursos, personas, equipos) {
        const div = document.createElement("div");
        div.className = "card";

        const colorNivel = (nivel) => {
            switch (nivel) {
                case "LEVE": return "green";
                case "MODERADO": return "orange";
                case "GRAVE": return "red";
                default: return "gray";
            }
        };

        const color = colorNivel(ubicacion.nivelAfectacion);

        // Recursos: texto simple
        let textoRecursos;
        if (!recursos || recursos.length === 0) {
            textoRecursos = "<em>No hay recursos registrados en esta ubicación.</em>";
        } else {
            textoRecursos = "<ul>";
            recursos.forEach(r => {
                textoRecursos += `<li>${r.tipo}: ${r.cantidad}</li>`;
            });
            textoRecursos += "</ul>";
        }

        // Equipos
        let textoEquipos;
        if (!equipos || equipos.length === 0) {
            textoEquipos = "<em>No hay equipos de rescate en esta ubicación.</em>";
        } else {
            textoEquipos = "<ul>";
            equipos.forEach(e => {
                textoEquipos += `<li>${e.nombre} (${e.tipo}) — ${e.miembros} miembros</li>`;
            });
            textoEquipos += "</ul>";
        }

        // Personas (código original: lista de nombres)
        let textoPersonas;
        if (!personas || personas.length === 0) {
            textoPersonas = "<em>No hay personas registradas en esta ubicación.</em>";
        } else {
            textoPersonas = "<ul>";
            personas.forEach(p => {
                textoPersonas += `<li>${p.nombre}</li>`;
            });
            textoPersonas += "</ul>";
        }

        // 🔵 NUEVO: mostrar SOLO el número de personas (contador),
        // dejando el código anterior intacto pero sobrescribiendo el texto final.
        const numPersonas = (personas && personas.length) ? personas.length : 0;
        if (numPersonas === 0) {
            textoPersonas = "<em>No hay personas registradas en esta ubicación.</em>";
        } else {
            textoPersonas = `<strong>${numPersonas}</strong> persona(s) registrada(s) en esta ubicación.`;
        }

        div.innerHTML = `
            <h3>${ubicacion.nombre}</h3>
            <p>
                Tipo de zona: <strong>${ubicacion.tipoZona}</strong><br>
                Nivel de afectación:
                <strong style="color:${color}">${ubicacion.nivelAfectacion}</strong><br>
                Coordenadas:
                <code>(${ubicacion.latitud.toFixed(4)}, ${ubicacion.longitud.toFixed(4)})</code>
            </p>

            <h4>Recursos</h4>
            ${textoRecursos}

            <h4>Equipos de rescate</h4>
            ${textoEquipos}

            <h4>Personas</h4>
            ${textoPersonas}

            <div class="mt">
                <button class="btn btn-danger" onclick="eliminarUbicacion('${ubicacion.nombre}')">
                    Eliminar ubicación
                </button>
            </div>
        `;

        return div;
    }

    // FUNCIÓN PARA ELIMINAR UBICACIÓN
    window.eliminarUbicacion = async function (nombreUbicacion) {
        if (!confirm(`¿Seguro que deseas eliminar la ubicación "${nombreUbicacion}"?`)) {
            return;
        }

        try {
            // El WebServer espera el nombre en la QUERY, no en el body
            const resp = await fetch(`/api/ubicaciones?nombre=${encodeURIComponent(nombreUbicacion)}`, {
                method: "DELETE"
            });

            if (resp.ok) {
                alert("Ubicación eliminada correctamente.");
                cargarUbicaciones(); // recargar la lista
            } else {
                const txt = await resp.text();
                alert("No se pudo eliminar la ubicación. Detalle: " + txt);
            }
        } catch (err) {
            console.error("Error eliminando ubicación:", err);
            alert("Error de comunicación con el servidor.");
        }
    };

    // Cargar todo y pintar
    async function cargarUbicaciones() {
        contenedor.innerHTML = "Cargando ubicaciones...";

        try {
            // Primero cargamos equipos (para no repetir peticiones)
            await cargarEquipos();

            // Luego cargamos ubicaciones
            const resp = await fetch("/api/ubicaciones");
            if (!resp.ok) {
                contenedor.innerHTML = "No se pudieron cargar las ubicaciones.";
                return;
            }

            const ubicaciones = await resp.json(); // [{nombre, tipoZona, nivelAfectacion, latitud, longitud}]

            if (ubicaciones.length === 0) {
                contenedor.innerHTML = "No hay ubicaciones registradas. Debes crearlas en el Mapa Interactivo.";
                return;
            }

            contenedor.innerHTML = "";

            // Para cada ubicación pedimos recursos y personas
            for (const u of ubicaciones) {
                const [recursos, personas] = await Promise.all([
                    cargarRecursosUbicacion(u.nombre),
                    cargarPersonasUbicacion(u.nombre)
                ]);

                // Usamos los equiposGlobal y filtramos por ubicación
                const equiposUbic = equiposGlobal.filter(e => e.ubicacion === u.nombre);

                const tarjeta = crearTarjetaUbicacion(u, recursos, personas, equiposUbic);
                contenedor.appendChild(tarjeta);
            }

        } catch (err) {
            console.error(err);
            contenedor.innerHTML = "Ocurrió un error al cargar la información de ubicaciones.";
        }
    }

    // Iniciar
    cargarUbicaciones();
});
