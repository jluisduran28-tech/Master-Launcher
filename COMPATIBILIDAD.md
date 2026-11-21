# Compatibilidad: Android TV / Google TV y Smart TV en México

Una sola interfaz (`web/index.html`) para todas las plataformas.
- **Android TV / Google TV:** `app/` la carga en un WebView y le da acceso real a tus apps instaladas, lanzarlas y volumen del sistema.
- **Samsung (Tizen) y LG (webOS):** la misma página se empaqueta como app web (`tizen/`, `webos/`).

## 1. Qué marcas y sistemas hay en México
No encontré un ranking público confiable de participación **por marca en México**. Lo que sí hay:
- Globalmente (Counterpoint, 1T 2026): Samsung 17 %, TCL 14 %, Hisense 13 %, LG 9 %.
- TCL dijo tener poco más de 10 % en México y busca 13 % en 2026 (Expansión, ene 2026). Roku tiene acuerdos con más de 17 marcas en México (Expansión, oct 2026).
- El estudio de Profeco 2026 evaluó 49 modelos de 13 marcas: Samsung, Hisense, TCL, LG, Sony, Atvio, Aiwa, Philips, Haier, entre otras.

| Sistema | Marcas en México | Cómo corre este proyecto | ¿Reemplaza el inicio? |
|---|---|---|---|
| **Google TV / Android TV** | TCL, Sony, Xiaomi, parte de Hisense, Atvio | APK (WebView + puente nativo) | Posible por ADB, no garantizado |
| **Tizen** | Samsung | App web `.wgt` | No (solo app) |
| **webOS** | LG | App web `.ipk` | No (solo app) |
| **VIDAA** | Hisense (muchos modelos de entrada y media) | App HTML5, **no verificado** (requiere programa de desarrolladores de Hisense) | No |
| **Roku OS** | TCL, Hisense, Atvio y otras | **No reutilizable** (SceneGraph/BrightScript) | No |
| **Fire TV** | Algunos Hisense de entrada | Basado en Android; no probado | No |

## 2. Navegadores de cada TV y qué se ve
La interfaz usa flex-gap (Chromium 84+) y las propiedades `translate`/`scale` (104+).

| Samsung | Chromium | LG webOS | Chromium | Resultado |
|---|---|---|---|---|
| 2025–2026 (Tizen 9–10) | 120–130 | 25 | — | Completo |
| 2024 (Tizen 8) | 108 | 24 | 108 | Completo |
| 2023 (Tizen 7) | 94 | 23 | 94 | Completo con efectos de compatibilidad y modo ligero |
| 2022 (Tizen 6.5) | 85 | 22 | 87 | Igual que arriba |
| 2021 (Tizen 6.0) | 76 | 6.x | 79 | Debería funcionar con el polyfill de flex-gap (v4.7, sin probar en TV real) |
| 2020 y anteriores | ≤ 69 | ≤ 5.x | ≤ 68 | **No soportado** |

Fuentes: especificaciones del motor web de Samsung y de LG (developer.samsung.com, webostv.developer.lge.com). En Android el WebView se actualiza solo, pero revisa que la TV tenga uno reciente.
La app detecta el motor y activa sola el **modo ligero** (sin desenfoques, sombras ni animaciones continuas). Se puede forzar en Ajustes → Rendimiento. También hay **Márgenes de pantalla** para TV con overscan.

## 3. Instalación
### Android TV / Google TV
1. Abre `TVLauncher/` en Android Studio y sincroniza (genera el wrapper de Gradle). No pude compilar aquí: es la primera vez que se compila.
2. Activa opciones de desarrollador y depuración por red en la TV: `./scripts/connect.sh IP_DE_LA_TV`.
3. `./scripts/run.sh` compila, instala y abre.
4. **Reemplazar el inicio (opcional y bajo tu riesgo):** `./scripts/set-launcher.sh on`. Google TV y Android TV no ofrecen un selector oficial de launcher: se desactiva el de Google por ADB.
   - Reportes de 2026 indican que actualizaciones recientes de Google TV pueden volver a imponer su launcher y que en algunos controles el botón Home está atado al de Google.
   - Se pierden filas del launcher de Google como "Seguir viendo".
   - Revertir: `./scripts/set-launcher.sh off`. Instala siempre el APK **antes** de desactivar el de Google.
   - Si no funciona, ábrelo como app normal desde la lista de apps.
5. **Apagar la TV desde el temporizador (opcional):** `adb shell dpm set-active-admin com.example.tvlauncher/.AdminReceiver`. En muchas TV, bloquear la pantalla no equivale a apagar; pruébalo.

### Samsung (Tizen)
1. Activa el modo desarrollador en la TV y crea un perfil de certificado (Tizen Studio o extensión de VS Code).
2. `./scripts/package-tizen.sh MI_PERFIL IP_TV`.
3. Para publicar: certificación en Samsung TV Seller Office.
4. Las apps del sistema no son accesibles desde una app web, por lo que el menú de apps queda como **demostración** (sin lanzar apps reales).

### LG (webOS)
1. Instala el webOS TV SDK y activa el modo desarrollador en la TV.
2. `./scripts/package-webos.sh NOMBRE_DISPOSITIVO`.
3. Para publicar: revisión en LG Seller Lounge. Mismo límite sobre apps reales.

### Hisense VIDAA / Roku
No incluido. VIDAA admite apps HTML5 por su programa de desarrolladores (confirmar requisitos con Hisense). Roku requiere otro proyecto.

## 4. Lo que NO está resuelto
- **Clima, canales, avisos y red** siguen siendo de ejemplo. Hace falta conectar proveedores reales (el sintonizador de TV abierta es del fabricante).
- **Volumen:** en Android el puente usa el volumen del sistema. Con HDMI-CEC/ARC puede estar bloqueado. En Tizen/webOS, el sistema maneja el volumen y su propio indicador.
- **Modo ambiente con USB:** en Android se usa el selector de archivos del sistema (la TV debe tener un explorador de documentos). En Tizen/webOS esas opciones se ocultan.
- **Seguridad:** perfiles y PIN se guardan en el almacenamiento local sin cifrar; es control familiar básico, no seguridad fuerte.
- **Nada de esto se probó en televisores reales**, solo en un navegador de escritorio con simulaciones del puente, del motor antiguo y de Tizen.

## 5. Lista para probar en tu TV
1. Se ve completo a 1080p y 4K sin recortes (probar Márgenes de pantalla).
2. D-pad, OK, Atrás, CH+/CH−, Menú.
3. Cada estilo de menú, con 20+ apps, sin tirones.
4. Abrir y volver desde otra app.
5. Reiniciar la TV y comprobar que recuerda volumen, canal y ajustes.
6. Modo ambiente por horas (memoria y calor).

## 6. Asistente de Google
- **Android TV / Google TV:** el ícono del micrófono de la barra (o el botón del Asistente del control, si llega a la app) reproduce la animación y abre el Asistente de Google del sistema. No usa el micrófono de la app ni pide permisos. En la mayoría de las TV el botón físico lo atiende el sistema antes que la app.
- **Sin asistente instalado:** muestra un modo demostración con comandos de ejemplo (volumen, abrir app, clima, canal, temporizador, modo ambiente).
- **Samsung, LG y Hisense VIDAA:** el asistente se oculta, porque una app web no puede abrir Bixby, ThinQ ni el asistente de VIDAA.
- **Vista previa web:** es solo una demostración con comandos predefinidos; no escucha tu voz.

## 7. Ocultar el menú
- Tras 5 s sin tocar nada, la barra y el menú de apps se esconden y queda solo el fondo (se puede cambiar a 10 s, 30 s o nunca en Ajustes → "Ocultar menú sin actividad"). Cualquier botón los vuelve a mostrar (esa primera pulsación solo despierta la pantalla).
- ▼ sobre el menú de apps, cuando no hay más filas, lo esconde. ▼, OK o ◀ ▶ lo muestran de nuevo, y ▲ sube a la barra.
- En la barra, ◀ ▶ dan la vuelta: del último ícono se pasa al primero y al revés.

## 8. Temas claro y oscuro (regla 60/30/10)
- El ícono de medio círculo, a la derecha del engrane, cambia entre dos temas con una animación circular que se expande desde el botón.
- **Claro:** 60 % arena del desierto (fondo); 30 % verde oliva (barra, apps y menús); 10 % rojo racing (texto y borde del cursor).
- **Oscuro:** 60 % negro mate (fondo); 30 % gris tormenta (barra, apps y menús); 10 % naranja fuego (texto y borde del cursor).
- Ambos usan la tipografía Sora con sombra 3D y una inclinación animada en los textos grandes. Se puede apagar en Ajustes → Estilo → "Texto con sombra 3D". El interruptor "Barra y apps en color de menús" controla las superficies de color sólido.
- Si la TV no tiene internet, la fuente Sora se sustituye por la del sistema.

## 9. Ajustes nuevos (v4.5)
- **Volumen y canales con contraste:** el indicador de volumen lleva una placa del tono opuesto al fondo (oscura sobre fondos claros, clara sobre oscuros) y la tarjeta del canal activo usa el color de acento.
- **Modo infantil:** al activarlo sin PIN, pide crear uno. En Parental → "Apps permitidas en modo infantil" eliges qué apps se ven (por perfil). En modo infantil, abrir la configuración pide el PIN.
- **Wi‑Fi:** el nombre de red, la IP y la velocidad solo aparecen al pulsar "Información de Wi‑Fi".
- **Curvatura del menú** (Mínima, Suave, Media, Fuerte): aplica al cilindro (ángulo), a las cartas (abanico) y a la cuadrícula (arco).
- **Tamaño de las apps** (Pequeño, Mediano, Grande, Extra grande): en la cuadrícula cambia también las columnas (6, 5, 4, 3).

## 10. Novedades v4.6
- **Perfil en la barra:** OK corto despliega un menú (cambiar de usuario, modo infantil, ajustes de usuario, control parental) que se oculta solo a los 5 s sin tocar nada. OK largo (mantener 0.6 s) abre directamente el cambio de usuario. Salir de un perfil infantil pide el PIN parental.
- **Opacidad de fondos:** Barra → "Opacidad de fondos" (25 % a 100 %) controla el fondo de menús, volumen, canales, clima y reloj. "Opacidad de barra y apps" queda aparte.
- **Fondos y ambiente:** Ambiente → "Elegir fondo y ambiente". Fuentes: Temas del launcher (6 fondos incluidos), Sistema, USB y, en Android, el fondo del sistema (`TVBridge.systemWallpaper`, sin probar en TV real).
  - OK fija un fondo.
  - OK largo sobre un fondo activa el aleatorio solo de esa fuente.
  - Al filtrar por una sola fuente aparece una única opción de aleatorio; con "Todas", el aleatorio es de todas.
  - OK largo en "Diseño" (Estilo) elige un tema al azar, y en "Usar como fondo del launcher" activa el aleatorio.
- **Segunda pantalla (Pantalla 2):** ventana en la esquina inferior derecha para ver otro canal o app. Se abre con el ícono de la barra o la tecla `p`. Se puede cambiar, intercambiar con el canal principal y cerrar.
  - **Es una demostración de la interfaz.** Una app launcher no puede incrustar otras apps ni el sintonizador del sistema. En una TV real haría falta un reproductor propio (por ejemplo, para canales IPTV) o el PiP que implemente cada app.

## 11. v4.7: compatibilidad ampliada
Ver **REPORTE_COMPATIBILIDAD.md** (resultados de la revisión, tabla por marca y por versión de Android, y fluidez con CPU limitada). Novedades: polyfill de flex-gap, modo ligero automático según la fluidez real, aviso si el navegador es muy antiguo y recorte de tarjetas fuera de vista.
