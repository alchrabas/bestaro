export const UPDATE_RECORDS = "UPDATE_RECORDS";

export const updateRecords = records => {
    return {
        type: UPDATE_RECORDS,
        records,
    };
};


export let lastMoveTimestamp = Infinity;

export const updateLastMoveTimestamp = () => lastMoveTimestamp = Date.now(); // TODO put into redux state

export const fetchDataFromServer = () => {
    return (dispatch, getState) => {
        console.log("FETCHING DATA FROM SERVER");
        const center = getState().map.center;
        const filters = getState().filters;
        fetch(`/api/${center.lat}/${center.lng}/`
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

export const recordsReducer = (state = [], action) => {
    switch (action.type) {
        case UPDATE_RECORDS:
            return action.records;
        default:
            return state;
    }
};
