import { updateLastMoveTimestamp } from './records';

export const UPDATE_MAP_BOUNDS = 'UPDATE_MAP_BOUNDS';
export const REFRESH_MAP = 'REFRESH_MAP';


export const updateMapBounds = bounds => {
    updateLastMoveTimestamp();
    return {
        type: UPDATE_MAP_BOUNDS,
        bounds,
    };
};

export const refreshMap = () => {
    return {
        type: REFRESH_MAP,
    };
};

export const mapReducer = (state = {
    bounds: { minLat: 0, minLon: 0, maxLat: 0, maxLon: 0 },
    refreshSerialId: 0,
}, action) => {
    switch (action.type) {
        case UPDATE_MAP_BOUNDS:
            return Object.assign({}, state, {
                bounds: action.bounds,
            });
        case REFRESH_MAP:
            return Object.assign({}, state, {
                refreshSerialId: state.refreshSerialId + 1,
            });
        default:
            return state;
    }
};