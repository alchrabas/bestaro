import {changeEventTypeFilter, goToMap} from "../store";
import {connect} from "react-redux";
import {EVENT_FOUND, EVENT_LOST} from "../constants";
import * as React from "react";

const WelcomePage = ({goToLost, goToFound}) => {
    return <div className="welcome-page">
        <div style={{overflow: "hidden"}}>
            <img key="logo" src="/assets/images/kotologo-big.png"
                 style={{float: "left", marginRight: "10px"}}/>
            <p className="welcome-header">
                {Messages("welcome_header")}
            </p>
        </div>
        <div key="text" className="welcome-text">{Messages("welcome_text")}</div>
        <div className="pure-g" style={{marginTop: "50px"}}>
            <div className="pure-hidden-md pure-u-lg-1-12"/>
            <div className="pure-u-11-24 pure-u-lg-1-3">
                <button onClick={goToLost} className="pure-button big-button-lost">Poszukiwane</button>
            </div>
            <div className="pure-u-1-12 pure-u-lg-1-6"/>
            <div className="pure-u-11-24 pure-u-lg-1-3">
                <button onClick={goToFound} className="pure-button big-button-found">Znalezione</button>
            </div>
            <div className="pure-hidden-md pure-u-md-1-12"/>
        </div>
    </div>;
};

const WelcomePageContainer = connect(
    state => state,
    dispatch => {
        return {
            goToLost: () => {
                dispatch(changeEventTypeFilter(EVENT_LOST));
                dispatch(goToMap());
            },
            goToFound: () => {
                dispatch(changeEventTypeFilter(EVENT_FOUND));
                dispatch(goToMap());
            },
        };
    })(WelcomePage);

export default WelcomePageContainer;
