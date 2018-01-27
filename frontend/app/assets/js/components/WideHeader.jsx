import React from "react";
import {connect} from "react-redux";
import {goToMap, goToReadMore} from "../store";

const WideHeader = ({goToMap, goToReadMore}) => {
    return [
        <img key="logo" src="/assets/images/kotologo.png"/>,
        <span key="site-name" style={{
            fontSize: "32px",
            verticalAlign: "top",
        }}>MapaZwierzat.pl</span>,
        <div key="nav-buttons" className="nav-buttons">
            <button className="pure-button" onClick={goToMap}>Mapa</button>
            <button className="pure-button" onClick={goToReadMore}>Jak to działa?</button>
        </div>,
    ];
};

const WideHeaderContainer = connect(
    state => state,
    dispatch => {
        return {
            goToMap: () => dispatch(goToMap()),
            goToReadMore: () => dispatch(goToReadMore()),
        };
    }
)(WideHeader);

export default WideHeaderContainer;
