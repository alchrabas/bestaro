import {formatDate} from "../utils";
import {connect} from "react-redux";
import React from "react";
import {deselectRecord, scrollList} from "../store";
import RecordsList from "./RecordsList";

const RecordDetails = ({record, moveBack}) => {
    return <div className="pure-g">
        <div className="pure-u-1-2"> {Messages("details.event_date")} </div>
        <div className="pure-u-1-2"> {formatDate(record.eventDate)} </div>
        <div className="pure-u-1-2"> {Messages("details.post_date")} </div>
        <div className="pure-u-1-2"> {formatDate(record.publishDate)} </div>
        <div className="pure-u-1-2"> {Messages("details.event_type")} </div>
        <div className="pure-u-1-2"> {Messages("event_type." + record.eventType)} </div>
        <div className="pure-u-1"> {Messages("details.picture")} </div>
        <div className="pure-u-1"/>
        <img className="fullPicturePreview" src={"pictures/" + record.picture}/>
        <div className="pure-u-1">
            <a
                className="pure-button"
                href={record.link}
                target="_blank"
            > {Messages("details.link")}</a>
        </div>
        <div className="pure-u-1">
            <button className="pure-button" onClick={moveBack}> GO BACK</button>
        </div>
    </div>;
};

export const RecordDetailsContainer = connect((state, ownProps) => {
        return {
            record: ownProps.record,
        }
    },
    dispatch => {
        return {
            moveBack: () => dispatch(deselectRecord()),
        }
    }
)(RecordDetails);

