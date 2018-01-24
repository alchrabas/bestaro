import {formatDate} from "../utils";
import {connect} from "react-redux";
import React from "react";
import {deselectRecord, scrollList} from "../store";
import RecordsList from "./RecordsList";

const RecordDetails = ({record, moveBack, style, buttonsFixed}) => {

    const buttonStyle = {};
    if (buttonsFixed) {
        buttonStyle.position = "fixed";
        buttonStyle.bottom = "0";
    }

    return <div className="pure-g" style={style}>
        {!record.eventDate && [
            <div className="pure-u-1-2"> {Messages("details.event_date")} </div>,
            <div className="pure-u-1-2"> {formatDate(record.eventDate)} </div>
        ]}
        <div className="pure-u-1-2"> {Messages("details.post_date")} </div>
        <div className="pure-u-1-2"> {formatDate(record.publishDate)} </div>
        <div className="pure-u-1-2"> {Messages("details.event_type")} </div>
        <div className="pure-u-1-2"> {Messages("event_type." + record.eventType)} </div>
        <img className="fullPicturePreview" src={"pictures/" + record.picture}/>
        <div className="pure-u-1" style={buttonStyle}>
            <button
                style={{width: "50%"}}
                className="pure-button pure-button-primary big-wide-button"
                onClick={moveBack}>
                Powrót
            </button>
            <a
                style={{width: "50%"}}
                className="pure-button pure-button-primary big-wide-button"
                href={record.link}
                target="_blank"
            > {Messages("details.link")}</a>
        </div>
    </div>;
};

export const RecordDetailsContainer = connect((state, ownProps) => {
        return {
            record: ownProps.record,
            style: ownProps.style,
            buttonsFixed: ownProps.buttonsFixed,
        }
    },
    dispatch => {
        return {
            moveBack: () => dispatch(deselectRecord()),
        }
    }
)(RecordDetails);

