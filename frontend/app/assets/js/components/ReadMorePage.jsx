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
        <div className="page-with-text">
            <div style={{overflow: "hidden"}}>
                <img key="logo" src="/assets/images/kotologo-big.png"
                     style={{float: "left", marginRight: "10px"}}/>
                <p className="page-with-text-header">
                    Dane kontaktowe
                </p>
            </div>
            <div key="text" className="welcome-text">Ple ple ple zrobione przez: Aleksander Chrabąszcz.<br/>
                Jakiś ładny obrazek. COś jeszcze więcej tu napiszę ale nei wiem o co chodzi tyle że to istotne
                przyanierjiwje qiwej qiwe jqiw jdolorem ipsum costam sit amet, <br/>
                He hE he
            </div>
            <div className="pure-g" style={{marginTop: "50px"}}>
                <div className="pure-u-1-1 pure-u-lg-1-3" style={{margin: "auto"}}>
                    <button onClick={goToMap} className="pure-button big-wide-button">Powrót</button>
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
