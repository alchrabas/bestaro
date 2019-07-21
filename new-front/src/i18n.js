import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import Backend from 'i18next-xhr-backend';

const languageDetector = {
    type: 'languageDetector',
    init: function(services, detectorOptions, i18nextOptions) {
    },
    detect: function() {
        if (window.location.pathname.indexOf('/en') === 0) {
            return 'en';
        }
        return 'pl';
    },
    cacheUserLanguage: function() {
    },
};

i18n
    .use(languageDetector)
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