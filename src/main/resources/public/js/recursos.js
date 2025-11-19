// recursos.js
// Pantalla "Recursos y Equipos"
// Maneja:
//  - Crear recursos
//  - Crear equipos de rescate
//  - Listar recursos (incluyendo vencimiento / tipo medicamento)
//  - Listar equipos
//  - Cargar ubicaciones reales en los selects (coherencia con otras interfaces)

document.addEventListener("DOMContentLoaded", () => {

    // Form recurso
    const formularioRecurso  = document.getElementById("fr")           || document.getElementById("formRecurso");
    const selectTipo         = document.getElementById("tipo")         || document.getElementById("tipoRecurso");
    const inputCantidad      = document.getElementById("cant")         || document.getElementById("cantidadRecurso");
    const selectUbicacionRec = document.getElementById("ubic")         || document.getElementById("ubicacionRecurso");

    // Form equipo
    const formularioEquipo   = document.getElementById("fe")           || null;
    const inputNombreEquipo  = document.getElementById("nom")          || null;
    const inputTipoEquipo    = document.getElementById("tip")          || null;
    const inputMiembros      = document.getElementById("miem")         || null;
    const selectUbicacionEq  = document.getElementById("ubi")          || null;

    // Tablas (tbody)
    const tablaRecursosBody  = document.getElementById("tb")           || document.getElementById("tablaRecursos");
    const tablaEquiposBody   = document.getElementById("te")           || document.getElementById("tablaEquipos");
    const divOk              = document.getElementById("ok")           || document.getElementById("msgOk");

    const inputNombreRecurso    = document.getElementById("nombreRecurso");
    const inputFechaVencimiento = document.getElementById("fechaVencimiento");
    const inputTipoMedicamento  = document.getElementById("tipoMedicamento");

    const formularioAsignarEquipo = document.getElementById("formAsignarEquipo"); // <form ...>
    const selectEquipoExistente   = document.getElementById("equipoExistente");   // <select ...>
    const selectUbicacionAsignar  = document.getElementById("ubicacionEquipo");   // <select ...>

    function mostrarOK() {
        if (!divOk) return;
        divOk.style.display = "block";
        setTimeout(() => {
            divOk.style.display = "none";
        }, 1500);
    }

    function actualizarCamposTipo() {
        if (!selectTipo || !inputFechaVencimiento || !inputTipoMedicamento) return;

        if (selectTipo.value === "ALIMENTO") {
            inputFechaVencimiento.style.display = "inline-block";
            inputTipoMedicamento.style.display  = "none";
            inputTipoMedicamento.value = "";
        } else if (selectTipo.value === "MEDICINA") {
            inputTipoMedicamento.style.display  = "inline-block";
            inputFechaVencimiento.style.display = "none";
            inputFechaVencimiento.value = "";
        } else {
            inputFechaVencimiento.style.display = "none";
            inputTipoMedicamento.style.display  = "none";
            inputFechaVencimiento.value = "";
            inputTipoMedicamento.value  = "";
        }
    }

    if (selectTipo) {
        actualizarCamposTipo();
        selectTipo.addEventListener("change", actualizarCamposTipo);
    }

    async function cargarUbicacionesEnSelects() {
        try {
            const resp = await fetch("/api/ubicaciones");
            if (!resp.ok) {
                alert("No se pudieron cargar las ubicaciones.");
                return;
            }

            const data = await resp.json(); // [{nombre,...}]

            if (selectUbicacionRec) {
                selectUbicacionRec.innerHTML = '<option value="">Selecciona ubicación</option>';
            }
            if (selectUbicacionEq) {
                selectUbicacionEq.innerHTML  = '<option value="">Selecciona ubicación</option>';
            }
            if (selectUbicacionAsignar) {
                selectUbicacionAsignar.innerHTML = '<option value="">Selecciona ubicación</option>';
            }

            data.forEach(u => {
                if (selectUbicacionRec) {
                    const op1 = document.createElement("option");
                    op1.value = u.nombre;
                    op1.textContent = u.nombre;
                    selectUbicacionRec.appendChild(op1);
                }
                if (selectUbicacionEq) {
                    const op2 = document.createElement("option");
                    op2.value = u.nombre;
                    op2.textContent = u.nombre;
                    selectUbicacionEq.appendChild(op2);
                }
                if (selectUbicacionAsignar) {
                    const op3 = document.createElement("option");
                    op3.value = u.nombre;
                    op3.textContent = u.nombre;
                    selectUbicacionAsignar.appendChild(op3);
                }
            });

        } catch (e) {
            console.error("Error al cargar ubicaciones:", e);
            alert("Error al cargar las ubicaciones.");
        }
    }

    // Formatea cantidad + datos extra
    function formatearCantidadYExtras(recurso) {
        let texto = `${recurso.cantidad}`;
        if (recurso.tipo === "ALIMENTO" && recurso.vencimiento) {
            texto += ` (vence: ${recurso.vencimiento})`;
        } else if (recurso.tipo === "MEDICINA" && recurso.medicamento) {
            texto += ` (tipo: ${recurso.medicamento})`;
        }
        return texto;
    }

    async function listarRecursos() {
        if (!tablaRecursosBody) return;
        try {
            const respuesta = await fetch("/api/recursos");
            if (!respuesta.ok) {
                tablaRecursosBody.innerHTML =
                    '<tr><td colspan="3" class="muted center">Error al cargar recursos</td></tr>';
                return;
            }

            const data = await respuesta.json();

            if (!data || data.length === 0) {
                tablaRecursosBody.innerHTML =
                    '<tr><td colspan="3" class="muted center">Sin recursos</td></tr>';
                return;
            }

            const filas = data.map(r => {
                const tipo = r.tipo || "DESCONOCIDO";
                const cantidadConExtras = formatearCantidadYExtras(r);
                const ubic = r.ubicacion || "Sin ubicación";

                return `
                    <tr>
                        <td>${r.nombre ? `${r.nombre} (${tipo})` : tipo}</td>
                        <td>${cantidadConExtras}</td>
                        <td>${ubic}</td>
                    </tr>
                `;
            });

            tablaRecursosBody.innerHTML = filas.join("");

        } catch (err) {
            console.error("Error al listar recursos:", err);
            tablaRecursosBody.innerHTML =
                '<tr><td colspan="3" class="muted center">Error al cargar recursos</td></tr>';
        }
    }

    async function listarEquipos() {
        if (!tablaEquiposBody && !selectEquipoExistente) return;

        try {
            const respuesta = await fetch("/api/equipos");
            if (!respuesta.ok) {
                if (tablaEquiposBody) {
                    tablaEquiposBody.innerHTML =
                        '<tr><td colspan="4" class="muted center">Error al cargar equipos</td></tr>';
                }
                if (selectEquipoExistente) {
                    selectEquipoExistente.innerHTML =
                        '<option value="">No se pudieron cargar los equipos</option>';
                }
                return;
            }

            const data = await respuesta.json(); // [{nombre,tipo,miembros,ubicacion}, ...]

            if (!data || data.length === 0) {
                if (tablaEquiposBody) {
                    tablaEquiposBody.innerHTML =
                        '<tr><td colspan="4" class="muted center">Sin equipos</td></tr>';
                }
                if (selectEquipoExistente) {
                    selectEquipoExistente.innerHTML =
                        '<option value="">No hay equipos registrados</option>';
                }
                return;
            }

            if (tablaEquiposBody) {
                const filas = data.map(e => {
                    const nombre   = e.nombre   || "(sin nombre)";
                    const tipo     = e.tipo     || "(sin tipo)";
                    const miembros = e.miembros ?? "-";
                    const ubic     = e.ubicacion || "Sin ubicación";
                    return `
                        <tr>
                            <td>${nombre}</td>
                            <td>${tipo}</td>
                            <td>${miembros}</td>
                            <td>${ubic}</td>
                        </tr>
                    `;
                });
                tablaEquiposBody.innerHTML = filas.join("");
            }

            if (selectEquipoExistente) {
                selectEquipoExistente.innerHTML =
                    '<option value="">Selecciona equipo</option>';
                data.forEach(e => {
                    const opt = document.createElement("option");
                    opt.value = e.nombre;
                    opt.textContent = `${e.nombre} (${e.tipo})`;
                    selectEquipoExistente.appendChild(opt);
                });
            }

        } catch (err) {
            console.error("Error al listar equipos:", err);
            if (tablaEquiposBody) {
                tablaEquiposBody.innerHTML =
                    '<tr><td colspan="4" class="muted center">Error al cargar equipos</td></tr>';
            }
            if (selectEquipoExistente) {
                selectEquipoExistente.innerHTML =
                    '<option value="">Error al cargar equipos</option>';
            }
        }
    }

    if (formularioRecurso && selectTipo && inputCantidad && selectUbicacionRec) {
        formularioRecurso.addEventListener("submit", async (evento) => {
            evento.preventDefault();

            const tipo      = selectTipo.value;
            const cantidad  = Number(inputCantidad.value);
            const ubicacion = selectUbicacionRec.value;

            if (!ubicacion || !tipo || !cantidad || cantidad <= 0) {
                alert("Por favor completa correctamente los datos del recurso.");
                return;
            }

            const body = {
                tipo: tipo,
                cantidad: cantidad,
                ubicacion: ubicacion
            };

            if (inputNombreRecurso) {
                body.nombre = inputNombreRecurso.value.trim();
            }
            if (tipo === "ALIMENTO" && inputFechaVencimiento && inputFechaVencimiento.value) {
                body.vencimiento = inputFechaVencimiento.value;
            }
            if (tipo === "MEDICINA" && inputTipoMedicamento && inputTipoMedicamento.value.trim()) {
                body.medicamento = inputTipoMedicamento.value.trim();
            }

            try {
                const respuesta = await fetch("/api/recursos", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(body)
                });

                if (!respuesta.ok) {
                    const texto = await respuesta.text();
                    alert("Datos inválidos al crear recurso: " + texto);
                    return;
                }

                formularioRecurso.reset();
                actualizarCamposTipo();
                mostrarOK();
                listarRecursos();

            } catch (err) {
                console.error("Error al crear recurso:", err);
                alert("Ocurrió un error al comunicarse con el servidor.");
            }
        });
    }

    if (formularioEquipo && inputNombreEquipo && inputTipoEquipo && inputMiembros && selectUbicacionEq) {
        formularioEquipo.addEventListener("submit", async (evento) => {
            evento.preventDefault();

            const nombre   = inputNombreEquipo.value.trim();
            const tipo     = inputTipoEquipo.value.trim();
            const miembros = Number(inputMiembros.value);
            const ubicacion = selectUbicacionEq.value;

            if (!nombre || !tipo || !ubicacion || !miembros || miembros <= 0) {
                alert("Por favor completa correctamente los datos del equipo.");
                return;
            }

            const body = { nombre, tipo, miembros, ubicacion };

            try {
                const respuesta = await fetch("/api/equipos", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(body)
                });

                if (!respuesta.ok) {
                    const texto = await respuesta.text();
                    alert("Datos inválidos al crear equipo: " + texto);
                    return;
                }

                formularioEquipo.reset();
                mostrarOK();
                listarEquipos();

            } catch (err) {
                console.error("Error al crear equipo:", err);
                alert("Ocurrió un error al comunicarse con el servidor.");
            }
        });
    }

    //  ASIGNAR EQUIPO EXISTENTE
    if (formularioAsignarEquipo && selectEquipoExistente && selectUbicacionAsignar) {
        formularioAsignarEquipo.addEventListener("submit", async (evento) => {
            evento.preventDefault();

            const nombreEquipo    = selectEquipoExistente.value;
            const nombreUbicacion = selectUbicacionAsignar.value;

            if (!nombreEquipo) {
                alert("Selecciona un equipo existente.");
                return;
            }
            if (!nombreUbicacion) {
                alert("Selecciona una ubicación existente.");
                return;
            }

            try {
                const resp = await fetch("/api/equipos/asignar", {
                    method: "PUT",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        equipo: nombreEquipo,
                        ubicacion: nombreUbicacion
                    })
                });

                if (!resp.ok) {
                    const txt = await resp.text();
                    alert("No se pudo asignar el equipo. Detalle: " + txt);
                    return;
                }

                formularioAsignarEquipo.reset();
                mostrarOK();
                listarEquipos();

            } catch (err) {
                console.error("Error al asignar equipo:", err);
                alert("Error de comunicación con el servidor.");
            }
        });
    }


    cargarUbicacionesEnSelects();
    listarRecursos();
    listarEquipos();
});
