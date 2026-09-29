# Notas del Taller: Arquitectura Hexagonal en rica-api

Documento de auditoría, análisis conceptual y decisiones de diseño bajo el patrón de Puertos y Adaptadores (Arquitectura Hexagonal).

---

## 1. Auditoría de Puertos y Adaptadores Existentes

### ¿`InvestigadorRepository` es una interfaz, nunca una clase concreta. Según la guía, ¿es un puerto primario o secundario? Justifica con una frase: ¿quién inicia la llamada, el núcleo o algo externo?
Es un **puerto secundario** (o de salida / driven). La llamada es iniciada por el **núcleo** (casos de uso de la aplicación o fábricas) hacia la infraestructura de persistencia para consultar o guardar datos; el núcleo conduce la comunicación hacia el exterior.

### `InvestigadorController` — ¿es un adaptador primario o secundario? ¿Qué tecnología concreta envuelve?
Es un **adaptador primario** (o de entrada / driving). Recibe las solicitudes del mundo exterior e inicia la llamada hacia el caso de uso del núcleo. Envuelve la tecnología web **HTTP / REST mediante Spring MVC**, traduciendo solicitudes y payloads JSON a operaciones del dominio/aplicación.

### `InvestigadorService` hoy es una clase concreta, no una interfaz. `InvestigadorController` la llama directamente. ¿Qué pieza falta para que exista un puerto primario explícito?
Falta una **interfaz de caso de uso** (`InvestigadorUseCase`) que defina el contrato explícito de las operaciones ofrecidas por la capa de aplicación. Con este puerto primario, el controlador se desacopla de la implementación concreta del servicio.

### `InvestigadorFactory` — ¿pertenece al núcleo o a un adaptador?
Pertenece al **núcleo** (capa de aplicación/dominio). Su responsabilidad es coordinar la creación válida de entidades aplicando invariantes de negocio (como comprobar unicidad de correo institucional); no contiene dependencias de protocolos externos ni de tecnologías de infraestructura de bajo nivel.

---

## 2. Mapa de Roles y Estructura Hexagonal

| Paquete | Clases | Rol Hexagonal | Responsabilidad |
| --- | --- | --- | --- |
| `investigadores.dominio` | `Investigador`, `CorreoInstitucional`, `CorreoDuplicadoException`, `InvestigadorRegistrado` | **Núcleo (Dominio)** | Entidades, Value Objects y Eventos de negocio puros, libres de dependencias de infraestructura técnica. |
| `investigadores.aplicacion` | `InvestigadorUseCase`, `RepositorioInvestigadores`, `InvestigadorService`, `InvestigadorFactory` | **Núcleo (Aplicación)** | Casos de uso, orquestación del flujo de negocio y definición de puertos primarios y secundarios mínimos. |
| `investigadores.infraestructura.entrada.web` | `InvestigadorController`, `InvestigadorRequest`, `InvestigadorResponse`, `InvestigadorMapper` | **Adaptador Primario (Entrada)** | Traducción entre el protocolo HTTP/JSON externo y el puerto primario `InvestigadorUseCase`. |
| `investigadores.infraestructura.salida.persistencia` | `InvestigadorRepository`, `InvestigadorRepositoryJpaAdapter` | **Adaptador Secundario (Salida)** | Implementación del puerto secundario `RepositorioInvestigadores` mediante Spring Data JPA. |
| `investigadores.infraestructura.entrada.eventos` | `InvestigadorEventListener` | **Adaptador Primario (Eventos)** | Consumidor/observador de eventos de dominio generados por la aplicación. |