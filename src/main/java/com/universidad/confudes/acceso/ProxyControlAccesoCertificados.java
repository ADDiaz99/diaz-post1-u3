package com.universidad.confudes.acceso;

import com.universidad.confudes.certificados.ServicioCertificados;
import com.universidad.confudes.certificados.SolicitudCertificado;


public class ProxyControlAccesoCertificados implements ServicioCertificados {

    private final ServicioCertificados real;

    public ProxyControlAccesoCertificados(ServicioCertificados real) {
        this.real = real;
    }

    @Override
    public byte[] emitir(SolicitudCertificado solicitud) {
        String rol = ContextoUsuario.rolActual();
        if (!rol.equals("ORGANIZADOR") && !rol.equals("ADMIN")) {
            throw new SecurityException(
                "Rol '" + rol + "' no autorizado para descarga masiva de certificados");
        }
        return real.emitir(solicitud);
    }
}