#!/bin/bash

##
## mkpdf: eBook generator.
## 
## This script is used to generate the eBook formats of
## Learn ClojureScript from the Markdown sournces and assets.
##
## To run on ubuntu:
## sudo apt-get install pandoc texlive-xetex
## 

set -e
set -x

BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." >/dev/null 2>&1 && pwd )"
CONTENT_DIR="${BASE_DIR}/content"
RESOURCE_DIR="${BASE_DIR}/static"
SCRIPT_DIR="${BASE_DIR}/scripts"
TMP_DIR="$(mktemp -d)"
OUT_PDF="${1:-${RESOURCE_DIR}/pdf/learn-clojurescript_$(date +%Y-%-m-%d).pdf}"
OUT_EPUB="${1:-${RESOURCE_DIR}/pdf/learn-clojurescript_$(date +%Y-%-m-%d).epub}"

cleanup() {
    rm -rf "$TMP_DIR"
}
trap cleanup EXIT

convert_file() {
    if grep -q 'draft: true' "$1" ; then
        echo "Skipping draft: $1"
    else
        outfile="${TMP_DIR}/$(echo "${1#"${CONTENT_DIR}/"}" | sed -r -e 's/\//-/g' -e 's/_index/00index/g' -e 's/lesson-([0-9])-/lesson-0\1-/g')"
        # 1. make the absolute URL paths for resources into relative paths
        # 2. Convert KaTeX shortcode to embedded LaTeX math mode
        # 3. strip front matter
        # 4. output file
        echo "Processing ${outfile}"
        cat "$1" \
            | sed 's/\/img/img/g' \
            | awk 'BEGIN{c=0} /katex/{c=1} {if (c==1) print "\$"$0"\$"} {if (c==0) print $0} /\/katex/{c=0}' \
            | sed '/katex/d' \
            | perl -ne 'if ($i > 1) { print } else { /^---/ && $i++ }' \
            > "$outfile"
        echo "DONE processing ${outfile}"
    fi
}

preprocess_for_pdf() {
    convert_tmpdir="${TMP_DIR/convert}"
    mkdir -p "$convert_tmpdir"
    tmpfile="$(mktemp -p "$convert_tmpdir")"
    cat "$1" | "${SCRIPT_DIR}/preprocess-markdown.js" > "${tmpfile}"
    mv "${tmpfile}" "${1}"
}

export CONTENT_DIR TMP_DIR RESOURCE_DIR SCRIPT_DIR
export -f convert_file
export -f preprocess_for_pdf

cat <<HERE > "${TMP_DIR}/latex-header-extra.tex"
\setmainfont[
    Path = ${RESOURCE_DIR}/fonts/PT_Serif/,
    BoldFont = PTSerif-Bold,
    ItalicFont = PTSerif-Italic,
    BoldItalicFont = PTSerif-BoldItalic
]{PTSerif-Regular}
\setsansfont[
    Path = ${RESOURCE_DIR}/fonts/Oswald/,
    BoldFont = Oswald-Bold,
    ItalicFont = Oswald-Light,% not actual italic font
    BoldItalicFont = Oswald-SemiBold% not actual italic font
]{Oswald-Regular}
\setmonofont[
    Path = ${RESOURCE_DIR}/fonts/Fira_Code/,
    Scale = 0.7,
    BoldFont = FiraCode-Bold
]{FiraCode-Regular}

\usepackage{float}
\let\origfigure\figure
\let\endorigfigure\endfigure
\renewenvironment{figure}[1][2] {
    \expandafter\origfigure\expandafter[H]
} {
    \endorigfigure
}
HERE

find $CONTENT_DIR/section* -type f -name '*.md' -exec bash -c 'convert_file "$0"' {} \;

mkdir -p "$(dirname $OUT_PDF)" "$(dirname $OUT_EPUB)"

mkdir -p "${TMP_DIR}/epub" "${TMP_DIR}/pdf"
cp ${TMP_DIR}/*.md "${TMP_DIR}/epub/"
mv ${TMP_DIR}/*.md "${TMP_DIR}/pdf/"


# Generate EPUB.
"$SCRIPT_DIR/preprocess-epub-dir.js" "${TMP_DIR}/epub"
mv ${TMP_DIR}/epub/*.md "${TMP_DIR}/"
pandoc "${BASE_DIR}/metadata.txt" ${TMP_DIR}/*.md \
    --file-scope \
    --resource-path "$RESOURCE_DIR" \
    --css "${BASE_DIR}/scripts/templates/epub.css" \
    --toc \
    --toc-depth 1 \
    -o "$OUT_EPUB"

# Generate PDF.
find "${TMP_DIR}/pdf" -type f -name '*.md' -exec bash -c 'preprocess_for_pdf "$0"' {} \;
mv ${TMP_DIR}/pdf/*.md "${TMP_DIR}/"
pandoc ${TMP_DIR}/*.md \
    --file-scope \
    --resource-path "$RESOURCE_DIR" \
    --template="${BASE_DIR}/scripts/templates/default.latex" \
    --include-in-header="${TMP_DIR}/latex-header-extra.tex" \
    --toc \
    --toc-depth 2 \
    --pdf-engine=xelatex \
    -V geometry:margin=1.25in \
    -o "$OUT_PDF"

