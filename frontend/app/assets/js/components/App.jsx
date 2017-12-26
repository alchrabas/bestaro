import React from "react";
import WideLayout from "./WideLayout";
import NarrowLayout from "./NarrowLayout";
import {connect} from "react-redux";

let App = ({wide}) => {
    if (wide) {
        return <WideLayout/>;
    } else {
        return <NarrowLayout/>;
    }
};

const AppContainer = connect(state => {
    return {
        wide: state.responsive.isWide,
    }
})(App);

export default AppContainer;
