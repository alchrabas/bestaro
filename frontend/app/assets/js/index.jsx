'use strict';

import {fetchDataFromServer, changeFilter, store} from "./store";
import React from "react";
import ReactDOM from "react-dom";
import {Provider} from "react-redux";
import 'purecss/build/pure-min.css';
import 'purecss/build/grids-responsive-min.css';
import 'react-virtualized/styles.css';
import "../stylesheets/main.scss";
import AppContainer from "./components/App";
import {dateToString} from "./utils";

import {EVENT_ANY} from "./constants";


const weekAgoDate = new Date();
weekAgoDate.setDate(weekAgoDate.getDate() - 7);

store.dispatch(changeFilter(
    dateToString(weekAgoDate),
    dateToString(new Date()),
    EVENT_ANY
));


ReactDOM.render(
    <Provider store={store}>
        <AppContainer/>
    </Provider>
    ,
    document.getElementById('root')
);


let lastMoveTimestamp = Date.now();
// create action creator
const updateLastMoveTimestamp = () => lastMoveTimestamp = Date.now(); // todo move to redux


// ask server for data when it may be outdated
setInterval(() => {
    const currentTimestamp = Date.now();
    if (currentTimestamp >= lastMoveTimestamp + 3000) {
        store.dispatch(fetchDataFromServer());
        lastMoveTimestamp = Infinity;
    }
}, 1000);

setTimeout(() => store.dispatch(fetchDataFromServer()), 1000);

