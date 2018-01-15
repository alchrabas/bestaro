import GoogleMapContainer from "./MapWrapper";
import * as React from "react";
import TopBarContainer from "./TopBar";
import {connect} from "react-redux";
import RecordsList from "./RecordsList";
import {RecordDetailsContainer} from "./Sidebar";
import MapCacheContainer from "./MapCache";

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
            <div key="text"
                 dangerouslySetInnerHTML={{__html: Messages("welcome_text")}}/>
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


const WideLayout = () => {
    return [
        <TopBarContainer key="topBar"/>,
        <div className="row content" key="center">
            <SidebarForWideLayoutContainer/>
            <div className="google-map-parent">
                <MapCacheContainer key="googleMap"/>
                <div id="center-marker"/>
            </div>
        </div>
    ];
};

export default WideLayout;
