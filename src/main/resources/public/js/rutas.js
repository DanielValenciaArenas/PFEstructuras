// rutas.js
// Interfaz de Rutas: SOLO calcular la ruta más corta.
// No se crean rutas aquí, solo se usan las que ya existen en el sistema.

document.addEventListener("DOMContentLoaded", () => {

    const selectOrigen   = document.getElementById("origen");
    const selectDestino  = document.getElementById("destino");
    const formRutaCorta  = document.getElementById("formRutaCorta");
    const areaResultado  = document.getElementById("resultado");

    // Cargar ubicaciones disponibles desde el backend
    async function cargarUbicaciones() {
        try {
            const respuesta = await fetch("/api/ubicaciones");
            if (!respuesta.ok) {
                throw new Error("No se pudieron cargar las ubicaciones");
            }

            const ubicaciones = await respuesta.json();

            // Limpiar selects
            selectOrigen.innerHTML  = '<option value="">Seleccione origen...</option>';
            selectDestino.innerHTML = '<option value="">Seleccione destino...</option>';

            // Llenar selects con los nombres de las ubicaciones
            ubicaciones.forEach(u => {
                const opcionOrigen = document.createElement("option");
                opcionOrigen.value = u.nombre;
                opcionOrigen.textContent = u.nombre;

                const opcionDestino = document.createElement("option");
                opcionDestino.value = u.nombre;
                opcionDestino.textContent = u.nombre;

                selectOrigen.appendChild(opcionOrigen);
                selectDestino.appendChild(opcionDestino);
            });

        } catch (e) {
            console.error(e);
            selectOrigen.innerHTML  = '<option value="">Error cargando ubicaciones</option>';
            selectDestino.innerHTML = '<option value="">Error cargando ubicaciones</option>';
            areaResultado.textContent = "Ocurrió un error al cargar las ubicaciones.";
        }
    }

    // Manejar el envío del formulario para calcular la ruta más corta
    formRutaCorta.addEventListener("submit", async (evento) => {
        evento.preventDefault();

        const origen  = selectOrigen.value;
        const destino = selectDestino.value;

        if (!origen || !destino) {
            areaResultado.textContent = "Debe seleccionar origen y destino.";
            return;
        }

        if (origen === destino) {
            areaResultado.textContent = "El origen y el destino no pueden ser la misma ubicación.";
            return;
        }

        try {
            const url = `/api/rutas/corta?origen=${encodeURIComponent(origen)}&destino=${encodeURIComponent(destino)}`;
            const respuesta = await fetch(url);

            const texto = await respuesta.text();

            if (!respuesta.ok) {
                areaResultado.textContent = "Error al calcular la ruta: " + texto;
                return;
            }

            // El backend devuelve un texto simple, lo mostramos tal cual
            areaResultado.textContent = texto;

        } catch (e) {
            console.error(e);
            areaResultado.textContent = "Ocurrió un error al comunicarse con el servidor.";
        }
    });

    // Inicializar: cargar ubicaciones al abrir la página
    cargarUbicaciones();
});
