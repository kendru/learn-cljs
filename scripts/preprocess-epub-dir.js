#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

const dir = process.argv[2];

const files = fs.readdirSync(dir);
files.sort();


const zeroPad = (input) => {
    input = input+'';
    switch (input.length) {
        case 1:
            return `00${input}`;
        case 2:
            return `0${input}`;
        default:
            return input;
    }
};

const fileIndex = {};

const lessonFilePattern = /^(section-\d+)-(lesson-\d+-[^.]+)\.md/;
for (const i in files) {
    const file = files[i];
    console.error(file);
    const matches = file.match(lessonFilePattern);
    if (!matches || matches.length === 0) {
        continue;
    }
    let [, sectionPortion, lessonPortion] = matches;
    lessonPortion = lessonPortion.replace(/lesson-0(\d)-/, 'lesson-$1-');
    fileIndex[`/${sectionPortion}/${lessonPortion}`] = `ch${zeroPad(parseInt(i)+1)}.xhtml`;
}

for (const file of files) {
    const filepath = path.join(dir, file);
    let content = fs.readFileSync(filepath, 'utf-8');
    content = content.replaceAll(/\((\/section-\d+\/[^)#/]+)\/?(?:#([^)]+))?\)/g, (match, file, anchor) => {
        let out = match;
        for (const [oldFile, newFile] of Object.entries(fileIndex)) {
            if (file.startsWith(oldFile)) {
                out = `(${newFile}${file.substring(oldFile.length)}${anchor ? `#${anchor}` : ''})`;
                break;
            }
        }
        if (out === match) {
            throw new Error(`No replacement found: ${match}`);
        }

        return out;
    });

    fs.writeFileSync(filepath, content, 'utf-8');
}

