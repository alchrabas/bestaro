import {formatDate} from "../utils";
import {connect} from "react-redux";
import React from "react";
import {deselectRecord, scrollList} from "../store";
import RecordsList from "./RecordsList";

const RecordDetails = ({record, moveBack, style}) => {
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
        <div className="pure-u-1">
            <button
                style={{width: "50%"}}
                className="pure-button pure-button-primary"
                onClick={moveBack}>
                GO BACK
            </button>
            <a
                style={{width: "50%"}}
                className="pure-button pure-button-primary"
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
        }
    },
    dispatch => {
        return {
            moveBack: () => dispatch(deselectRecord()),
        }
    }
)(RecordDetails);

