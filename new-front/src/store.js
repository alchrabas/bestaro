import {applyMiddleware, combineReducers, createStore} from "redux";

import {composeWithDevTools} from "redux-devtools-extension";
import thunkMiddleware from "redux-thunk";
import {reducer as responsive} from 'redux-mediaquery'
import {mapReducer} from "./ducks/map";
import {fetchDataFromServer, lastMoveTimestamp, recordsReducer} from "./ducks/records";
import {filterReducer} from "./ducks/filter";
import {setLanguage, uiReducer} from "./ducks/ui";


// ask server for data when it may be outdated
setInterval(() => {
    const currentTimestamp = Date.now();
    // check it in store state
    if (currentTimestamp >= lastMoveTimestamp + 1500) {
        store.dispatch(fetchDataFromServer());
    }
}, 250);


const mainReducer = combineReducers({
    responsive,
    filters: filterReducer,
    records: recordsReducer,
    ui: uiReducer,
    map: mapReducer,
});

export let store = createStore(mainReducer,
    composeWithDevTools(applyMiddleware(thunkMiddleware))
);

if (window.location.href.endsWith("/en/")) {
    store.dispatch(setLanguage("en"));
} else {
    store.dispatch(setLanguage("pl"));
}