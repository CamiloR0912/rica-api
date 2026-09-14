**TALLER DE CLASE · LECCIÓN 3 DE 13 · SEMANA 4 (PRÁCTICA)**

**Ingeniería de Software II**

**DDD en rica-api: de modelo anémico a modelo rico**

Trabajo individual (3 h) sobre el código real de rica-api. Hoy Investigador es una bolsa de getters y setters — el ejemplo de manual de Modelo de Dominio Anémico (guía, sección 11). Este taller lo convierte en un modelo rico, paso a paso, sin inventar un dominio nuevo: es el mismo investigadores/publicaciones que ya tienen corriendo desde el mini-curso Spring Boot.

|  |
| --- |
| **Sobre este taller**  Es individual: cada quien trabaja sobre su propia copia de rica-api y recorre el ciclo completo de patrones tácticos que vimos en la guía — Value Object, Servicio de Dominio, límite de Agregado y Factory — aplicados directamente sobre Investigador y Publicacion.  No es un ejercicio de pizarra: al final de las 3 horas, el código debe compilar, los tests deben pasar, y rica-api debe seguir funcionando exactamente igual desde afuera (mismos endpoints, mismas respuestas) — pero con un dominio mejor diseñado por dentro. Ese es, de hecho, uno de los puntos centrales de DDD: refactorizar hacia un modelo más rico no debería romper el contrato externo del sistema. |

|  |
| --- |
| **Duración estimada**  **0 · Preparación** — ~15 min  **1 · Lenguaje Ubicuo: auditoría rápida** — ~20 min  **2 · Value Object: CorreoInstitucional** — ~45 min  **3 · Servicio de Dominio** — ~35 min  **4 · Límite del Agregado (sin código)** — ~15 min  **5 · Factory** — ~35 min  **6 · Confirmar el avance en Git** — ~5 min  **Total (pasos 0–6, obligatorios): ≈ 2 h 50 min**  7 · Domain Event (opcional): +20–25 min  Son estimaciones honestas por paso, no ajustadas para completar exactamente 3 horas — por eso el total obligatorio deja un margen real de unos 10 minutos sobre el bloque de clase, para dudas, errores de compilación y el tiempo que toma releer código que no escribiste tú. Si un grupo va más lento en un paso, ese margen es justo para eso, no para el reto opcional. |

**0. Preparación ~15 min**

**1.** Ubica tu copia de rica-api (la del mini-curso Spring Boot). Para no perder tu trabajo previo, crea una rama nueva llamada taller-ddd:

|  |
| --- |
| **terminal** |
| git checkout -b taller-ddd |

**2.** Levanta las bases de datos (PostgreSQL para investigadores, MongoDB para publicaciones):

|  |
| --- |
| **terminal** |
| docker-compose up -d |

Luego confirma que el proyecto arranca correctamente:

|  |
| --- |
| **terminal** |
| mvn spring-boot:run |

**3.** Corre los tests existentes — deben pasar en verde antes de empezar. Vas a volver a correr este mismo comando después de cada paso:

|  |
| --- |
| **terminal** |
| mvn test |

**4.** Abre investigadores/Investigador.java, investigadores/InvestigadorService.java y publicaciones/Publicacion.java. Este es el punto de partida — el mismo código que dejaste al final del Tutorial 6.

|  |
| --- |
| **EL PUNTO DE PARTIDA, SIN EDITAR TODAVÍA**  Investigador hoy es exactamente el anti-patrón de la sección 11 de la guía: un @Entity con id, nombreCompleto, correoInstitucional (un String suelto, sin ninguna regla) y grupoInvestigacion (otro String suelto) — puros getters y setters, cero comportamiento. InvestigadorService.registrar() es lo único que tiene una pizca de regla de negocio (el chequeo de correo duplicado), y está mezclado con la orquestación. Esto no es un defecto de quien lo escribió — es exactamente lo esperable en el Tutorial 6, porque en ese punto del curso todavía no habíamos visto DDD. Hoy lo arreglamos. |

**1. Lenguaje Ubicuo: auditoría rápida ~20 min**

|  |
| --- |
| **Meta:** confirmar que los nombres en el código dicen lo mismo que dirían un coordinador de investigación o un investigador real — no jerga técnica genérica. |

Antes de tocar una línea de código, léete InvestigadorController, InvestigadorRequest e InvestigadorResponse completos y responde, por escrito, en un comentario o en un archivo NOTAS.md nuevo:

**?** ¿Hay algún campo o método con un nombre genérico (data, info, value, item) que debería tener un nombre del dominio?

**?** ¿correoInstitucional y grupoInvestigacion son términos que reconocería alguien de la Facultad sin que se los tradujeran? (Spoiler: sí — por eso no los vas a renombrar. El Lenguaje Ubicuo no siempre implica cambiar nombres; a veces implica confirmar que ya están bien y dejar constancia de por qué.)

**?** En Publicacion, el campo se llama investigadorCorreo, no investigadorId ni autorId. Escribe una frase explicando por qué ese nombre es más preciso para el Lenguaje Ubicuo de este dominio que una alternativa genérica como refId.

|  |
| --- |
| Este paso no cambia código. Es, a propósito, el paso que más se salta la gente — y es el que evita que los siguientes cuatro pasos se construyan sobre nombres equivocados. |

**2. Value Object: CorreoInstitucional ~45 min**

|  |
| --- |
| **Meta:** que sea imposible construir un correo institucional inválido — la regla vive en un solo lugar, no dispersa en validaciones de formulario. |

Crea la clase nueva en el paquete investigadores:

|  |
| --- |
| **investigadores/CorreoInstitucional.java** |
| **package** com.rica.ricaapi.investigadores;    **import** jakarta.persistence.Embeddable;    **@Embeddable**  **public** **record** CorreoInstitucional(String valor) {    **public** CorreoInstitucional {  **if** (valor == **null** || !valor.endsWith("@uptc.edu.co")) {  **throw** **new** IllegalArgumentException(  "El correo institucional debe pertenecer al dominio @uptc.edu.co");  }  }  } |

Ahora cambia Investigador.java para usar este tipo en vez de String:

|  |
| --- |
| **investigadores/Investigador.java (antes → después)** |
| *// antes*  **@Column**(name = "correo\_institucional", nullable = **false**, unique = **true**, length = 150)  **private** String correoInstitucional;    *// después*  **@Embedded**  **@AttributeOverride**(name = "valor",  column = **@Column**(name = "correo\_institucional", nullable = **false**, unique = **true**, length = 150))  **private** CorreoInstitucional correoInstitucional; |

Ajusta el constructor y los getters/setters de Investigador para que trabajen con CorreoInstitucional en vez de String. Esto rompe la compilación en tres sitios más — arréglalos en este orden:

| **Archivo** | **Qué cambiar** |
| --- | --- |
| InvestigadorRepository | El método derivado existsByCorreoInstitucional(String) ya no aplica sobre un tipo embebido con un solo campo simple así. Cámbialo a existsByCorreoInstitucional\_Valor(String valor) — la sintaxis de Spring Data para navegar una propiedad anidada con guion bajo. |
| InvestigadorMapper | aEntidad() ahora construye new CorreoInstitucional(request.getCorreoInstitucional()) en vez de asignar el String directo. Si el correo no cumple la regla, el record lanza IllegalArgumentException ahí mismo — antes de que el objeto exista. |
| InvestigadorServiceTest | Los constructores new Investigador(1L, "Ana Torres", "ana.torres@uptc.edu.co", "GIT-UPTC") deben envolver el correo: new Investigador(1L, "Ana Torres", new CorreoInstitucional("ana.torres@uptc.edu.co"), "GIT-UPTC"). Y el mock existsByCorreoInstitucional(...) pasa a existsByCorreoInstitucional\_Valor(...). |

|  |
| --- |
| **Verificación**  Corre mvn test. Deben seguir pasando los mismos tres tests de antes. Luego, a mano, intenta construir un CorreoInstitucional("carlos@gmail.com") en una consola de pruebas o en un test rápido: debe lanzar IllegalArgumentException. Si no lanza, revisa el constructor compacto del record. |

|  |
| --- |
| **¿Y Lombok?** — Podrías usar @Getter/@Setter de Lombok en Investigador para no escribir estos métodos a mano — es una opción legítima y muchos equipos en producción la usan. Ten cuidado, eso sí, con @Data: genera automáticamente equals()/hashCode() a partir de todos los campos, incluido el id, lo cual choca con la regla de la guía (sección 4) sobre Entidades: la igualdad de una Entidad debe basarse solo en su identidad, no en el valor de sus atributos. Si decides usar Lombok aquí, limítate a @Getter/@Setter (o solo @Getter si más adelante avanzas hacia inmutabilidad) y evita @Data o @EqualsAndHashCode sin especificar explícitamente qué campos usar. Para este taller no es obligatorio: puedes escribirlos a mano o usar Lombok si tu proyecto ya lo tiene configurado — lo que importa es que entiendas por qué la igualdad de una Entidad no puede delegarse ciegamente a una anotación. |

**3. Servicio de Dominio: límite de publicaciones por año ~35 min**

|  |
| --- |
| **Meta:** una regla de negocio real que involucra a Investigador y a Publicacion a la vez — el caso de manual para un Servicio (guía, sección 5). |

La regla: un investigador no puede tener más de 5 publicaciones registradas en el mismo año. No es una regla de Investigador (no la puede verificar solo) ni de Publicacion (tampoco, necesita contar las demás) — es exactamente el tipo de comportamiento que DDD Quickly le asigna a un Servicio: no pertenece naturalmente a ningún objeto, involucra a varios, y no guarda estado propio.

Primero, añade el método de conteo al repositorio de publicaciones:

|  |
| --- |
| **publicaciones/PublicacionRepository.java** |
| **long** countByInvestigadorCorreoAndAnio(String investigadorCorreo, Integer anio); |

Luego crea el Servicio de Dominio:

|  |
| --- |
| **publicaciones/LimitePublicacionesAnualesService.java** |
| **package** com.rica.ricaapi.publicaciones;    **import** com.rica.ricaapi.investigadores.Investigador;  **import** org.springframework.stereotype.Service;    **@Service**  **public** **class** LimitePublicacionesAnualesService {    **private** **static** **final** **int** MAXIMO\_POR\_ANIO = 5;    **private** **final** PublicacionRepository publicacionRepository;    **public** LimitePublicacionesAnualesService(PublicacionRepository publicacionRepository) {  **this**.publicacionRepository = publicacionRepository;  }    **public** **boolean** puedeRegistrar(Investigador investigador, Publicacion nueva) {  **long** registradasEsteAnio = publicacionRepository.countByInvestigadorCorreoAndAnio(  investigador.getCorreoInstitucional().valor(), nueva.getAnio());  **return** registradasEsteAnio < MAXIMO\_POR\_ANIO;  }  } |

Por último, en PublicacionService.registrar(...) (o donde hoy se guarda una Publicacion nueva), inyecta este Servicio y recházala si puedeRegistrar(...) devuelve false — lanza una excepción de negocio nueva, por ejemplo LimiteAnualExcedidoException, siguiendo el mismo patrón que ya usa CorreoDuplicadoException.

|  |
| --- |
| **Nota deliberada de ubicación** — este Servicio vive en el paquete publicaciones, no en investigadores, aunque recibe un Investigador como parámetro. La dirección de la dependencia importa: publicaciones ya depende de investigadores (lo consulta por su correo), nunca al revés. Si este Servicio viviera en investigadores, ese paquete tendría que conocer Publicacion — invirtiendo una dependencia que hoy es correcta. Esta es, en código, la misma pregunta que quedó abierta en el cierre del mini-curso Spring Boot. |

**4. Límite del Agregado: documentarlo, no inventarlo ~15 min**

|  |
| --- |
| **Meta:** escribir, en un párrafo corto, el límite del Agregado Investigador que el código ya respeta hoy — sin saberlo. |

Buena noticia: si completaste los Tutoriales 5 y 6 del mini-curso, tu código **ya cumple** la regla de Vernon de referenciar otros Agregados solo por identidad. Publicacion.investigadorCorreo es un String, nunca un objeto Investigador completo. Este paso no es de código — es de escribir, en tu NOTAS.md, algo como una mini-ADR que deje explícito lo que hoy es implícito:

| **Pregunta** | **Tu respuesta (para NOTAS.md)** |
| --- | --- |
| ¿Cuál es la raíz del Agregado Investigador? | Complétalo — pista: es la única clase con @Entity en el paquete. |
| ¿Qué vive dentro del límite? | Lista los campos: id, nombreCompleto, correoInstitucional (ahora un Value Object), grupoInvestigacion. |
| ¿Por qué Publicacion NO está dentro de este límite? | Un investigador puede tener muchas publicaciones — meterlas dentro del Agregado violaría la regla de Vernon de "Agregados pequeños" (guía, sección 7) y dispararía cargas y transacciones enormes por cada cambio a un investigador. |
| ¿Qué pasaría si alguien agrega un campo List<Publicacion> publicaciones directo en Investigador? | Rompería el límite: dos transacciones sobre el mismo Investigador podrían pisarse la lista de publicaciones de otra persona; el Agregado dejaría de ser pequeño; y publicaciones dejaría de poder evolucionar — o desplegarse — de forma independiente. |

**5. Factory: creación atómica de Investigador ~35 min**

|  |
| --- |
| **Meta:** mover la validación de correo y el chequeo de duplicados a un único punto de construcción — nadie más debería poder ensamblar un Investigador a medias. |

|  |
| --- |
| **investigadores/InvestigadorFactory.java** |
| **package** com.rica.ricaapi.investigadores;    **import** org.springframework.stereotype.Component;    **@Component**  **public** **class** InvestigadorFactory {    **private** **final** InvestigadorRepository investigadorRepository;    **public** InvestigadorFactory(InvestigadorRepository investigadorRepository) {  **this**.investigadorRepository = investigadorRepository;  }    **public** Investigador crear(String nombreCompleto, String correoInstitucional, String grupoInvestigacion) {  CorreoInstitucional correo = **new** CorreoInstitucional(correoInstitucional);    **if** (investigadorRepository.existsByCorreoInstitucional\_Valor(correo.valor())) {  **throw** **new** CorreoDuplicadoException(  "Ya existe un investigador registrado con el correo " + correo.valor());  }    **return** **new** Investigador(**null**, nombreCompleto, correo, grupoInvestigacion);  }  } |

Simplifica InvestigadorService para que delegue en la Factory en vez de repetir la validación:

|  |
| --- |
| **investigadores/InvestigadorService.java (después)** |
| **public** Investigador registrar(String nombreCompleto, String correoInstitucional, String grupoInvestigacion) {  Investigador investigador = investigadorFactory.crear(nombreCompleto, correoInstitucional, grupoInvestigacion);  **return** investigadorRepository.save(investigador);  } |

|  |
| --- |
| Ajusta InvestigadorController para pasar los tres campos sueltos (o el InvestigadorRequest desempaquetado) en vez de un Investigador ya armado — el punto de este paso es que **nada fuera de la Factory construye un Investigador directamente**. Actualiza también el test de registrarRechazaCorreoInstitucionalDuplicado: ahora el mock relevante es sobre InvestigadorFactory o sobre el repositorio dentro de ella, según cómo decidas testear el Servicio vs. la Factory por separado. |

**6. Confirmar el avance en Git ~5 min**

Cierra el taller con un commit — es buena práctica dejar un punto de restauración claro entre el código de partida (Tutorial 6) y el resultado del refactor DDD:

|  |
| --- |
| **terminal** |
| git add .  git commit -m "feat: refactor Investigador hacia modelo rico (Value Object, Servicio de Dominio, Factory)"  git push |

|  |
| --- |
| **Verificación**  Ejecuta git log --oneline; debe aparecer este commit sobre la rama taller-ddd, con los commits del Tutorial 6 debajo. |

**7. Reto extra (opcional): Domain Event +20–25 min**

|  |  |  |
| --- | --- | --- |
| **SOLO SI TE SOBRA TIEMPO**  Publica un evento cuando la Factory crea un investigador nuevo — sin conectarlo todavía a nada real, solo para practicar el patrón de la sección 8 de la guía:   |  | | --- | | **investigadores/InvestigadorRegistrado.java** | | **public** **record** InvestigadorRegistrado(String correoInstitucional, Instant ocurridoEn) {} |   Inyecta un ApplicationEventPublisher en InvestigadorFactory y publica el evento justo antes de retornar el Investigador recién construido. Escribe un @EventListener mínimo en cualquier clase con @Component que solo haga log.info(...). Esto no cambia nada del comportamiento observable — es exactamente la idea de consistencia eventual de la sección 8: hoy el "otro lado" del evento es un log; en la Lección 5 (Microservicios) será un consumidor real, posiblemente en otro servicio. |

**8. Checklist para tu proyecto de grupo fuera de las 3 h**

Los mismos cinco pasos de este taller — Lenguaje Ubicuo, Value Object, Servicio de Dominio, límite de Agregado, Factory — se repiten en el proyecto de grupo, pero antes de repartir subdominios entre Dev 1/2/3, el equipo corre un Event Storming (guía, sección 17) para decidir dónde están esos límites en su propio dominio, no en el de RICA. La guía completa de esta actividad de equipo, con el punto de partida de cada proyecto (G1–G9), está en el documento web **Arranque del proyecto de grupo** (proyecto-grupo.html).

|  |
| --- |
| **EJEMPLO RESUELTO: EVENT STORMING INFORMAL SOBRE RICA**  Así se vería el ejercicio aplicado al propio dominio de RICA — úsalo como plantilla, no lo copies literalmente para tu proyecto:  **1. Eventos en pasado** (lo que ya ocurre o podría ocurrir en el sistema, sin pensar todavía en clases ni tablas): Investigador registrado → Correo institucional validado → Publicación registrada → Límite anual de publicaciones alcanzado → Publicación rechazada por límite anual → Grupo de investigación actualizado.  **2. Evento pivote**: Límite anual de publicaciones alcanzado. Es pivote porque no lo puede resolver un solo "objeto" — necesita el historial de publicaciones de un investigador en un año, y de él dependen dos consecuencias con dueños distintos (aceptar o rechazar la publicación nueva). Donde aparece un evento así, suele haber un límite de contexto cerca.  **3. Bounded Contexts candidatos**, a partir de ese pivote: **Gestión de Investigadores** (todo lo que ocurre antes del pivote: registrar, validar, actualizar) y **Gestión de Publicaciones** (todo lo que depende de contar publicaciones y aplicar el límite). No es casualidad que coincidan con los paquetes investigadores y publicaciones que ya existen en rica-api — ese fue, de hecho, el razonamiento original detrás de esa división (guía, sección 1). |

**☐** Como equipo, corran un Event Storming informal (documento compartido) sobre su dominio: lista de eventos en pasado, identifiquen los eventos pivote.

**☐** A partir de los eventos pivote, propongan los Bounded Contexts candidatos de su proyecto — no antes.

**☐** Cada integrante (Grupo N · Dev 1/2/3) toma un subdominio y le aplica, sobre su propio código, los mismos cinco pasos de este taller: audita el Lenguaje Ubicuo, identifica al menos un Value Object real, identifica al menos un Servicio de Dominio real (no forzado), documenta el límite de su Agregado principal, y construye su Factory.

**☐** La integración entre subdominios se valida por revisión de pares (Pull Request) — el mismo mecanismo de Integración Continua de la guía, sección 14. Quien revisa verifica, entre otras cosas, que los demás Agregados se referencien solo por id.