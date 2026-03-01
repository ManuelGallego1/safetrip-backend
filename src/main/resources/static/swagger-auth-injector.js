/**
 * Script que inyecta automáticamente el token JWT en Swagger UI
 * Este archivo debe estar en: src/main/resources/static/swagger-auth-injector.js
 */

(function() {
    'use strict';

    console.log('🔐 Swagger Auth Injector cargado');

    // Verificar si hay token en sessionStorage
    const token = sessionStorage.getItem('swagger_jwt_token');
    const userRole = sessionStorage.getItem('swagger_user_role');
    const userEmail = sessionStorage.getItem('swagger_user_email');

    if (!token) {
        console.warn('⚠️ No se encontró token JWT. Redirigiendo al login...');
        window.location.href = '/swagger-login.html';
        return;
    }

    console.log('✅ Token JWT encontrado');
    console.log('👤 Usuario:', userEmail);
    console.log('🎭 Rol:', userRole);

    // Esperar a que Swagger UI esté completamente cargado
    function waitForSwaggerUI() {
        const checkInterval = setInterval(() => {
            const swaggerUI = window.ui;

            if (swaggerUI && swaggerUI.preauthorizeApiKey) {
                clearInterval(checkInterval);

                // Autorizar automáticamente con el token
                swaggerUI.preauthorizeApiKey('bearerAuth', token);

                console.log('✅ Token inyectado automáticamente en Swagger');

                // Mostrar mensaje de bienvenida en la consola
                console.log('%c🎉 Swagger autenticado exitosamente! ',
                    'background: #667eea; color: white; font-size: 14px; padding: 10px; border-radius: 5px;');
                console.log(`%cRol: ${userRole}`,
                    'color: #667eea; font-weight: bold;');

                // Agregar botón de logout en la página
                addLogoutButton(userEmail, userRole);
            }
        }, 100);

        // Timeout después de 10 segundos
        setTimeout(() => {
            clearInterval(checkInterval);
            console.warn('⚠️ No se pudo cargar Swagger UI completamente');
        }, 10000);
    }

    // Agregar botón de logout
    function addLogoutButton(email, role) {
        setTimeout(() => {
            const topbar = document.querySelector('.topbar');
            if (topbar && !document.getElementById('swagger-user-info')) {
                const userInfo = document.createElement('div');
                userInfo.id = 'swagger-user-info';
                userInfo.style.cssText = `
                    display: flex;
                    align-items: center;
                    gap: 15px;
                    margin-right: 20px;
                    color: #3b4151;
                    font-size: 14px;
                `;

                userInfo.innerHTML = `
                    <div style="display: flex; align-items: center; gap: 10px;">
                        <span style="font-weight: 600;">👤 ${email}</span>
                        <span style="background: #667eea; color: white; padding: 4px 12px; border-radius: 12px; font-size: 12px;">
                            ${role}
                        </span>
                    </div>
                    <button id="swagger-logout-btn" style="
                        background: #dc3545;
                        color: white;
                        border: none;
                        padding: 8px 16px;
                        border-radius: 6px;
                        cursor: pointer;
                        font-size: 13px;
                        font-weight: 600;
                        transition: background 0.2s;
                    ">
                        🚪 Cerrar Sesión
                    </button>
                `;

                topbar.appendChild(userInfo);

                // Evento de logout
                document.getElementById('swagger-logout-btn').addEventListener('click', () => {
                    if (confirm('¿Estás seguro de que deseas cerrar sesión?')) {
                        // Limpiar sessionStorage
                        sessionStorage.removeItem('swagger_jwt_token');
                        sessionStorage.removeItem('swagger_user_role');
                        sessionStorage.removeItem('swagger_user_email');

                        // Limpiar cookie
                        document.cookie = 'swagger_auth_token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT';

                        window.location.href = '/swagger-login.html';
                    }
                });

                // Hover effect
                const logoutBtn = document.getElementById('swagger-logout-btn');
                logoutBtn.addEventListener('mouseenter', () => {
                    logoutBtn.style.background = '#c82333';
                });
                logoutBtn.addEventListener('mouseleave', () => {
                    logoutBtn.style.background = '#dc3545';
                });
            }
        }, 1000);
    }

    // Interceptar todas las peticiones para agregar el token
    const originalFetch = window.fetch;
    window.fetch = function(...args) {
        const [url, config = {}] = args;

        // Agregar token a todas las peticiones API
        if (typeof url === 'string' && url.includes('/api/')) {
            config.headers = {
                ...config.headers,
                'Authorization': `Bearer ${token}`
            };
        }

        return originalFetch(url, config)
            .catch(error => {
                if (error.message.includes('401') || error.message.includes('Unauthorized')) {
                    console.error('❌ Token expirado o inválido. Redirigiendo al login...');
                    sessionStorage.clear();
                    window.location.href = '/swagger-login.html';
                }
                throw error;
            });
    };

    // Iniciar el proceso
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', waitForSwaggerUI);
    } else {
        waitForSwaggerUI();
    }

})();