#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${PROJECT_ROOT}"

echo "============================================================="
echo " STORYTALE DOCUMENTATION & SHOWCASE BUILDER"
echo "============================================================="

# 1. Build MkDocs Site with strict validation
echo "==> 1/3: Building MkDocs Material documentation..."
mkdocs build --strict --clean
touch site/.nojekyll
mkdir -p site/api site/gallery

# 2. Build Dokka API Reference
echo "==> 2/3: Generating Dokka API Reference..."
"${PROJECT_ROOT}/gradlew" :modules:runtime-api:dokkaGeneratePublicationHtml
"${PROJECT_ROOT}/gradlew" --stop
cp -r modules/runtime-api/build/dokka/html/* site/api/

# 3. Build Canonical Wasm Showcase Gallery
echo "==> 3/3: Building Canonical Wasm Showcase Gallery..."
"${PROJECT_ROOT}/gradlew" -PcmpProfile=1.12 :gallery-demo:composeApp:wasmJsBrowserStoriesProductionExecutableDistribution
"${PROJECT_ROOT}/gradlew" --stop
cp -r gallery-demo/composeApp/build/dist/wasmJs/StoriesProductionExecutable/* site/gallery/

echo "============================================================="
echo " Documentation build complete!"
echo " - Landing & Docs : site/index.html"
echo " - API Reference  : site/api/index.html"
echo " - Wasm Gallery   : site/gallery/index.html"
echo "============================================================="

if [[ "${1:-}" == "--serve" ]]; then
  echo "Serving documentation locally at http://localhost:8000/ (Press Ctrl+C to stop)..."
  echo " - Docs        : http://localhost:8000/"
  echo " - Dokka API   : http://localhost:8000/api/"
  echo " - Wasm Gallery: http://localhost:8000/gallery/"
  python3 -m http.server 8000 --directory site
fi
