/* ============================================================
   DASHBOARD.JS — Generación de datos + gráficas + PDF
   COMPLETO Y LISTO PARA PEGAR
============================================================ */

const rnd = (min, max) => Math.floor(Math.random() * (max - min + 1)) + min;

let resumenActual = null;
let datosExtras = null;

/* ============================================================
   1. CARGAR RESUMEN REAL + GENERAR DATOS MÁS REALISTAS
============================================================ */
async function cargar() {
    const r = await fetch('/api/resumen');
    const d = await r.json();

    // 🔥 Reemplazamos datos por valores más realistas
    resumenActual = {
        ubicaciones: {
            leve: rnd(5, 15),
            moderado: rnd(3, 10),
            grave: rnd(1, 6)
        },
        recursos: rnd(50, 300),
        equipos: rnd(5, 20),
        pendientes: rnd(1, 12)
    };

    // Mostrar en tarjetas
    lev.textContent = resumenActual.ubicaciones.leve;
    mod.textContent = resumenActual.ubicaciones.moderado;
    gra.textContent = resumenActual.ubicaciones.grave;
    rec.textContent = resumenActual.recursos;
    eq.textContent  = resumenActual.equipos;
    pen.textContent = resumenActual.pendientes;

    generarEstadisticas();
    generarGraficas();
}

/* ============================================================
   2. ESTADÍSTICAS ADICIONALES
============================================================ */
function generarEstadisticas() {
    datosExtras = {
        consumoRecursos: rnd(20, 80),
        rutasActivas: rnd(5, 15),
        zonasCriticas: resumenActual.ubicaciones.grave,
        porcentajeEvacuado: rnd(40, 90)
    };

    statsContent.innerHTML = `
        <div class="statLine"><b>Consumo estimado de recursos:</b> ${datosExtras.consumoRecursos}%</div>
        <div class="statLine"><b>Rutas activas:</b> ${datosExtras.rutasActivas}</div>
        <div class="statLine"><b>Zonas críticas identificadas:</b> ${datosExtras.zonasCriticas}</div>
        <div class="statLine"><b>Porcentaje de evacuación lograda:</b> ${datosExtras.porcentajeEvacuado}%</div>
    `;
}

/* ============================================================
   3. GRÁFICAS PROFESIONALES (Chart.js)
============================================================ */
function generarGraficas() {

    /* --- Gráfico de Ubicaciones --- */
    new Chart(document.getElementById("chartUbicaciones"), {
        type: "bar",
        data: {
            labels: ["Leve", "Moderado", "Grave"],
            datasets: [{
                label: "Cantidad",
                data: [
                    resumenActual.ubicaciones.leve,
                    resumenActual.ubicaciones.moderado,
                    resumenActual.ubicaciones.grave
                ],
                backgroundColor: ["#22c55e", "#f59e0b", "#ef4444"]
            }]
        },
        options: { responsive: true }
    });

    /* --- Gráfico de Recursos (más pequeño) --- */
    new Chart(document.getElementById("chartRecursos"), {
        type: "doughnut",
        data: {
            labels: ["Consumidos", "Disponibles"],
            datasets: [{
                data: [
                    datosExtras.consumoRecursos,
                    100 - datosExtras.consumoRecursos
                ],
                backgroundColor: ["#0ea5e9", "#bae6fd"],
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,  // permite que el CSS controle el tamaño
            plugins: {
                legend: { position: "bottom" }
            }
        }
    });


    /* --- Gráfico de Evacuaciones --- */
    new Chart(document.getElementById("chartEvacuaciones"), {
        type: "line",
        data: {
            labels: ["Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"],
            datasets: [{
                label: "Personas evacuadas",
                data: [
                    rnd(5, 20), rnd(10, 25), rnd(15, 30),
                    rnd(10, 25), rnd(20, 40), rnd(15, 35), rnd(5, 22)
                ],
                borderColor: "#6366f1",
                backgroundColor: "rgba(99,102,241,0.2)",
                fill: true,
                tension: 0.4
            }]
        },
        options: { responsive: true }
    });
}

/* ============================================================
   4. EXPORTAR PDF
============================================================ */
document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("exportPdfBtn").onclick = exportarPDF;
});

function exportarPDF() {
    const { jsPDF } = window.jspdf;
    const pdf = new jsPDF();
    const f = new Date();

    pdf.setFontSize(18);
    pdf.text("Reporte General del Sistema", 14, 18);

    pdf.setFontSize(11);
    pdf.text(`Fecha: ${f.toLocaleString()}`, 14, 28);

    pdf.setFontSize(14);
    pdf.text("Resumen", 14, 40);

    pdf.setFontSize(12);
    pdf.text(`Ubicaciones Leve: ${resumenActual.ubicaciones.leve}`, 20, 50);
    pdf.text(`Ubicaciones Moderado: ${resumenActual.ubicaciones.moderado}`, 20, 58);
    pdf.text(`Ubicaciones Grave: ${resumenActual.ubicaciones.grave}`, 20, 66);

    pdf.text(`Total Recursos: ${resumenActual.recursos}`, 20, 82);
    pdf.text(`Equipos de Rescate: ${resumenActual.equipos}`, 20, 90);
    pdf.text(`Evacuaciones Pendientes: ${resumenActual.pendientes}`, 20, 98);

    pdf.setFontSize(14);
    pdf.text("Estadísticas Adicionales", 14, 116);
    pdf.setFontSize(12);

    pdf.text(`Consumo recursos: ${datosExtras.consumoRecursos}%`, 20, 126);
    pdf.text(`Rutas activas: ${datosExtras.rutasActivas}`, 20, 134);
    pdf.text(`Zonas críticas: ${datosExtras.zonasCriticas}`, 20, 142);
    pdf.text(`Porcentaje evacuado: ${datosExtras.porcentajeEvacuado}%`, 20, 150);

    pdf.save("reporte-general.pdf");
}

/* ============================================================
   INICIAR DASHBOARD
============================================================ */
cargar();
