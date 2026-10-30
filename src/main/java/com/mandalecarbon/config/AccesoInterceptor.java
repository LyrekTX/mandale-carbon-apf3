package com.mandalecarbon.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Protege los paneles internos: cada rol entra solo a su panel; el gerente entra a todos.
 * Sin sesión de personal, redirige al acceso del personal.
 */
public class AccesoInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        String uri = req.getRequestURI().substring(req.getContextPath().length());
        String requerido = uri.split("/")[1];
        HttpSession s = req.getSession(false);
        String rol = s == null ? null : (String) s.getAttribute("rol");
        if (rol != null && (rol.equals("gerente") || rol.equals(requerido))) {
            return true;
        }
        res.sendRedirect(req.getContextPath() + "/acceso?destino=" + requerido);
        return false;
    }
}
