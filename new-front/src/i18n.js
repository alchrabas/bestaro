import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import Backend from 'i18next-xhr-backend';

// const languageDetector = {
//     name: 'myDetectorsName',
//
//     lookup(options) {
//         // options -> are passed in options
//         return 'en';
//     },
//
//     cacheUserLanguage(lng, options) {
//         // options -> are passed in options
//         // lng -> current language, will be called after init and on changeLanguage
//
//         // store it
//     }
// };


i18n
    // .use(languageDetector)
    .use(Backend)
    .use(initReactI18next)
    .init({
        backend: {
            loadPath: '/locales/{{lng}}.json',
        },
        react: {
            useSuspense: false,
            wait: true,
        },
        keySeparator: false,
        lng: 'pl',
        defaultNS: 'translation',
        ns: ['translation'],
        fallbackLng: 'pl',
        languages: ['pl', 'en'],
        debug: true,
        interpolation: {
            escapeValue: false,
        }
    });



export default i18n;