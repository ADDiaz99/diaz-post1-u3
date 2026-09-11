# diaz-post1-u3
Post-contenido — Patrones Estructurales aplicados al backend de ConfUDES

## Decisiones de diseño

### Necesidad 1 — Registro de asistencia

**Patrón seleccionado:** Adapter (`AdaptadorQRCheck`).

**Justificación:** El sistema necesita interactuar con un único servicio externo, `QRCheckClient`, pero su interfaz no es compatible con la que utiliza internamente el sistema, `ServicioAsistencia`. Mientras `QRCheckClient` trabaja con el método `validar(QRCheckRequest)` y maneja códigos de respuesta como 200 y 401, el sistema interno espera trabajar con `ResultadoCheckIn`. Además, existe una diferencia en los tipos de datos, ya que el proveedor recibe el `eventoId` como `long`, mientras que internamente se maneja como `String`.

Por esta razón, el patrón Adapter es el más adecuado, ya que permite colocar `QRCheckClient` detrás de la interfaz `ServicioAsistencia` y convertir los datos y resultados entre ambos contratos. De esta manera, se puede integrar el servicio externo sin realizar modificaciones en las clases existentes, como `ControladorCheckIn` y el módulo de reportes.

**Por qué no se utilizaría Facade:** Facade está pensado principalmente para simplificar el acceso a un conjunto de varios servicios o colaboradores que deben ser coordinados. En este caso solamente existe un servicio externo y el inconveniente principal no es la cantidad de colaboradores, sino que la interfaz de `QRCheckClient` no coincide con la interfaz que necesita el sistema. Por lo tanto, Facade no solucionaría directamente el problema existente.

### Necesidad 2 — Emisión de certificados

**Patrón seleccionado:** Facade (`ServicioCertificadosImpl`).

**Justificación:** En este caso participan cuatro servicios: `ValidadorAsistencia`, `GeneradorCertificadoPDF`, `FirmaDigitalService` y `EnvioCorreoService`. Todos ellos poseen interfaces que pueden ser utilizadas directamente y no existe ningún problema de incompatibilidad entre sus contratos. La dificultad está en que `ControladorCertificados` debía conocer y manejar los cuatro servicios de manera individual, haciendo que el controlador fuera más complejo y susceptible a cambios.

El patrón Facade permite centralizar toda esta lógica en una única clase, encargándose de coordinar el proceso completo: validar la asistencia, generar el certificado, firmarlo digitalmente y finalmente enviarlo por correo. Así, `ControladorCertificados` solamente necesita trabajar con un único colaborador y realizar una llamada sencilla mediante `emitir(...)`, reduciendo considerablemente su complejidad.

**Por qué no se utilizaría Adapter:** Adapter se utiliza cuando existe una incompatibilidad entre la interfaz que necesita un cliente y la interfaz que ofrece un colaborador. En este escenario no existe ese inconveniente, ya que los cuatro servicios cuentan con APIs compatibles y funcionales. Implementar un Adapter no solucionaría el problema principal, porque `ControladorCertificados` seguiría teniendo que conocer los cuatro servicios. Lo que realmente se necesita es ocultar y centralizar esa coordinación, que es precisamente lo que proporciona Facade.
