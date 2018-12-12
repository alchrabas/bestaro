'use strict';

import {changeDateFilter, store} from "./store";
import React from "react";
import ReactDOM from "react-dom";
import {Provider} from "react-redux";
import 'purecss/build/pure-min.css';
import 'purecss/build/grids-responsive-min.css';
import 'react-virtualized/styles.css';
import "../stylesheets/main.scss";
import AppContainer from "./components/AppContainer";
import {dateToString} from "./utils";

import {EVENT_ANY} from "./constants";
import {mediaQueryTracker} from "redux-mediaquery";


const weekAgoDate = new Date();
weekAgoDate.setDate(weekAgoDate.getDate() - 7);

store.dispatch(changeDateFilter(
    dateToString(weekAgoDate),
    dateToString(new Date()),
    EVENT_ANY
));

mediaQueryTracker({
    isWide: "screen and (min-width: 1025px)",
    innerWidth: true,
    innerHeight: true,
}, store.dispatch);


ReactDOM.render(
    <Provider store={store}>
        <AppContainer/>
    </Provider>
    ,
    document.getElementById('root')
);
