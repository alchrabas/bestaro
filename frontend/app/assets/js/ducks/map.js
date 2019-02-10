import {updateLastMoveTimestamp} from "./records";

export const UPDATE_MAP_CENTER = "UPDATE_MAP_CENTER";
export const REFRESH_MAP = "REFRESH_MAP";


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

export const mapReducer = (state = {
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