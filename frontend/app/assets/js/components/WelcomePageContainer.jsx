import {goToMap} from "../ducks/ui";
import {connect} from "react-redux";
import {EVENT_ANY} from "../constants";
import React from "react";
import {fetchDataFromServer} from "../ducks/records";
import {changeEventTypeFilter} from "../ducks/filter";

const WelcomePage = ({goToMap}) => {
    return <div className="page-with-text">
        <div style={{overflow: "hidden"}}>
            <img className="pure-img" key="logo" src="/assets/images/kotologo-big.png"
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

const WelcomePageContainer = connect(
    state => state,
    dispatch => {
        return {
            goToMap: () => {
                dispatch(changeEventTypeFilter(EVENT_ANY));
                dispatch(fetchDataFromServer());
                dispatch(goToMap());
            },
        };
    })(WelcomePage);

export default WelcomePageContainer;
