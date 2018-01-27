import {applyMiddleware, combineReducers, createStore} from "redux";

import {composeWithDevTools} from "redux-devtools-extension";
import ReduxThunk from "redux-thunk";
import {reducer as responsive} from 'redux-mediaquery'
import {VIEW_READ_MORE, VIEW_MAP, VIEW_WELCOME} from "./constants";


export const CHANGE_DATE_FILTER = "CHANGE_DATE_FILTER";
export const CHANGE_EVENT_TYPE_FILTER = "CHANGE_EVENT_TYPE_FILTER";
export const UPDATE_RECORDS = "UPDATE_RECORDS";
export const SELECT_RECORD = "SELECT_RECORD";
export const UPDATE_MAP_CENTER = "UPDATE_MAP_CENTER";
export const SCROLL_LIST = "SCROLL_LIST";
export const CHANGE_VIEW = "CHANGE_VIEW";
export const REFRESH_MAP = "REFRESH_MAP";

export const changeDateFilter = (dateFrom, dateTo) => {
    return {
        type: CHANGE_DATE_FILTER,
        dateFrom,
        dateTo,
    };
};

export const changeEventTypeFilter = (eventType) => {
    return {
        type: CHANGE_EVENT_TYPE_FILTER,
        eventType,
    };
};

export const updateRecords = records => {
    return {
        type: UPDATE_RECORDS,
        records,
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

let lastMoveTimestamp = Infinity;
// create action creator
const updateLastMoveTimestamp = () => lastMoveTimestamp = Date.now(); // todo move to redux, but for now ugly will work


// ask server for data when it may be outdated
setInterval(() => {
    const currentTimestamp = Date.now();
    if (currentTimestamp >= lastMoveTimestamp + 1500) {
        store.dispatch(fetchDataFromServer());
    }
}, 250);


export const updateMapCenter = center => {
    updateLastMoveTimestamp();
    return {
        type: UPDATE_MAP_CENTER,
        center,
    };
};

export const refreshMap = () => {
    return {
        type: REFRESH_MAP,
    };
};


export const fetchDataFromServer = () => {
    return (dispatch, getState) => {
        console.log("FETCHING DATA FROM SERVER");
        const center = getState().map.center;
        const filters = getState().filters;
        fetch(`/rest/${center.lat}/${center.lng}/`
            + `${filters.dateFrom}/${filters.dateTo}/${filters.eventType}`)
            .then(response => response.json())
            .then(data =>
                dispatch(updateRecords(data))
            ).catch(e => {
            console.log("Error when trying to fetch data", e);
        });
        lastMoveTimestamp = Infinity;
    };
};


const filterReducer = (state = {
    "dateFrom": null,
    "dateTo": null,
    "eventType": null
}, action) => {
    switch (action.type) {
        case CHANGE_DATE_FILTER:
            return Object.assign({}, state, {
                dateFrom: action.dateFrom,
                dateTo: action.dateTo,
            });
        case CHANGE_EVENT_TYPE_FILTER:
            return Object.assign({}, state, {
                eventType: action.eventType,
            });
        default:
            return state;
    }
};


const recordsReducer = (state = [], action) => {
    switch (action.type) {
        case UPDATE_RECORDS:
            return action.records;
        default:
            return state;
    }
};

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

export const goToWelcome = () => {
    return {
        type: CHANGE_VIEW,
        view: VIEW_WELCOME,
    };
};

export const goToReadMore = () => {
    return {
        type: CHANGE_VIEW,
        view: VIEW_READ_MORE,
    };
};

const uiReducer = (state = {
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

const mapReducer = (state = {
    center: {lat: 0, lng: 0},
    refreshSerialId: 0,
}, action) => {
    switch (action.type) {
        case UPDATE_MAP_CENTER:
            return Object.assign({}, state, {
                center: action.center,
            });
        case REFRESH_MAP:
            return Object.assign({}, state, {
                refreshSerialId: state.refreshSerialId + 1,
            });
        default:
            return state;
    }
};

const mainReducer = combineReducers({
    responsive,
    filters: filterReducer,
    records: recordsReducer,
    ui: uiReducer,
    map: mapReducer,
});

export let store = createStore(mainReducer,
    composeWithDevTools(applyMiddleware(ReduxThunk))
);
