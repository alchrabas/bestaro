import {connect} from "react-redux";
import React from "react";
import HeaderContainer from "./HeaderContainer";
import {withRouter} from "react-router";
import Messages from './Messages';
import kotologoBig from '../images/kotologo-big.png';

const ReadMorePage = ({wide, goToMap}) => {
    return [
        <div className="row top-bar header" key="header">
            <HeaderContainer/>
        </div>,
        <div className="page-with-text" key="content">
            <div style={{overflow: "hidden"}}>
                <img alt="Logo" key="logo" src={kotologoBig}
                     style={{float: "left", marginRight: "10px"}}/>
                <p className="page-with-text-header">
                    {Messages("read_more.header")}
                </p>
            </div>
            <div key="text" className="welcome-text"
                dangerouslySetInnerHTML={{__html: Messages("read_more.text")}}/>
            <div className="pure-g" style={{marginTop: "50px"}}>
                <div className="pure-u-1-1 pure-u-lg-1-3" style={{margin: "auto"}}>
                    <button onClick={goToMap} className="pure-button pure-button-primary big-wide-button">
                        {Messages("read_more.back_button")}
                    </button>
                </div>
            </div>
        </div>
    ];
};

const ReadMorePageContainer = withRouter(connect(
    state => {
        return {
            wide: state.responsive.isWide,
        };
    },
    (dispatch, ownProps) => {
        return {
            goToContact: () => {
                ownProps.history.push("read-more");
            },
            goToMap: () => {
                ownProps.history.push("map");
            },
        }
    },
)(ReadMorePage));

export default ReadMorePageContainer;
