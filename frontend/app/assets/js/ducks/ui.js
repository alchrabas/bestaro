import {VIEW_MAP, VIEW_PRIVACY_POLICY, VIEW_READ_MORE, VIEW_WELCOME} from "../constants";


export const SCROLL_LIST = "SCROLL_LIST";
export const CHANGE_VIEW = "CHANGE_VIEW";
export const SET_LANGUAGE = "SET_LANGUAGE";
export const SELECT_RECORD = "SELECT_RECORD";

export const scrollList = (rowNumber) => {
    return {
        type: SCROLL_LIST,
        listRow: rowNumber,
    };
};

export const goToMap = () => {
    return {
        type: CHANGE_VIEW,
        view: VIEW_MAP,
    };
};

export const goToReadMore = () => {
    return {
        type: CHANGE_VIEW,
        view: VIEW_READ_MORE,
    };
};

export const goToPrivacyPolicy = () => {
    return {
        type: CHANGE_VIEW,
        view: VIEW_PRIVACY_POLICY,
    };
};

export const switchLanguage = (language) => {
    if (language === "en") {
        window.location.replace("/en/");
    } else {
        window.location.replace("/");
    }
};

export const setLanguage = (language) => {
    return {
        type: SET_LANGUAGE,
        language,
    };
};

export const selectRecord = recordId => {
    return (dispatch, getState) => {
        const selectedRecord = getState().records.filter(r => r.id === recordId)[0];
        dispatch({type: SELECT_RECORD, selectedRecord: selectedRecord});
    }
};

export const deselectRecord = () => {
    return {type: SELECT_RECORD, selectedRecord: null};
};

export const uiReducer = (state = {
    selectedRecord: null,
    listRow: null,
    currentView: VIEW_WELCOME,
    language: "pl",
}, action) => {
    switch (action.type) {
        case SELECT_RECORD:
            return Object.assign({}, state, {
                selectedRecord: action.selectedRecord,
            });
        case SCROLL_LIST:
            return Object.assign({}, state, {
                listRow: action.listRow,
            });
        case CHANGE_VIEW:
            return Object.assign({}, state, {
                currentView: action.view
            });
        case SET_LANGUAGE:
            return Object.assign({}, state, {
                language: action.language
            });
        default:
            return state;
    }
};