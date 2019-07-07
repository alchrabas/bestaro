import {EVENT_ANY} from "../constants";

export const CHANGE_DATE_FILTER = "CHANGE_DATE_FILTER";
export const CHANGE_EVENT_TYPE_FILTER = "CHANGE_EVENT_TYPE_FILTER";

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

export const filterReducer = (state = {
    "dateFrom": null,
    "dateTo": null,
    "eventType": EVENT_ANY,
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