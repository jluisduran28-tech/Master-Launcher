#!/bin/bash
# La interfaz vive en web/index.html (única fuente). Android la toma directo de ../web.
# Este script solo prueba que cargue en un navegador local.
python3 -m http.server 8080 --directory web
