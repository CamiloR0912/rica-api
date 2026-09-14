# Notas del Taller: DDD en rica-api

Documento de auditoría y análisis de diseño guiado por el dominio (DDD).

---

## 1. Lenguaje Ubicuo: Auditoría Rápida

### ¿Hay algún campo o método con un nombre genérico (data, info, value, item) que debería tener un nombre del dominio?
No. Al revisar las clases `InvestigadorController`, `InvestigadorRequest` e `InvestigadorResponse`, los nombres utilizados corresponden directamente al lenguaje del problema y no a contenedores técnicos ambiguos:
- Campos: `nombreCompleto`, `correoInstitucional`, `grupoInvestigacion`, `id`.
- Operaciones/Métodos: `listar`, `buscarPorId`, `registrar`, `aEntidad`, `aResponse`.

Todos los términos comunican una intención de negocio específica y evitan jerga genérica o anémica como `data`, `info`, `item` o `value`.

### ¿`correoInstitucional` y `grupoInvestigacion` son términos que reconocería alguien de la Facultad sin que se los tradujeran?
Sí. En la comunidad universitaria y de investigación de la Facultad (UPTC):
- **`correoInstitucional`**: Es el identificador institucional oficial asignado a los miembros de la comunidad académica para sus labores académicas e investigativas (con terminación `@uptc.edu.co`).
- **`grupoInvestigacion`**: Corresponde a la unidad organizativa oficial reconocida por la Dirección de Investigaciones (DIN) y MinCiencias (por ejemplo, GIT-UPTC) en la que se agrupan los investigadores.

No requieren traducción porque no son tecnicismos de programación sino términos de la realidad institucional. El Lenguaje Ubicuo aquí ratifica que los nombres existentes ya son precisos y deben mantenerse.

### En `Publicacion`, el campo se llama `investigadorCorreo`, no `investigadorId` ni `autorId`. ¿Por qué ese nombre es más preciso para el Lenguaje Ubicuo de este dominio que una alternativa genérica como `refId`?
En este dominio, el correo institucional es el identificador natural, único y reconocible del investigador ante la universidad. Nombrarlo `investigadorCorreo` aporta máxima claridad semántica:
1. Comunica explícitamente el **rol del dominio** (`investigador`, no cualquier `autor` externo).
2. Especifica el **mecanismo de identidad natural** empleado (`correo`), en lugar de una clave sintética artificial o una referencia opaca (`refId`).
3. Refleja fielmente la regla de diseño DDD de referenciar otros Agregados únicamente por su identidad (`investigadorCorreo`), manteniendo la frontera entre el contexto de `investigadores` y el de `publicaciones` sin acoplarlos a nivel de entidad ni depender de IDs internos autoincrementales de una base de datos relacional.

---

## 4. Límite del Agregado: Documentación

| Pregunta | Respuesta de Diseño |
| --- | --- |
| **¿Cuál es la raíz del Agregado Investigador?** | La clase `Investigador` (es la única entidad anotada con `@Entity` en el paquete `investigadores`). |
| **¿Qué vive dentro del límite?** | La identidad `id` (Long), los atributos de dominio `nombreCompleto` y `grupoInvestigacion`, y el Value Object `correoInstitucional` (`CorreoInstitucional`). |
| **¿Por qué Publicacion NO está dentro de este límite?** | Porque un investigador puede acumular decenas o cientos de publicaciones a lo largo de su trayectoria. Incluirlas dentro de la frontera del Agregado `Investigador` violaría el principio de **"Diseñar Agregados Pequeños"** (Vaughn Vernon), acoplaría los ciclos de vida de ambas entidades, sobrecargaría el rendimiento y la memoria en consultas cotidianas del investigador, y crearía bloqueos transaccionales innecesarios. |
| **¿Qué pasaría si alguien agrega un campo `List<Publicacion> publicaciones` directo en `Investigador`?** | Se violaría la frontera del Agregado y el desacoplamiento entre contextos: 1) Dos transacciones simultáneas que agreguen o editen publicaciones del mismo investigador podrían generar colisiones y problemas de concurrencia optimista/pesimista. 2) Se acoplarían tecnologías de persistencia distintas (PostgreSQL para investigadores y MongoDB para publicaciones). 3) Se impediría que el catálogo de publicaciones evolucione o se despliegue de forma independiente como microservicio. |
