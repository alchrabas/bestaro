import {applyMiddleware, combineReducers, createStore} from "redux";

import {composeWithDevTools} from "redux-devtools-extension";
import ReduxThunk from "redux-thunk";


export const CHANGE_FILTER = "CHANGE_FILTER";
export const UPDATE_RECORDS = "UPDATE_RECORDS";
export const SELECT_RECORD = "SELECT_RECORD";
export const UPDATE_MAP_BOUNDS = "UPDATE_MAP_BOUNDS";

export const changeFilter = (dateFrom, dateTo, eventType) => {
    return {
        type: CHANGE_FILTER,
        dateFrom,
        dateTo,
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

export const updateMapBounds = (northEast, southWest) => {
    return {
        type: UPDATE_MAP_BOUNDS,
        northEast,
        southWest
    };
};

const filterReducer = (state = {
    "dateFrom": null,
    "dateTo": null,
    "eventType": null
}, action) => {
    switch (action.type) {
        case CHANGE_FILTER:
            return Object.assign({}, state, {
                dateFrom: action.dateFrom,
                dateTo: action.dateTo,
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

const uiReducer = (state = {selectedRecord: null}, action) => {
    switch (action.type) {
        case SELECT_RECORD:
            return {
                selectedRecord: action.selectedRecord,
            };
        default:
            return state;
    }
};

const mapReducer = (state = {
    northEast: 0,
    southWest: 0,
}, action) => {
    switch (action.type) {
        case UPDATE_MAP_BOUNDS:
            return {
                northEast: action.northEast,
                southWest: action.southWest,
            };
        default:
            return state;
    }
};

const mainReducer = combineReducers({
    filters: filterReducer,
    records: recordsReducer,
    ui: uiReducer,
    map: mapReducer,
});

export let store = createStore(mainReducer,
    composeWithDevTools(applyMiddleware(ReduxThunk))
);
