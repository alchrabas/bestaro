import React from "react";
import {connect} from "react-redux";
import {VIEW_CONTACT, VIEW_MAP, VIEW_WELCOME} from "../constants";
import WelcomePageContainer from "./WelcomePage";
import MapPageContainer from "./MapPage";
import ContactPageContainer from "./ContactPage";

let App = ({currentView}) => {
    switch (currentView) {
        case VIEW_WELCOME:
            return <WelcomePageContainer/>;
        case VIEW_MAP:
            return <MapPageContainer/>;
        case VIEW_CONTACT:
            return <ContactPageContainer/>;
    }
};

const AppContainer = connect(state => {
    return {
        currentView: state.ui.currentView,
    }
})(App);

export default AppContainer;
