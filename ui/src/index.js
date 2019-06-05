import React from 'react';
import ReactDOM from 'react-dom';
import { addLocale, useLocale } from 'ttag';

import App from './app';

const locale             = 'en';
const localeTranslations = require(`../i18n/${locale}.po`);
addLocale(locale, localeTranslations);
useLocale(locale);

console.log('  _____            _        _   ______          _ \n' +
    ' |  __ \\          | |      | | |  ____|        | |\n' +
    ' | |__) |___   ___| | _____| |_| |__ _   _  ___| |\n' +
    ' |  _  // _ \\ / __| |/ / _ \\ __|  __| | | |/ _ \\ |\n' +
    ' | | \\ \\ (_) | (__|   <  __/ |_| |  | |_| |  __/ |\n' +
    ' |_|  \\_\\___/ \\___|_|\\_\\___|\\__|_|   \\__,_|\\___|_|\n' +
    '                                                  \n' +
    '                                                  ');
console.log('\n' +
    '                    .   ,- To the moon, Alice !\n' +
    '                   .\'.\n' +
    '                   |o|\n' +
    '                  .\'o\'.\n' +
    '                  |.-.|\n' +
    '                  \'   \'\n' +
    '                   ( )\n' +
    '                    )\n' +
    '                   ( )\n' +
    '\n' +
    '               ____\n' +
    '          .-\'""p 8o""`-.\n' +
    '       .-\'8888P\'Y.`Y[ \' `-.\n' +
    '     ,\']88888b.J8oo_      \'`.\n' +
    '   ,\' ,88888888888["        Y`.\n' +
    '  /   8888888888P            Y8\\\n' +
    ' /    Y8888888P\'             ]88\\\n' +
    ':     `Y88\'   P              `888:\n' +
    ':       Y8.oP \'- >            Y88:\n' +
    '|          `Yb  __             `\'|\n' +
    ':            `\'d8888bo.          :\n' +
    ':             d88888888ooo.      ;\n' +
    ' \\            Y88888888888P     /\n' +
    '  \\            `Y88888888P     /\n' +
    '   `.            d88888P\'    ,\'\n' +
    '     `.          888PP\'    ,\'\n' +
    '       `-.      d8P\'    ,-\'   -RF-\n' +
    '          `-.,,_\'__,,.-\'');

ReactDOM.render(<App />, document.getElementById('app'));
