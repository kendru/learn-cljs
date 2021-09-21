#!/usr/bin/env node

let table = null;
let processingTable = null;

const processTable = (line) => {
    let cells = line.
        split('|')
        .map(chars => chars.trim());
    // Strip extra elements from the leading and trailing pipe characters
    cells.shift();
    cells.pop();

    switch (processingTable) {
        case null:
            table = {
                header: cells,
                rows: [],
            };
            processingTable = 'divider';
            break;
            
        case 'divider':
            processingTable = 'body';
            return;
            
        case 'body':
            table.rows.push(cells);
            break;
    }
};

const repeatChar = (c, n) => {
    let out = '';
    for (let i = 0; i < n; i++) {
        out += c;
    }
    return out;
}

const fmtDivider = (char, lengths) =>
    `+${(lengths.map(n => repeatChar(char, n+2))).join('+')}+`;

const fmtRow = (fields, lengths) =>
    `| ${fields.map((field, i) => field + repeatChar(' ', lengths[i]-field.length)).join(' | ')} |`;

const emitTable = () => {
    let cellContentLength = [];
    for (const headerCell of table.header) {
        cellContentLength.push(headerCell.length);
    }
    for (const row of table.rows) {
        for (let i = 0; i < row.length; i++) {
            const cell = row[i];
            cellContentLength[i] = Math.max(cellContentLength[i], cell.length);
        }
    }

    // Print table.
    console.log(fmtDivider('-', cellContentLength));
    console.log(fmtRow(table.header, cellContentLength));
    console.log(fmtDivider('=', cellContentLength));
    for (const row of table.rows) {
        console.log(fmtRow(row, cellContentLength));
        console.log(fmtDivider('-', cellContentLength));
    }

    table = null;
    processingTable = null;
}

let processingFigure = false;

const processLine = (line) => {
  // Format tables as grid tables for better formatting.
  if (line.startsWith('|')) {
    processTable(line);
    return;
  }
  if (processingTable !== null) {
    emitTable();
  }

  // Strip duplicate figure caption.
  if (line.startsWith('![')) {
    processingFigure = true;
  }
  if (processingFigure && line.trim().length > 0) {
    if (line.startsWith('_') || line.startsWith('*')) {
      processingFigure = false;
      return;
    }
  }

  // Format lesson titles.
  if (/^# Lesson \d+:/.test(line)) {
    const lessonTitle = line.split(':')[1].trim();
    console.log(`\\chapter{${lessonTitle}}`);
    return;
  }

  // Format section intros.
  if (/^# Section \d+:/.test(line)) {
    const sectionTitle = line.split(':')[1].trim();
    console.log(`\\cleardoublepage\n{\\let\\newpage\\relax\\part{${sectionTitle}}}`);
    return;
  }

  // Normal line - output as usual.
  console.log(line);
};

let lineBuf = "";
const flushLine = () => {
  processLine(lineBuf);
  lineBuf = "";
};

const processChunk = (input) => {
  for (const char of input) {
    if (char === "\n") {
      flushLine();
    } else {
      lineBuf += char;
    }
  }
  if (lineBuf.length > 0) {
    flushLine();
  }
};

process.stdin.on("data", processChunk);

process.stdin.on("end", () => {
  process.exit(0);
});

process.stdin.resume();
process.stdin.setEncoding("utf-8");
