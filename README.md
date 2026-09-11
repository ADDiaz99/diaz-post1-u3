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

### Necesidad 3 — Mejoras opcionales del certificado

**Patrón seleccionado:** Decorator (`MejoraMarcaDeAgua`, `MejoraCodigoQR`, `MejoraTraduccionIngles`).

**Justificación:** Se decidió no utilizar herencia porque sería necesario crear una subclase diferente para cada combinación de mejoras. Con tres mejoras ya existen 8 combinaciones posibles, y si posteriormente se añadiera una cuarta, la cantidad aumentaría a 16 subclases. Esto haría que la solución fuera cada vez más difícil de mantener y no escalara correctamente.

También se descartó la opción de utilizar parámetros booleanos dentro de `emitir(...)`. Cada nueva mejora implicaría agregar otro parámetro y nuevas condiciones `if` dentro del método. Además, el orden en que se aplican las mejoras quedaría definido directamente en el código, en lugar de permitir que se establezca al momento de construir el servicio.

Decorator permite solucionar ambos inconvenientes. Cada mejora se implementa como una clase que cumple con la interfaz `ServicioCertificados` y que envuelve otro objeto del mismo tipo, ya sea el Facade principal o algún otro decorator. De esta manera, las mejoras pueden combinarse y organizarse en cualquier orden sin tener que crear una clase para cada combinación. Solo sería necesario crear una nueva clase cuando aparezca una nueva mejora.

**Por qué no se utilizaría Proxy:** Proxy se enfoca principalmente en controlar el acceso o decidir si una operación debe ser delegada al objeto real. No está diseñado para ir agregando funcionalidades al resultado ni para combinar diferentes comportamientos de manera acumulativa. Si se utilizara Proxy para las mejoras, el problema de poder combinarlas libremente seguiría existiendo, ya que cada componente tendría que encargarse tanto de controlar la ejecución como de modificar el resultado. Decorator separa mejor estas responsabilidades y permite encadenar las mejoras de forma natural.

### Necesidad 4 — Control de acceso a la descarga masiva

**Patrón seleccionado:** Proxy de protección (`ProxyControlAccesoCertificados`).

**Justificación:** El resto del sistema debe continuar trabajando con la interfaz `ServicioCertificados` sin tener que conocer que existe una validación de permisos. El Proxy utiliza la misma interfaz que el servicio real y se encarga de verificar el acceso antes de permitir la operación.

Si el resultado de `ContextoUsuario.rolActual()` no corresponde a `ORGANIZADOR` o `ADMIN`, el Proxy lanza una `SecurityException` y evita realizar la llamada a `real.emitir(...)`. Esto es importante porque la emisión puede implicar operaciones costosas, como una llamada al proveedor de firma digital, y no tiene sentido ejecutarlas cuando el usuario no cuenta con los permisos necesarios.

**Por qué no se utilizaría Decorator:** Un Decorator está diseñado para agregar comportamiento y posteriormente delegar la operación al objeto que está envolviendo. Por su estructura, no está pensado principalmente para bloquear la ejecución antes de que esta ocurra. Si se utilizara para controlar los permisos, la operación podría llegar a ejecutarse antes de poder reaccionar al resultado, provocando que el proveedor de firma realice una llamada incluso para un usuario que no tiene autorización. Proxy resulta más apropiado porque permite verificar los permisos y decidir si la operación se ejecuta o se bloquea desde el principio.

### Reflexión — Composite y Flyweight (opcional)

La agenda de un congreso, organizada mediante tracks, sesiones y actividades, puede representarse fácilmente como una estructura jerárquica en forma de árbol. Por esta razón, el patrón Composite sería apropiado, ya que permitiría manejar tanto una actividad individual como una sesión completa utilizando una interfaz común.

Por otro lado, Flyweight no resulta adecuado para las credenciales QR, debido a que cada una contiene información específica de un participante y de un evento. Al no existir un estado común que pueda compartirse de manera significativa entre diferentes instancias, no habría un beneficio real en utilizar este patrón.

## Herramientas utilizadas

* Java 17, Spring Boot 3.2, Apache Maven, JUnit 5
* VS Code, Git, GitHub

## Conclusiones

Diferenciar entre Decorator y Proxy puede resultar complicado porque ambos patrones tienen una estructura muy similar y utilizan objetos que envuelven a otros objetos. Sin embargo, durante el desarrollo fue posible entender que la principal diferencia está en su propósito: Decorator busca agregar funcionalidades, mientras que Proxy se enfoca en controlar el acceso o la ejecución. También aprendí que elegir un patrón no consiste simplemente en utilizar el primero que permita solucionar el problema, sino en analizar qué necesidad específica tiene el sistema y comparar las alternativas disponibles. Descartar patrones que parecen adecuados al principio ayuda a encontrar una solución más organizada, escalable y fácil de mantener.

### Reflexión — Composite y Flyweight 
La agenda de cada congreso (tracks → sesiones → actividades) es una
estructura árbol-de-partes donde Composite encajaría naturalmente,
permitiendo tratar una actividad individual y una sesión completa de
forma uniforme. Flyweight, en cambio, no aplica a las credenciales QR:
cada una tiene datos únicos e irrepetibles por participante y evento,
sin estado compartible entre instancias que valga la pena extraer.