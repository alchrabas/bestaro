import {connect} from "react-redux";
import {goToPrivacyPolicy, goToMap} from "../store";
import React from "react";
import HeaderContainer from "./HeaderContainer";


const PrivacyPolicyPage = ({wide, goToMap}) => {
    return [
        <div className="row top-bar header" key="header">
            <HeaderContainer/>
        </div>,
        <div className="page-with-text" key="content">
            <div style={{overflow: "hidden"}}>
                <p className="page-with-text-header">
                    {Messages("privacy_policy.header")}
                </p>
            </div>
            <div key="text" className="welcome-text"
                 dangerouslySetInnerHTML={{__html: Messages("privacy_policy.text")}}/>
            <div className="pure-g" style={{marginTop: "20px"}}>
                <div className="pure-u-1-1 pure-u-lg-1-3" style={{margin: "auto"}}>
                    <button onClick={goToMap} className="pure-button pure-button-primary big-wide-button">
                        {Messages("privacy_policy.back_button")}</button>
                </div>
            </div>
        </div>
    ];
};

const PrivacyPolicyPageContainer = connect(
    state => {
        return {
            wide: state.responsive.isWide,
        };
    },
    dispatch => {
        return {
            goToContact: () => {
                dispatch(goToPrivacyPolicy())
            },
            goToMap: () => {
                dispatch(goToMap());
            },
        }
    },
)(PrivacyPolicyPage);

export default PrivacyPolicyPageContainer;
