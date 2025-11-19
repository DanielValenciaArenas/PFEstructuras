// /public/js/dashboard.js
// Requiere Chart.js

(async function () {
    // utilidades
    const $ = id => document.getElementById(id);

    const canvasUbic = $('chartUbicaciones');
    const canvasRec  = $('chartRecursos');
    const canvasEvac = $('chartEvacuaciones');

    // Chart instances
    let chartUbic = null, chartRec = null, chartEv = null;

    async function fetchJson(url) {
        const res = await fetch(url);
        if (!res.ok) throw new Error(`HTTP ${res.status} ${url}`);
        return await res.json();
    }

    // Carga todos los datos necesarios
    async function obtenerDatos() {
        const resumen = await fetchJson('/api/resumen');
        const ubicaciones = await fetchJson('/api/ubicaciones');
        const recursos = await fetchJson('/api/recursos');
        const equipos = await fetchJson('/api/equipos');
        const evacuaciones = await fetchJson('/api/evacuaciones');
        return { resumen, ubicaciones, recursos, equipos, evacuaciones };
    }

    // Rellena los widgets superiores (números grandes)
    function renderWidgets(resumen) {
        $('lev').textContent = resumen.ubicaciones.leve ?? 0;
        $('mod').textContent = resumen.ubicaciones.moderado ?? 0;
        $('gra').textContent = resumen.ubicaciones.grave ?? 0;
        $('rec').textContent = resumen.recursos ?? 0;
        $('eq').textContent  = resumen.equipos ?? 0;
        $('pen').textContent = resumen.pendientes ?? 0;
    }

    // Construye dataset para el gráfico de ubicaciones
    function datosGraficoUbicaciones(resumen) {
        return {
            labels: ['Leve', 'Moderado', 'Grave'],
            datasets: [{
                label: 'Ubicaciones por nivel',
                data: [resumen.ubicaciones.leve || 0, resumen.ubicaciones.moderado || 0, resumen.ubicaciones.grave || 0],
                backgroundColor: ['#10b981','#f59e0b','#ef4444'],
                borderColor: ['#059669','#b45309','#dc2626'],
                borderWidth: 1
            }]
        };
    }

    // Construye dataset para recursos: sumar cantidades por tipo
    function datosGraficoRecursos(recursosList) {
        const counts = {};
        (recursosList || []).forEach(r => {
            const t = (r.tipo || 'OTRO').toUpperCase();
            counts[t] = (counts[t] || 0) + (r.cantidad || 0);
        });
        const labels = Object.keys(counts);
        const data = labels.map(l => counts[l]);
        const colors = labels.map((_,i) => ['#60a5fa','#34d399','#f59e0b','#f97316'][i % 4]);
        return { labels, datasets: [{ label: 'Recursos (cantidad total)', data, backgroundColor: colors }] };
    }

    // Gráfico de evacuaciones: conteo por estado para ver actividad
    function datosGraficoEvacuaciones(evList) {
        const estados = { PENDIENTE:0, EN_PROCESO:0, COMPLETADA:0 };
        (evList || []).forEach(e => {
            const s = e.estado || 'PENDIENTE';
            if (estados[s] !== undefined) estados[s]++;
            else estados[s] = (estados[s]||0)+1;
        });
        return {
            labels: Object.keys(estados),
            datasets: [{
                label: 'Evacuaciones',
                data: Object.values(estados),
                backgroundColor: ['#f59e0b','#06b6d4','#10b981']
            }]
        };
    }

    // Crear/actualizar charts Chart.js
    function crearOModificarChart(elCanvas, tipo, datos, opciones, referencia){
        if(!elCanvas) return null;
        if(referencia && referencia.destroy) referencia.destroy();
        return new Chart(elCanvas.getContext('2d'), {
            type: tipo,
            data: datos,
            options: opciones || {}
        });
    }

    async function exportarPDF(resumen, datosExtras) {
        const { jsPDF } = window.jspdf;
        const pdf = new jsPDF('p','pt','a4');
        const margen = 40;
        let y = 40;

        pdf.setFontSize(16);
        pdf.text('Reporte general — Sistema de Gestión de Desastres', margen, y);
        y += 22;

        pdf.setFontSize(10);
        pdf.text(`Fecha: ${new Date().toLocaleString()}`, margen, y);
        y += 20;

        pdf.setFontSize(12);
        pdf.text('Resumen de ubicaciones:', margen, y); y+=16;
        pdf.setFontSize(11);
        pdf.text(` • Leve: ${resumen.ubicaciones.leve}`, margen+8, y); y+=14;
        pdf.text(` • Moderado: ${resumen.ubicaciones.moderado}`, margen+8, y); y+=14;
        pdf.text(` • Grave: ${resumen.ubicaciones.grave}`, margen+8, y); y+=18;

        pdf.text('Recursos y equipos:', margen, y); y+=16;
        pdf.text(` • Recursos totales (items): ${resumen.recursos}`, margen+8, y); y+=14;
        pdf.text(` • Equipos de rescate: ${resumen.equipos}`, margen+8, y); y+=18;

        try {
            if (chartUbic) {
                const imgU = chartUbic.toBase64Image();
                pdf.addImage(imgU, 'PNG', margen, y, 240, 120);
            }
            if (chartRec) {
                const imgR = chartRec.toBase64Image();
                pdf.addImage(imgR, 'PNG', margen + 260, y, 240, 120);
            }
            y += 140;
            if (chartEv) {
                const imgE = chartEv.toBase64Image();
                pdf.addImage(imgE, 'PNG', margen, y, 240, 120);
            }
        } catch (err) {
            console.warn('No se pudieron exportar imágenes de charts:', err);
        }

        pdf.save('reporte-general.pdf');
    }

    // Inicializador principal
    async function init() {
        try {
            const { resumen, recursos, evacuaciones } = await obtenerDatos();

            renderWidgets(resumen);

            // Crear charts
            const optSmall = {
                maintainAspectRatio: false,
                plugins: { legend: { display: false } }
            };

            // Ubicaciones -> bar
            chartUbic = crearOModificarChart(canvasUbic, 'bar', datosGraficoUbicaciones(resumen), {
                maintainAspectRatio: false,
                scales: { y: { beginAtZero:true, ticks:{precision:0} } }
            }, chartUbic);

            chartRec = crearOModificarChart(canvasRec, 'doughnut', datosGraficoRecursos(recursos), {
                maintainAspectRatio: false,
                plugins: { legend: { position: 'bottom' } }
            }, chartRec);

            chartEv = crearOModificarChart(canvasEvac, 'pie', datosGraficoEvacuaciones(evacuaciones), {
                maintainAspectRatio: false,
                plugins: { legend: { position: 'bottom' } }
            }, chartEv);

            // Botón Exportar PDF
            const btn = $('exportPdfBtn');
            if (btn) {
                btn.onclick = () => exportarPDF(resumen, { });
            }

        } catch (err) {
            console.error('Error cargando dashboard:', err);
            // mostrar error mínimo en pantalla
            const c = $('statsContent');
            if (c) c.innerHTML = '<div class="muted">No se pudieron cargar las estadísticas (revisa consola).</div>';
        }
    }

    init();

})();
