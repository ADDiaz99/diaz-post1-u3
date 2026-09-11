package com.universidad.confudes.certificados;


public class MejoraCodigoQR implements ServicioCertificados {

    private final ServicioCertificados envuelto;
    private final String urlVerificacionBase;

    public MejoraCodigoQR(ServicioCertificados envuelto, String urlVerificacionBase) {
        this.envuelto = envuelto;
        this.urlVerificacionBase = urlVerificacionBase;
    }

    @Override
    public byte[] emitir(SolicitudCertificado solicitud) {
        byte[] documento = envuelto.emitir(solicitud);
        String url = urlVerificacionBase + "/" + solicitud.getParticipanteId();
        return UtilidadesPDF.insertarCodigoQR(documento, url);
    }
}