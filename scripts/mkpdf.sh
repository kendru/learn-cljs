#!/bin/bash
set -e

BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." >/dev/null 2>&1 && pwd )"
CONTENT_DIR="${BASE_DIR}/content"
RESOURCE_DIR="${BASE_DIR}/static"
TMP_DIR="$(mktemp -d)"
OUT_FILE="${1:-${RESOURCE_DIR}/pdf/learn-clojurescript_$(date +%Y-%-m-%d).pdf}"

cleanup() {
    rm -rf "$TMP_DIR"
}
trap cleanup EXIT

convert_file() {
    if grep -q 'draft: true' "$1" ; then
        echo "Skipping draft: $1"
    else
        outfile="${TMP_DIR}/$(echo "${1#"${CONTENT_DIR}/"}" | sed -e 's/\//-/g' -e 's/_index/00index/g')"
        # 1. make the absolute URL paths for resources into relative paths
        # 2. Convert KaTeX shortcode to embedded LaTeX math mode
        # 3. strip front matter
        # ... TODO: Remove links to other sections
        # 4. output file
        cat $1 \
            | sed 's/\/img/img/g' \
            | awk 'BEGIN{c=0} /katex/{c=1} {if (c==1) print "\$"$0"\$"} {if (c==0) print $0} /\/katex/{c=0}' \
            | sed '/katex/d' \
            | perl -ne 'if ($i > 1) { print } else { /^---/ && $i++ }' \
            > "$outfile"
    fi
}
export CONTENT_DIR TMP_DIR RESOURCE_DIR
export -f convert_file

cat <<HERE > "${TMP_DIR}/fontoptions.tex"
\setmainfont[
    Path = ${RESOURCE_DIR}/fonts/PT_Serif/,
    BoldFont = PTSerif-Bold,
    ItalicFont = PTSerif-Italic,
    BoldItalicFont = PTSerif-BoldItalic
]{PTSerif-Regular}
\setmonofont[
    Path = ${RESOURCE_DIR}/fonts/Fira_Code/,
    Scale = 0.7,
    BoldFont = FiraCode-Bold
]{FiraCode-Regular}
HERE

find $CONTENT_DIR/section* -type f -name '*.md' -exec bash -c 'convert_file "$0"' {} \;

mkdir -p "$(dirname $OUT_FILE)"
find $TMP_DIR -type f -print \
    | sort -V \
    | xargs pandoc \
        -f markdown \
        --file-scope \
        --toc \
        --toc-depth 2 \
        -V geometry:margin=1.25in \
        -V fontsize=10pt \
        -V documentclass=report \
        -fmarkdown-implicit_figures \
        --dpi 300 \
        --include-in-header="${TMP_DIR}/fontoptions.tex" \
        --resource-path "$RESOURCE_DIR" \
        -t latex \
        --pdf-engine=xelatex \
        -o "$OUT_FILE"