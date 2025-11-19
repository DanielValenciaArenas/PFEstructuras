// distribucion.js
// Maneja la distribución prioritaria de recursos usando el endpoint /api/recursos/distribuirPrioridad

document.addEventListener("DOMContentLoaded", () => {

    const selectUbicacionOrigen = document.getElementById("ubicacionOrigen");
    const selectRecursoOrigen   = document.getElementById("recursoOrigen");
    const inputCantidadZona     = document.getElementById("cantidadPorZona");
    const logDistribucion       = document.getElementById("logDistribucion");

    let recursosGlobal = [];

    function setLog(texto) {
        if (!logDistribucion) return;
        logDistribucion.textContent = texto;
    }

    async function cargarUbicaciones() {
        try {
            const resp = await fetch("/api/ubicaciones");
            if (!resp.ok) {
                setLog("No se pudieron cargar las ubicaciones.");
                return;
            }
            const data = await resp.json(); // [{nombre,...}]

            if (!selectUbicacionOrigen) return;

            selectUbicacionOrigen.innerHTML =
                '<option value="">Ubicación origen (bodega)</option>';

            data.forEach(u => {
                const op = document.createElement("option");
                op.value = u.nombre;
                op.textContent = u.nombre;
                selectUbicacionOrigen.appendChild(op);
            });

        } catch (e) {
            console.error("Error al cargar ubicaciones:", e);
            setLog("Error al cargar ubicaciones.");
        }
    }

    async function cargarRecursos() {
        try {
            const resp = await fetch("/api/recursos");
            if (!resp.ok) {
                setLog("No se pudieron cargar los recursos.");
                return;
            }
            const data = await resp.json(); // [{id, nombre, tipo, cantidad, ubicacion,...}]
            recursosGlobal = Array.isArray(data) ? data : [];

            console.log("Recursos cargados:", recursosGlobal);

        } catch (e) {
            console.error("Error al cargar recursos:", e);
            setLog("Error al cargar recursos.");
        }
    }

    function actualizarRecursosParaOrigen() {
        if (!selectUbicacionOrigen || !selectRecursoOrigen) return;

        const origen = selectUbicacionOrigen.value;
        selectRecursoOrigen.innerHTML =
            '<option value="">Recurso en origen</option>';

        if (!origen) return;

        const recursosEnOrigen = recursosGlobal.filter(r => r.ubicacion === origen);

        if (!recursosEnOrigen.length) {
            const op = document.createElement("option");
            op.value = "";
            op.textContent = "No hay recursos en esta ubicación";
            selectRecursoOrigen.appendChild(op);
            return;
        }

        recursosEnOrigen.forEach(r => {
            const op = document.createElement("option");
            op.value = r.id; // idRecurso interno (RA..., RM...)
            const tipo = r.tipo || "GEN";
            op.textContent = `${r.nombre || "(sin nombre)"} (${tipo}) - cant: ${r.cantidad}`;
            selectRecursoOrigen.appendChild(op);
        });
    }

    if (selectUbicacionOrigen) {
        selectUbicacionOrigen.addEventListener("change", actualizarRecursosParaOrigen);
    }

    const formDistribuir = document.getElementById("formDistribuir");
    if (formDistribuir && selectUbicacionOrigen && selectRecursoOrigen && inputCantidadZona) {
        formDistribuir.addEventListener("submit", async (e) => {
            e.preventDefault();

            const origen    = selectUbicacionOrigen.value;
            const idRecurso = selectRecursoOrigen.value;
            const cantTotal = Number(inputCantidadZona.value);

            if (!origen) {
                alert("Selecciona una ubicación origen (bodega).");
                return;
            }
            if (!idRecurso) {
                alert("Selecciona un recurso en la ubicación origen.");
                return;
            }
            if (!cantTotal || cantTotal <= 0) {
                alert("La cantidad total a distribuir debe ser mayor que 0.");
                return;
            }

            try {
                const resp = await fetch("/api/recursos/distribuirPrioridad", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        origen: origen,
                        idRecurso: idRecurso,
                        cantidadPorZona: cantTotal
                    })
                });

                if (!resp.ok) {
                    const txt = await resp.text();
                    setLog("Error al distribuir recursos: " + txt);
                    return;
                }

                const mensajes = await resp.json(); // ["msg1","msg2",...]
                if (!Array.isArray(mensajes) || mensajes.length === 0) {
                    setLog("No se generaron movimientos de distribución.");
                } else {
                    setLog(mensajes.join("\n"));
                }

                await cargarRecursos();
                actualizarRecursosParaOrigen();

            } catch (err) {
                console.error("Error al distribuir recursos:", err);
                setLog("Error de comunicación con el servidor.");
            }
        });
    }

    (async () => {
        await cargarUbicaciones();
        await cargarRecursos();
        actualizarRecursosParaOrigen();
    })();
});
