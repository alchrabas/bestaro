import {connect} from "react-redux";
import React from "react";
import WideMapPage from "./WideMapPageContainer";
import NarrowMapPageContainer from "./NarrowMapPageContainer";

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
