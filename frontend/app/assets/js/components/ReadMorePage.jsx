import {connect} from "react-redux";
import {goToReadMore, goToMap} from "../store";
import React from "react";
import WideHeader from "./WideHeader";
import NarrowMenu from "./NarrowMenu";


const ReadMorePage = ({wide, goToMap}) => {
    return [
        <div className="row top-bar header" key="header">
            {wide ? <WideHeader/> : <NarrowMenu/>}
        </div>,
        <div className="page-with-text" key="content">
            <div style={{overflow: "hidden"}}>
                <img key="logo" src="/assets/images/kotologo-big.png"
                     style={{float: "left", marginRight: "10px"}}/>
                <p className="page-with-text-header">
                    {Messages("read_more.header")}
                </p>
            </div>
            <div key="text" className="welcome-text">
                {Messages("read_more.text")}
            </div>
            <div className="pure-g" style={{marginTop: "50px"}}>
                <div className="pure-u-1-1 pure-u-lg-1-3" style={{margin: "auto"}}>
                    <button onClick={goToMap} className="pure-button pure-button-primary big-wide-button">{Messages("read_more.back_button")}</button>
                </div>
            </div>
        </div>
    ];
};

const ReadMorePageContainer = connect(
    state => {
        return {
            wide: state.responsive.isWide,
        };
    },
    dispatch => {
        return {
            goToContact: () => {
                dispatch(goToReadMore())
            },
            goToMap: () => {
                dispatch(goToMap());
            },
        }
    },
)(ReadMorePage);

export default ReadMorePageContainer;
