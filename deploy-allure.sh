#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

echo "=== Generating Allure report ==="
if [ -x "$DIR/.allure/allure-2.29.0/bin/allure" ]; then
    "$DIR/.allure/allure-2.29.0/bin/allure" generate target/allure-results -o target/site/allure-maven-plugin --clean
elif command -v allure &> /dev/null; then
    allure generate target/allure-results -o target/site/allure-maven-plugin --clean
else
    mvn allure:report -Dallure.results.directory=target/allure-results
fi

DEPLOY_DIR="/tmp/allure-deploy"
REPO_URL=$(git config --get remote.origin.url || echo "git@github.com:bucard-tech/bucardwebauto.git")

if [ ! -d "$DEPLOY_DIR/.git" ]; then
    rm -rf "$DEPLOY_DIR"
    git clone "$REPO_URL" "$DEPLOY_DIR"
else
    git -C "$DEPLOY_DIR" remote set-url origin "$REPO_URL" || true
fi

echo "=== Copying generated report to deploy directory ==="
rm -rf "$DEPLOY_DIR"/*
cp -R target/site/allure-maven-plugin/* "$DEPLOY_DIR"/
touch "$DEPLOY_DIR/.nojekyll"

echo "=== Copying custom visual gallery assets ==="
cp "$DIR/scripts/gallery.css" "$DEPLOY_DIR/"
cp "$DIR/scripts/gallery.js" "$DEPLOY_DIR/"

echo "=== Generating gallery.json from test cases ==="
python3 "$DIR/scripts/generate_gallery.py" "$DEPLOY_DIR"

echo "=== Injecting gallery assets and customizations into index.html ==="
python3 -c "
with open('$DEPLOY_DIR/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

# Inject gallery CSS before </head>
if 'gallery.css' not in html:
    html = html.replace('</head>', '    <link rel=\"stylesheet\" type=\"text/css\" href=\"gallery.css\">\n</head>')

# Inject gallery JS before </body>
if 'gallery.js' not in html:
    html = html.replace('</body>', '    <script src=\"gallery.js\"></script>\n</body>')

with open('$DEPLOY_DIR/index.html', 'w', encoding='utf-8') as f:
    f.write(html)
"

echo "=== Deploying to gh-pages branch ==="
cd "$DEPLOY_DIR"
git checkout gh-pages || git checkout -b gh-pages
git add -A
git commit -m "deploy: update live Allure report with latest smoke suite" || true
git push origin gh-pages --force
git push git@github.com:eminmemmedov/bucardwebauto.git gh-pages --force || true

echo "=== Deployment complete! ==="
