package com.mandalecarbon.model;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Traduce el estado operativo a una clase de color que las vistas pueden presentar. */
@Component("tono")
public class Tono {
    private static final Map<String, String> TONOS = new LinkedHashMap<>();
    static {
        // más específicos primero (se compara con startsWith)
        TONOS.put("por aceptar", "warn");
        TONOS.put("en preparación", "hot");
        TONOS.put("listo", "ok");
        TONOS.put("en camino", "info");
        TONOS.put("entregado", "mute");
        TONOS.put("pagado", "ok");
        TONOS.put("pendiente", "warn");
        TONOS.put("por validar", "info");
        TONOS.put("observado", "bad");
        TONOS.put("disponible", "ok");
        TONOS.put("libre", "ok");
        TONOS.put("ocupada", "hot");
        TONOS.put("reservada", "info");
        TONOS.put("confirmada", "ok");
        TONOS.put("stock bajo", "warn");
        TONOS.put("crítico", "bad");
        TONOS.put("pausado", "mute");
        TONOS.put("no aplica", "mute");
        TONOS.put("en proceso", "hot");
        TONOS.put("cobrar", "warn");
    }

    /** Devuelve ok | warn | hot | info | bad | mute, o "" si el texto no es un estado. */
    public String de(String texto) {
        if (texto == null) return "";
        String t = texto.toLowerCase();
        for (Map.Entry<String, String> e : TONOS.entrySet()) {
            if (t.startsWith(e.getKey())) return e.getValue();
        }
        return "";
    }
}
