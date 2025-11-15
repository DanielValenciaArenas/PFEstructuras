// /public/js/portal.js
Auth.requireLogin();

const s = Auth.getSession();
document.getElementById("who").textContent = `${s.nombre} · ${s.rol}`;
document.getElementById("btnLogout").onclick = Auth.logout;

// Ocultar tarjetas que no corresponden al rol
document.querySelectorAll("[data-roles]").forEach(card => {
  const roles = card.getAttribute("data-roles").split(",").map(r => r.trim());
  if (!roles.includes(s.rol)) card.remove();
});
