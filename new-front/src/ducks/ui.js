import { VIEW_MAP, VIEW_PRIVACY_POLICY, VIEW_READ_MORE, VIEW_WELCOME } from '../constants';


export const SCROLL_LIST = "SCROLL_LIST";
export const CHANGE_VIEW = "CHANGE_VIEW";
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
        default:
            return state;
    }
};