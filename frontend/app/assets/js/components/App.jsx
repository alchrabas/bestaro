import React from "react";
import TopBarContainer from "./TopBar";
import GoogleMapContainer from "./MapWrapper";
import SidebarContainer from "./Sidebar";

let AppContainer = () => {
    return [
        <TopBarContainer key="topBar"/>,
        <div className="row content" key="center">
            <div key="sidebar" className="sidebar">
                <SidebarContainer/>
            </div>
            <GoogleMapContainer key="googleMap"/>
        </div>
    ];
};

export default AppContainer;
