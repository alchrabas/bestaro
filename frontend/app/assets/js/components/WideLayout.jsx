import GoogleMapContainer from "./MapWrapper";
import SidebarContainer from "./Sidebar";
import * as React from "react";
import TopBarContainer from "./TopBar";


const WideLayout = () => {
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

export default WideLayout;
