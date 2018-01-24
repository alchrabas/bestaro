import {connect} from "react-redux";
import React from "react";
import WideMapPage from "./WideMapPage";
import NarrowMapPageContainer from "./NarrowMapPage";

const MapPage = ({wide}) => {
    if (wide) {
        return <WideMapPage/>;
    } else {
        return <NarrowMapPageContainer/>;
    }
};


const MapPageContainer = connect(state => {
    return {
        wide: state.responsive.isWide,
    };
})(MapPage);


export default MapPageContainer;
