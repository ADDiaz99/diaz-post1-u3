package com.universidad.confudes.certificados;

public class MejoraMarcaDeAgua implements ServicioCertificados {

    private final ServicioCertificados envuelto;
    private final String textoMarcaDeAgua;

    public MejoraMarcaDeAgua(ServicioCertificados envuelto, String textoMarcaDeAgua) {
        this.envuelto = envuelto;
        this.textoMarcaDeAgua = textoMarcaDeAgua;
    }

    @Override
    public byte[] emitir(SolicitudCertificado solicitud) {
        byte[] documento = envuelto.emitir(solicitud);
        return UtilidadesPDF.aplicarMarcaDeAgua(documento, textoMarcaDeAgua);
    }
}