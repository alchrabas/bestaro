import React from "react";
import FiltersContainer from "./FiltersContainer";
import {connect} from "react-redux";
import RecordsListContainer from "./RecordsListContainer";
import RecordDetailsContainer from "./RecordDetailsContainer";
import MapCacheContainer from "./MapCacheContainer";
import HeaderContainer from "./HeaderContainer";


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
            <RecordsListContainer
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


export const WideMapPage = () => {
    return [
        <div className="row top-bar header" key="header">
            <HeaderContainer/>
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

const WideMapPageContainer = connect(
    state => ({
        wide: state.responsive.isWide,
    })
)(WideMapPage);


export default WideMapPageContainer;
