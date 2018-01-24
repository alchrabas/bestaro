import React from "react";
import FiltersContainer from "./Filters";
import {connect} from "react-redux";
import RecordsList from "./RecordsList";
import {RecordDetailsContainer} from "./Sidebar";
import MapCacheContainer from "./MapCache";
import {goToContact} from "../store";
import WideHeader from "./WideHeader";


const SidebarForWideLayout = ({selectedRecord, listRow}) => {
    if (selectedRecord) {
        return <RecordDetailsContainer style={{flex: "0 0 40%"}}
                                       record={selectedRecord}/>;
    } else {
        return <div
            key="sidebar"
            style={{
                display: "flex",
                flexDirection: "column"
            }}
            className="sidebar">
            <FiltersContainer key="filters"/>
            <RecordsList
                style={{display: "flex", flex: 1}}
                listRow={listRow}
            />
        </div>;
    }
};

const SidebarForWideLayoutContainer = connect(state => {
        return {
            selectedRecord: state.ui.selectedRecord,
            listRow: state.ui.listRow,
        };
    }
)(SidebarForWideLayout);


const WideMapPage = ({goToContact}) => {
    return [
        <div className="row top-bar header" key="header">
            <WideHeader goToContact={goToContact}/>
        </div>,
        <div className="row content" key="center">
            <SidebarForWideLayoutContainer key="sidebar"/>
            <div className="google-map-parent">
                <MapCacheContainer key="googleMap"/>
                <div id="center-marker"/>
            </div>
        </div>
    ];
};


const WideMapPageContainer = connect(state => {
        return {
            wide: state.responsive.isWide,
        };
    },
    dispatch => {
        return {
            goToContact: () => dispatch(goToContact()),
        };
    })(WideMapPage);


export default WideMapPageContainer;
