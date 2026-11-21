# Reporte de compatibilidad (v4.7)

**Qué se probó y qué no.** Todo se probó en un Chromium de escritorio con simulaciones: User-Agent de cada TV, un motor sin las funciones modernas, CPU limitada y distintas resoluciones. **No se probó en ninguna TV real, ni se compiló el APK** (no hay Android SDK aquí). Los números de fluidez son relativos: el navegador de prueba no tiene GPU y una TV real puede comportarse mejor o peor.

## 1. Resultado de la revisión del código
- **JavaScript:** escrito en el estilo antiguo (sin flechas, `let/const`, plantillas, `?.`, `async`). No pide nada más reciente que Chromium 64 (`focus({preventScroll})`, que sin soporte solo mueve el scroll). `scripts/audit-compat.py` lo verifica.
- **CSS:** las funciones modernas tienen respaldo:
  - `gap` en flex (Chromium 84+): ahora hay un **polyfill** para Samsung/LG 2021 y WebView antiguos. Con el polyfill, comparado contra el diseño nativo, la diferencia visual fue de 0 a 2.4 % de píxeles. El máximo (2.4 %) fue en el asistente, por sus botones que se acomodan en varias líneas.
  - `translate:` y `scale:` (104+): el cursor sigue destacando mediante `transform`.
  - `overflow:clip` (90+): cae a `hidden`. `inset` ya no se usa.
  - `backdrop-filter`: si falta, solo se pierde el desenfoque.
- **Boot:** si el navegador es demasiado antiguo y la interfaz no arranca, se muestra un aviso en vez de una pantalla en negro.
- **Resoluciones:** 1024×768, 1280×720, 1366×768, 1920×1080, 2560×1440 y 3840×2160 escalan sin salirse de la pantalla.

## 2. Fluidez con CPU limitada (ms por cuadro mientras se navega; 16.7 ms = 60 fps)
| Estilo | CPU 4× más lenta, completo | CPU 4×, ligero | CPU 8×, completo | CPU 8×, ligero |
|---|---|---|---|---|
| Cilindro 3D | 68 | 17 | 143 | 30 |
| Cuadrícula | 42 | 17 | 89 | 23 |
| Cartas | 56 | 22 | 118 | 42 |

- **Modo ligero:** quita desenfoques, sombras y animaciones continuas, y reduce mucho el trabajo de dibujo.
- **Automático:** si el promedio pasa de 55 ms por cuadro en dos recorridos seguidos, activa el modo ligero solo, lo recuerda para el siguiente arranque y avisa. En la prueba se activó tras 2 recorridos con CPU 12× más lenta, y con CPU normal nunca se activó.
- **Manual:** Ajustes → Rendimiento (Automático, Completo, Ligero).
- En TV de gama baja con CPU lenta, el estilo de **cartas** es el más pesado incluso en modo ligero; ahí conviene la **cuadrícula**.

## 3. Marcas y sistemas
| Marca | Sistema típico en México | Cómo se instala | Nivel esperado |
|---|---|---|---|
| **Samsung** | Tizen (2026: Chromium 130; 2025: 120; 2024: 108; 2023: 94; 2022: 85; 2021: 76) | App web `.wgt` (`package-tizen.sh`) | 2024 en adelante: completo. 2022–2023: completo con efectos de compatibilidad. 2021: debería funcionar con los respaldos, **sin probar**. 2020 o anteriores: sin garantía |
| **LG** | webOS (24: Chromium 108; 23: 94; 22: 87; 6.x: 79) | App web `.ipk` (`package-webos.sh`) | Igual que Samsung. Algunos 2022–2023 reciben actualizaciones que suben el navegador: revisa la versión en tu TV |
| **Hisense** | VIDAA en la mayoría; Google TV, Fire TV o Roku TV en algunos modelos | Google TV: APK. VIDAA: app HTML5 con cuenta de desarrollador de VIDAA, o abrir la página alojada en el navegador de la TV (no verificado) | Google TV: completo. VIDAA: no probado, su versión de Chromium no la pude confirmar |
| **TCL** | Google TV o Roku TV | Google TV: APK. Roku: no es posible | Google TV: completo |
| **Xiaomi** | Google TV / Android TV (PatchWall en modelos antiguos) | APK | Completo en Google TV / Android TV 9 o superior |
| **Sony** | Google TV | APK | Completo |
| **Philips, Atvio, Aiwa, RCA, Philco, JVC, Panasonic, Toshiba, Haier y otras** | **No verificado por modelo**; varía entre Google TV, Android TV, Roku, Fire TV y otros | Si trae Google TV o Android TV: APK. Roku: no. Fire TV: no probado | Depende del sistema, revisa Ajustes → Acerca de |

El **modo navegador** sirve en cualquier TV con navegador moderno: se aloja `web/index.html` con HTTPS y se abre en el navegador de la TV. Es útil como prueba, pero no lista las apps reales y trae la barra del navegador. El enlace de Claude es privado, así que para esto hay que alojarlo en un sitio público.

## 4. Android TV y Google TV por versión
- **Código:** `minSdk 21` (Android 5.0). Solo usa APIs más nuevas protegidas con comprobación (`isStreamMute` API 23 y la versión del WebView API 26). Objetivo: `targetSdk 35`.
- **Declaraciones:** el manifest declara leanback no obligatorio, sin pantalla táctil, banner, categorías HOME y LEANBACK_LAUNCHER, y `<queries>` para listar apps desde Android 11.

| Versión | Qué esperar |
|---|---|
| Android TV 5–6 (API 21–23) | Lo permite el código. El WebView depende de que la TV lo actualice por Play. Si es menor a Chromium 69 la interfaz puede no arrancar |
| Android TV 7–8 (API 24–27) | Igual. Es común un WebView desactualizado en TV sin Play Store |
| Android TV 9–11 (API 28–30) | Es el caso típico de Xiaomi y TCL. WebView actualizable, funciona completo |
| Google TV, Android 12–14 (API 31–34) | Funciona. Reemplazar el inicio por ADB es lo más frágil (ver COMPATIBILIDAD.md §3) |
| Android 15 (API 35) | Compila con el SDK 35. Edge-to-edge no afecta una TV sin barras. Sin probar |

- **Aviso en la app:** si el WebView es menor a Chromium 69 (API 26+), muestra un aviso para actualizarlo.
- **Bloqueo del fabricante:** algunos fabricantes bloquean instalar o cambiar el inicio.

## 5. Limitaciones conocidas
- Roku OS: fuera de alcance. VIDAA y Fire TV: no probados.
- Samsung y LG: no pueden listar apps reales ni usar selector de archivos.
- Clima, canales, avisos, Wi‑Fi y segunda pantalla son simulados. Hace falta conectarlos a datos reales.
- Fuentes de Google Fonts: sin internet se usa la fuente del sistema.

## 6. Qué enviarme si algo falla
Marca, modelo, versión del sistema, método de instalación, una foto del problema y, en Android, el resultado de `adb logcat -d | tail -50`.
