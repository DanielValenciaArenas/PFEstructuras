// /public/js/auth.js
const Auth = (() => {
    const KEY = "sessionPFE";

    function isLogged() {
        try { return !!JSON.parse(localStorage.getItem(KEY)); }
        catch { return false; }
    }

    function getSession() {
        try { return JSON.parse(localStorage.getItem(KEY)) || null; }
        catch { return null; }
    }

    function requireLogin() {
        if (!isLogged()) {
            location.replace("/public/login.html");
        }
    }

    async function login(usuario, contrasena) {
        const res = await fetch("/api/login", {
            method: "POST",
            headers: {"Content-Type":"application/json"},
            body: JSON.stringify({usuario, contrasena})
        });

        if (!res.ok) return false;

        const data = await res.json();
        if (!data.ok) return false;

        localStorage.setItem(KEY, JSON.stringify({
            usuario,
            nombre: data.nombre,
            rol: data.rol
        }));

        // 🔥 Redirigir correctamente al dashboard
        setTimeout(() => {
            location.replace("/public/dashboard.html");
        }, 20);

        return true;
    }

    function logout() {
        localStorage.removeItem(KEY);
        location.replace("/public/login.html");
    }

    return { isLogged, getSession, requireLogin, login, logout };
})();
