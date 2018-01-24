import {connect} from "react-redux";
import {goToContact} from "../store";
import React from "react";
import WideHeader from "./WideHeader";

const ContactPage = () => {
    return [
        <div className="row top-bar header" key="header">
            <WideHeader goToContact={goToContact}/>
        </div>,
        <div className="row content" key="center">
            INFORMACJE KONTAKTOWE TU SĄ
        </div>
    ];
};

const ContactPageContainer = connect(
    state => {
        return {};
    },
    dispatch => {
        return {
            goToContact: () => {
                dispatch(goToContact())
            },
        }
    },
)(ContactPage);

export default ContactPageContainer;
