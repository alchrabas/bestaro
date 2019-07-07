import {connect} from "react-redux";
import React from "react";
import {withRouter} from "react-router";
import Messages from './Messages';
import kotologoBig from '../images/kotologo-big.png';

const WelcomePage = ({goToMap}) => {
    return <div className="page-with-text">
        <div style={{overflow: "hidden"}}>
            <img alt="Logo" className="pure-img" key="logo" src={kotologoBig}
                 style={{float: "left", marginRight: "10px"}}/>
            <p className="page-with-text-header">
                {Messages("welcome.header")}
            </p>
        </div>
        <div key="text" className="welcome-text">{Messages("welcome.text")}</div>
        <div className="pure-g" style={{marginTop: "50px"}}>
            <div className="pure-hidden-md pure-u-lg-1-4"/>
            <div className="pure-u-1 pure-u-lg-1-2">
                <button onClick={goToMap} className="pure-button pure-button-primary big-button-cta">
                    {Messages("welcome.go_to_map")}
                </button>
            </div>
            <div className="pure-hidden-md pure-u-lg-1-4"/>
        </div>
    </div>;
};

const WelcomePageContainer = withRouter(connect(
    state => state,
    (dispatch, ownProps) => {
        return {
            goToMap: () => {
                ownProps.history.push("map");
            },
        };
    })(WelcomePage));

export default WelcomePageContainer;
