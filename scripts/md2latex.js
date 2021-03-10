const fs = require('fs').promises;

const makeNode = (nodeType, ...children) => {
    return {
        nodeType,
        children
    };
};

const parseMarkdown = (str) => {
    const EOF = '\0';
    let i = 0;

    const nextChar = () => {
        return str[i++];
    };

    const current = () => {
        if (i >= str.length) {
            return EOF;
        }
        return str[i];
    };

    const peek = () => {
        if (i >= str.length-1) {
            return EOF;
        }
        return str[i+1];
    };

    const isSpace = () => {
        const c = current();
        return c === ' ' || c === '\t';
    };
    
    const isWhitespace = () => {
        return isSpace() || current() == '\n';
    };
    
    const consumeWhitespace = () => {
        while (isWhitespace()) {
            nextChar();
        }
    };

    const consumeSpaces = () => {
        while (isSpace()) {
            nextChar();
        }
    };

    const isEOF = () => {
        return current() === EOF;
    };

    const readEmphasis = () => {
        nextChar();
        let sb = '';
        let isEscaped = false;
        while (!isWhitespace() && !isEOF()) {
            const c = current();
            nextChar();
            if (isEscaped) {
                isEscaped = false;
                sb += c;
                continue;
            }

            if (c == '\\') {
                isEscaped = true;
                continue;
            }

            if (isEmphasisChar(c)) {
                break;
            }

            sb += c;
        }

        return makeNode('emphasis', makeNode('text', sb));
    };

    const readBold = (delim) => {
        nextChar();
        nextChar();
        let sb = '';
        let isEscaped = false;
        while (!isWhitespace() && !isEOF()) {
            const c = current();
            nextChar();
            
            if (isEscaped) {
                isEscaped = false;
                sb += c;
                continue;
            }

            if (c == '\\') {
                isEscaped = true;
                continue;
            }

            if (isEmphasisChar(c) && isEmphasisChar(current())) {
                nextChar();
                break;
            }

            sb += c;
        }

        return makeNode('bold', makeNode('text', sb));
    };
    
    const readCode = () => {
        nextChar();
        
        let isDoubleDelimited = false;
        if (current() === '`') {
            nextChar();
            isDoubleDelimited = true;
        }
        
        let sb = '';
        while (!isWhitespace() && !isEOF()) {
            const c = current();
            if (c == '`') {
                if (isDoubleDelimited) {
                    if (peek() === '`') {
                        nextChar();
                        nextChar();
                        break;
                    }
                } else {
                    nextChar();                    
                    break;
                }
            }
            sb += c;
            nextChar();
        }

        return makeNode('code', makeNode('text', sb));
        // if (c === '`' && peek() === '`') {
        //     nextChar();
        //     nextChar();
        //     const lang = readWord();
        //     consumeSpaces();
        // }
    }

    const isBulletChar = (c) => {
        return c === '-' || c === '*';
    }

    const isEmphasisChar = (c) => {
        return c === '*' || c === '_';
    }

    const readHeading = () => {
        let count = 0;
        while (current() === '#') {
            count++;
            nextChar();
        }
        consumeSpaces();
        let text = '';
        while (current() !== '\n' && !isEOF()) {
            text += current();
            nextChar();
        }
        consumeWhitespace();

        return makeNode(`heading${count}`, makeNode('text', text));
    };
    
    const readParagraph = () => {
        const node = makeNode('paragraph');
        
        let currentText = '';
        const pushText = () => {
            if (currentText.length === 0) {
                return;
            }
            node.children.push(makeNode('text', currentText));
            currentText = '';
        };

        loop:
        while (!isEOF()) {
            let c = current();
            switch (c) {
                case '\n':
                    nextChar();
                    consumeSpaces();
                    if (current() === '\n') {
                        consumeWhitespace();
                        break loop;
                    }
                    currentText += '\n';
                    break;

                case '_':
                case '*':
                    pushText();
                    if (isEmphasisChar(peek())) {
                        node.children.push(readBold());
                    } else {
                        node.children.push(readEmphasis());
                    }
                    break;

                case '`':
                    node.children.push(readCode());
                    break;

                default:
                    currentText += c;
                    nextChar();
            }
        }

        if (currentText.length > 0) {
            pushText();
        }

        return node;
    }

    const readChunk = () => {
        consumeSpaces();
        const c = current();
        
        if (c == '#') {
            return readHeading();
        }

        if (c == '`') {
            nextChar();
            const isCodeFence = current() === '`' && peek() === '`';
            i--; // Revert to previous position.
            if (isCodeFence) {
                return readCodeFence();
            }
        }

        if (isBulletChar(c) && (isBulletChar(peek()) || peek() === ' ')) {
            return readList();
        }

        if (c === '<') {
            return readHTML();
        }

        return readParagraph();
    };
    
    const chunks = [];
    while (!isEOF()) {
        chunks.push(readChunk());
        consumeWhitespace();
    }

    return chunks;
};

let emitAllHTML;

const emitHTML = (node) => {
    out = '';
    switch (node.nodeType) {
        case 'document':
            return emitAllHTML(node.children);
        case 'text':
            out += node.children[0];
            break;
        case 'bold':
            out += `<strong>${emitAllHTML(node.children)}</strong>`;
            break;
        case 'emphasis':
            out += `<emphasis>${emitAllHTML(node.children)}</emphasis>`;
            break;
        case 'paragraph':
            out += `<p>${emitAllHTML(node.children)}</p>\n`;
            break;
        case 'heading1':
            out += `<h1>${emitAllHTML(node.children)}</h1>\n`;
            break;
        case 'heading2':
            out += `<h2>${emitAllHTML(node.children)}</h2>\n`;
            break;
        case 'heading3':
            out += `<h3>${emitAllHTML(node.children)}</h3>\n`;
            break;
        case 'heading4':
            out += `<h4>${emitAllHTML(node.children)}</h4>\n`;
            break;
        case 'heading5':
            out += `<h5>${emitAllHTML(node.children)}</h5>\n`;
            break;
        case 'heading6':
            out += `<h6>${emitAllHTML(node.children)}</h6>\n`;
            break;
        default:
            exit(1, `Unknown node type: ${node.nodeType}`);
    }
    return out;
};

emitAllHTML = (nodes) => {
    out = '';
    for (const node of nodes) {
        out += emitHTML(node);
    }
    return out;
}

const exit = (code, message) => {
    console.error(message);
    process.exit(code);
};

const main = async () => {
    const [,,path,mode] = process.argv;
    if (!path) {
        exit(1, 'Path not given');
    }

    let file;
    try {
        file = await fs.readFile(path, { encoding: 'utf-8' });
    } catch(e) {
        exit(1, `Cannot open file, ${path}: ${e.message}`);
    }

    const doc = makeNode('document');
    doc.children = parseMarkdown(file);
    
    switch(mode) {
        case '--html':
            console.log(emitHTML(doc));
            break;
        default:
            console.log(JSON.stringify(doc));
    }
};
main();
