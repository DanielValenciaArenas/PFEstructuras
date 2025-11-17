// recursos.js
// Pantalla "Recursos y Equipos"
// Maneja:
//  - Crear recursos
//  - Crear equipos de rescate
//  - Listar recursos (incluyendo vencimiento / tipo medicamento)
//  - Listar equipos
//  - Cargar ubicaciones reales en los selects (coherencia con otras interfaces)
//  - 🔴 NUEVO: Asignar equipos EXISTENTES a ubicaciones (si existe el formulario en el HTML)

document.addEventListener("DOMContentLoaded", () => {

    // Referencias a elementos del DOM (coinciden con recursos.html)
    const formularioRecurso   = document.getElementById("fr");
    const selectTipo          = document.getElementById("tipo");
    const inputCantidad       = document.getElementById("cant");
    const selectUbicacionRec  = document.getElementById("ubic");

    const formularioEquipo    = document.getElementById("fe");
    const inputNombreEquipo   = document.getElementById("nom");
    const inputTipoEquipo     = document.getElementById("tip");
    const inputMiembros       = document.getElementById("miem");
    const selectUbicacionEq   = document.getElementById("ubi");

    const tablaRecursosBody   = document.getElementById("tb");
    const tablaEquiposBody    = document.getElementById("te");
    const divOk               = document.getElementById("ok");

    // 🔹 NUEVOS CAMPOS OPCIONALES (si existen en el HTML)
    const inputNombreRecurso      = document.getElementById("nombreRecurso");
    const inputFechaVencimiento   = document.getElementById("fechaVencimiento");
    const inputTipoMedicamento    = document.getElementById("tipoMedicamento");

    // 🔴 NUEVOS ELEMENTOS PARA ASIGNAR EQUIPOS EXISTENTES (opcional)
    const formularioAsignarEquipo = document.getElementById("formAsignarEquipo"); // <form ...>
    const selectEquipoExistente   = document.getElementById("equipoExistente");   // <select ...>
    const selectUbicacionAsignar  = document.getElementById("ubicacionEquipo");   // <select ...>

    // Mostrar mensaje "Guardado correctamente" por un momento
    function mostrarOK() {
        if (!divOk) return;
        divOk.style.display = "block";
        setTimeout(() => {
            divOk.style.display = "none";
        }, 1500);
    }

    // =============== MOSTRAR / OCULTAR CAMPOS EXTRA SEGÚN TIPO ===============
    function actualizarCamposTipo() {
        // Si no existen los inputs en el HTML, no hacemos nada
        if (!inputFechaVencimiento || !inputTipoMedicamento) return;

        if (selectTipo.value === "ALIMENTO") {
            // Mostrar fecha de vencimiento, ocultar tipo de medicamento
            inputFechaVencimiento.style.display = "inline-block";
            inputTipoMedicamento.style.display  = "none";
            inputTipoMedicamento.value = "";
        } else if (selectTipo.value === "MEDICINA") {
            // Mostrar tipo de medicamento, ocultar fecha
            inputTipoMedicamento.style.display  = "inline-block";
            inputFechaVencimiento.style.display = "none";
            inputFechaVencimiento.value = "";
        } else {
            // Otro tipo: ocultar ambos
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

    // =============== CARGAR UBICACIONES EN LOS SELECTS ===============
    async function cargarUbicacionesEnSelects() {
        try {
            const resp = await fetch("/api/ubicaciones");
            if (!resp.ok) {
                alert("No se pudieron cargar las ubicaciones.");
                return;
            }

            const data = await resp.json(); // [{nombre, tipoZona, nivelAfectacion, latitud, longitud}, ...]

            // Limpiar selects y dejar el placeholder
            if (selectUbicacionRec) {
                selectUbicacionRec.innerHTML = '<option value="">Selecciona ubicación</option>';
            }
            if (selectUbicacionEq) {
                selectUbicacionEq.innerHTML  = '<option value="">Selecciona ubicación</option>';
            }
            // 🔴 NUEVO: select de ubicación para asignar equipo
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

                // 🔴 NUEVO: misma lista para el select de asignación
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

    // Formatea la columna de "Cantidad" agregando información extra:
    //  - ALIMENTO: fecha de vencimiento
    //  - MEDICINA: tipo de medicamento
    function formatearCantidadYExtras(recurso) {
        let texto = `${recurso.cantidad}`;

        // Estos campos los debe enviar el backend si están disponibles:
        //  - recurso.vencimiento (String, ej: "2025-12-31")
        //  - recurso.medicamento (String, ej: "Analgesico")
        if (recurso.tipo === "ALIMENTO" && recurso.vencimiento) {
            texto += ` (vence: ${recurso.vencimiento})`;
        } else if (recurso.tipo === "MEDICINA" && recurso.medicamento) {
            texto += ` (tipo: ${recurso.medicamento})`;
        }

        return texto;
    }

    // =============== LISTAR RECURSOS ===============
    async function listarRecursos() {
        try {
            const respuesta = await fetch("/api/recursos");
            if (!respuesta.ok) {
                tablaRecursosBody.innerHTML =
                    '<tr><td colspan="3" class="muted center">Error al cargar recursos</td></tr>';
                return;
            }

            const data = await respuesta.json(); // [{tipo,cantidad,ubicacion,vencimiento,medicamento,...}, ...]

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
                        <td>${tipo}</td>
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

    // =============== LISTAR EQUIPOS ===============
    async function listarEquipos() {
        try {
            const respuesta = await fetch("/api/equipos");
            if (!respuesta.ok) {
                tablaEquiposBody.innerHTML =
                    '<tr><td colspan="4" class="muted center">Error al cargar equipos</td></tr>';
                // 🔴 También limpiamos el combo de equipos existentes si existe
                if (selectEquipoExistente) {
                    selectEquipoExistente.innerHTML =
                        '<option value="">No se pudieron cargar los equipos</option>';
                }
                return;
            }

            const data = await respuesta.json(); // [{nombre,tipo,miembros,ubicacion}, ...]

            if (!data || data.length === 0) {
                tablaEquiposBody.innerHTML =
                    '<tr><td colspan="4" class="muted center">Sin equipos</td></tr>';
                if (selectEquipoExistente) {
                    selectEquipoExistente.innerHTML =
                        '<option value="">No hay equipos registrados</option>';
                }
                return;
            }

            const filas = data.map(e => {
                const nombre = e.nombre || "(sin nombre)";
                const tipo   = e.tipo   || "(sin tipo)";
                const miembros = e.miembros ?? "-";
                const ubic   = e.ubicacion || "Sin ubicación";

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

            // 🔴 NUEVO: llenar combo de equipos existentes para ASIGNAR
            if (selectEquipoExistente) {
                selectEquipoExistente.innerHTML =
                    '<option value="">Selecciona un equipo</option>';
                data.forEach(e => {
                    const opt = document.createElement("option");
                    opt.value = e.nombre;
                    opt.textContent = `${e.nombre} (${e.tipo})`;
                    selectEquipoExistente.appendChild(opt);
                });
            }

        } catch (err) {
            console.error("Error al listar equipos:", err);
            tablaEquiposBody.innerHTML =
                '<tr><td colspan="4" class="muted center">Error al cargar equipos</td></tr>';

            if (selectEquipoExistente) {
                selectEquipoExistente.innerHTML =
                    '<option value="">Error al cargar equipos</option>';
            }
        }
    }

    // =============== ENVIAR FORMULARIO RECURSO ===============
    formularioRecurso.addEventListener("submit", async (evento) => {
        evento.preventDefault();

        const tipo = selectTipo.value;
        const cantidad = Number(inputCantidad.value);
        const ubicacion = selectUbicacionRec.value; // viene del select

        if (!ubicacion || !tipo || !cantidad || cantidad <= 0) {
            alert("Por favor completa correctamente los datos del recurso.");
            return;
        }

        // Cuerpo base
        const body = {
            tipo: tipo,
            cantidad: cantidad,
            ubicacion: ubicacion
        };

        // 🔹 Nombre del recurso (si el input existe)
        if (inputNombreRecurso) {
            body.nombre = inputNombreRecurso.value.trim();
        }

        // 🔹 Datos extra según tipo
        if (tipo === "ALIMENTO" && inputFechaVencimiento && inputFechaVencimiento.value) {
            body.vencimiento = inputFechaVencimiento.value; // ej: "2025-12-31"
        }
        if (tipo === "MEDICINA" && inputTipoMedicamento && inputTipoMedicamento.value.trim()) {
            body.medicamento = inputTipoMedicamento.value.trim(); // ej: "Analgésico"
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
            actualizarCamposTipo(); // volver a ocultar/mostrar campos extra
            mostrarOK();
            listarRecursos();

        } catch (err) {
            console.error("Error al crear recurso:", err);
            alert("Ocurrió un error al comunicarse con el servidor.");
        }
    });

    // =============== ENVIAR FORMULARIO EQUIPO (CREAR EQUIPO) ===============
    formularioEquipo.addEventListener("submit", async (evento) => {
        evento.preventDefault();

        const nombre = inputNombreEquipo.value.trim();
        const tipo   = inputTipoEquipo.value.trim();
        const miembros = Number(inputMiembros.value);
        const ubicacion = selectUbicacionEq.value; // viene del select

        if (!nombre || !tipo || !ubicacion || !miembros || miembros <= 0) {
            alert("Por favor completa correctamente los datos del equipo.");
            return;
        }

        const body = {
            nombre: nombre,
            tipo: tipo,
            miembros: miembros,
            ubicacion: ubicacion
        };

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

    // =============== 🔴 ENVIAR FORMULARIO ASIGNAR EQUIPO EXISTENTE ===============
    if (formularioAsignarEquipo && selectEquipoExistente && selectUbicacionAsignar) {
        formularioAsignarEquipo.addEventListener("submit", async (evento) => {
            evento.preventDefault();

            const nombreEquipo   = selectEquipoExistente.value;
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
                listarEquipos(); // para actualizar la columna "Ubicación" en la tabla

            } catch (err) {
                console.error("Error al asignar equipo:", err);
                alert("Error de comunicación con el servidor.");
            }
        });
    }

    // =============== INICIO ===============
    cargarUbicacionesEnSelects();
    listarRecursos();
    listarEquipos();
});
