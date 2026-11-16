#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

echo "=== Generating Allure report ==="
mvn allure:report

DEPLOY_DIR="/tmp/allure-deploy"
if [ ! -d "$DEPLOY_DIR/.git" ]; then
    rm -rf "$DEPLOY_DIR"
    git clone git@github.com:EminMemmedov/bucardwebauto.git "$DEPLOY_DIR"
fi

echo "=== Copying generated report to deploy directory ==="
rm -rf "$DEPLOY_DIR"/*
cp -R target/site/allure-maven-plugin/* "$DEPLOY_DIR"/
touch "$DEPLOY_DIR/.nojekyll"

echo "=== Injecting behaviors redirect into index.html ==="
python3 -c "
with open('$DEPLOY_DIR/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

redirect_code = '''<script>
if (!window.location.hash || window.location.hash === '#' || window.location.hash === '#/') {
    window.location.replace('#behaviors');
}
</script>'''

if 'window.location.replace' not in html:
    html = html.replace('<head>', '<head>\n' + redirect_code)
    with open('$DEPLOY_DIR/index.html', 'w', encoding='utf-8') as f:
        f.write(html)
"

echo "=== Deploying to gh-pages branch ==="
cd "$DEPLOY_DIR"
git checkout gh-pages || git checkout -b gh-pages
git add -A
git commit -m "deploy: update live Allure report with latest smoke suite" || true
git push origin gh-pages --force

echo "=== Syncing main branch on public repo ==="
git checkout main
git reset --hard gh-pages
git push origin main --force

echo "=== Deployment complete! ==="
