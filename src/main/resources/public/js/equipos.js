// equipos.js
// Pantalla "Equipos de rescate":
//  - Crea equipos SIN ubicación inicial
//  - Lista todos los equipos y muestra su ubicación actual (si tienen)

document.addEventListener("DOMContentLoaded", () => {

    const formEquipo     = document.getElementById("formEquipo");
    const inputNombre    = document.getElementById("nombreEquipo");
    const inputTipo      = document.getElementById("tipoEquipo");
    const inputMiembros  = document.getElementById("miembrosEquipo");
    const tablaEquipos   = document.getElementById("tablaEquipos");

    // ------- Listar equipos -------
    async function listarEquipos() {
        try {
            const resp = await fetch("/api/equipos");
            if (!resp.ok) {
                tablaEquipos.innerHTML =
                    '<tr><td colspan="4" class="muted center">No se pudieron cargar los equipos</td></tr>';
                return;
            }

            const data = await resp.json(); // [{nombre,tipo,miembros,ubicacion}, ...]

            if (!data || data.length === 0) {
                tablaEquipos.innerHTML =
                    '<tr><td colspan="4" class="muted center">No hay equipos registrados</td></tr>';
                return;
            }

            tablaEquipos.innerHTML = data.map(e => `
                <tr>
                    <td>${e.nombre}</td>
                    <td>${e.tipo}</td>
                    <td>${e.miembros}</td>
                    <td>${e.ubicacion || "Sin ubicación"}</td>
                </tr>
            `).join("");

        } catch (err) {
            console.error("Error cargando equipos:", err);
            tablaEquipos.innerHTML =
                '<tr><td colspan="4" class="muted center">Error cargando equipos</td></tr>';
        }
    }

    // ------- Crear equipo (sin ubicación) -------
    formEquipo.addEventListener("submit", async (ev) => {
        ev.preventDefault();

        const nombre   = inputNombre.value.trim();
        const tipo     = inputTipo.value.trim();
        const miembros = Number(inputMiembros.value);

        if (!nombre || !tipo || !miembros || miembros <= 0) {
            alert("Por favor completa correctamente los datos del equipo.");
            return;
        }

        const body = {
            nombre: nombre,
            tipo: tipo,
            miembros: miembros
            // OJO: aquí YA NO enviamos ubicación
        };

        try {
            const resp = await fetch("/api/equipos", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(body)
            });

            if (!resp.ok) {
                const txt = await resp.text();
                alert("No se pudo guardar el equipo. Detalle: " + txt);
                return;
            }

            formEquipo.reset();
            listarEquipos();

        } catch (err) {
            console.error("Error creando equipo:", err);
            alert("Error de comunicación con el servidor.");
        }
    });

    // Inicio
    listarEquipos();
});
