import React from "react";
import {connect} from "react-redux";
import {goToMap, goToReadMore, goToPrivacyPolicy} from "../store";

const WideHeader = ({goToMap, goToReadMore, goToPrivacyPolicy}) => {
    return [
        <img key="logo" src="/assets/images/kotologo.png"/>,
        <span key="site-name" style={{
            fontSize: "32px",
            verticalAlign: "top",
        }}>MapaZwierzat.pl</span>,
        <div key="nav-buttons" className="nav-buttons">
            <button className="pure-button" onClick={goToMap}>{Messages("navbar.map")}</button>
            <button className="pure-button" onClick={goToReadMore}>{Messages("navbar.read_more")}</button>
            {true ? <button className="pure-button" onClick={goToReadMore}>{Messages("navbar.english")}</button>
             : <button className="pure-button" onClick={goToReadMore}>{Messages("navbar.polish")}</button>}
            <button className="pure-button" onClick={goToPrivacyPolicy}>{Messages("navbar.privacy_policy")}</button>
        </div>,
    ];
};

const WideHeaderContainer = connect(
    state => state,
    dispatch => {
        return {
            goToMap: () => dispatch(goToMap()),
            goToReadMore: () => dispatch(goToReadMore()),
            goToPrivacyPolicy: () => dispatch(goToPrivacyPolicy()),
        };
    }
)(WideHeader);

export default WideHeaderContainer;
