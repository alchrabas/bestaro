import {formatDate} from "../utils";
import {connect} from "react-redux";
import React from "react";
import {deselectRecord} from "../ducks/ui";
import Messages from './Messages';

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
        <div className="pure-u-1-1" style={{textAlign: "center"}}>
            <img alt={Messages("details.animal_picture")} className="fullPicturePreview" src={"/pictures/" + record.picture}/>
        </div>
        <div className="pure-u-1" style={buttonStyle}>
            <button
                style={{width: "50%"}}
                className="pure-button pure-button-primary big-wide-button button-on-bottom"
                onClick={moveBack}>
                {Messages("details.back")}
            </button>
            <a
                className="pure-button pure-button-primary big-wide-button button-on-bottom"
                style={{borderLeft: "2px solid #22f", width: "50%"}}
                href={record.link}
                target="_blank"
                rel="noopener noreferrer"
            > {Messages("details.link")}</a>
        </div>
    </div>;
};

const RecordDetailsContainer = connect((state, ownProps) => {
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

export default RecordDetailsContainer;
