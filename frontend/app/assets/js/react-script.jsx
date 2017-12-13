import {tag, dateToString} from "./utils";
import {connect} from "react-redux";
import React from "react";

import {changeFilter, store, updateRecords} from "./store";
import TopBarContainer from "./components/TopBar";
import GoogleMapContainer from "./components/MapWrapper";
import Sidebar from "./components/Sidebar";
import {EVENT_ANY} from "./constants";

let AppContainer = ({dispatch, selectedRecord}) => {
    return [
        tag(TopBarContainer, {key: "topBar"}),
        tag("div", {className: "row content", key: "center"},
            [
                tag("div", {
                        key: "sidebar",
                        className: "sidebar"
                    },
                    tag(Sidebar, {selectedRecord: selectedRecord})),
                tag(GoogleMapContainer, {
                    key: "googleMap",
                })
            ],
        ),
    ];
};


export default AppContainer = connect((state, ownProps) => {
    return {
        selectedRecord: state.ui.selectedRecord,
    };
})(AppContainer);


export const fetchDataFromServer = () => {
    return (dispatch, getState) => {
        console.log("FETCHING DATA FROM SERVER");
        const boundsNE = getState().map.northEast;
        const boundsSW = getState().map.southWest;
        const filters = getState().filters;
        fetch(`/rest/${boundsSW.lat()}/${boundsSW.lng()}/${boundsNE.lat()}/${boundsNE.lng()}/`
            + `${filters.dateFrom}/${filters.dateTo}/${filters.eventType}`)
            .then(response => response.json())
            .then(data =>
                dispatch(updateRecords(data))
            ).catch(e => {
            console.log("Error when trying to fetch data", e);
        });
    };
};


const weekAgoDate = new Date();
weekAgoDate.setDate(weekAgoDate.getDate() - 7);

store.dispatch(changeFilter(
    dateToString(weekAgoDate),
    dateToString(new Date()),
    EVENT_ANY
));
