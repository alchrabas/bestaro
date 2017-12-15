import {connect} from "react-redux";
import React from "react";
import TopBarContainer from "./TopBar";
import GoogleMapContainer from "./MapWrapper";
import Sidebar from "./Sidebar";

let AppContainer = ({dispatch, selectedRecord}) => {
    return [
        <TopBarContainer key="topBar"/>,
        <div className="row content" key="center">
            <div key="sidebar" className="sidebar">
                <Sidebar selectedRecord={selectedRecord}/>
            </div>
            <GoogleMapContainer key="googleMap"/>
        </div>
    ];
};


AppContainer = connect(state => {
    return {
        selectedRecord: state.ui.selectedRecord,
    };
})(AppContainer);

export default AppContainer;
