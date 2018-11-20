import React from "react";
import {connect} from "react-redux";
import {VIEW_READ_MORE, VIEW_MAP, VIEW_WELCOME, VIEW_PRIVACY_POLICY} from "../constants";
import WelcomePageContainer from "./WelcomePage";
import MapPageContainer from "./MapPage";
import ReadMorePageContainer from "./ReadMorePage";
import PrivacyPolicyPageContainer from "./PrivacyPolicyPage";

let App = ({currentView}) => {
    switch (currentView) {
        case VIEW_WELCOME:
            return <WelcomePageContainer/>;
        case VIEW_MAP:
            return <MapPageContainer/>;
        case VIEW_READ_MORE:
            return <ReadMorePageContainer/>;
        case VIEW_PRIVACY_POLICY:
            return <PrivacyPolicyPageContainer/>;
    }
};

const AppContainer = connect(state => {
    return {
        currentView: state.ui.currentView,
    }
})(App);

export default AppContainer;
