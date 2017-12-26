import {formatDate} from "../utils";
import {connect} from "react-redux";
import React from "react";
import {deselectRecord, scrollList} from "../store";
import RecordsList from "./RecordsList";

let SidebarWithDetails = ({record, moveBack}) => {
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

const SidebarWithDetailsContainer = connect((state, ownProps) => {
        return {
            record: ownProps.record,
        }
    },
    dispatch => {
        return {
            moveBack: () => dispatch(deselectRecord()),
        }
    }
)(SidebarWithDetails);

const SidebarWelcomePage = ({goToList}) => {
    return [
        <div key="text"
             dangerouslySetInnerHTML={{__html: Messages("welcome_text")}}/>,
        <div key="credits"
             className="credits"
             dangerouslySetInnerHTML={{__html: Messages("credits")}}/>,
        <button
            key="goToList "
            className="pure-button"
            onClick={goToList}>To list</button>
    ];
};

const SidebarWelcomePageContainer = connect(state => {
        return {};
    },
    dispatch => {
        return {
            goToList: () => dispatch(scrollList(0)),
        };
    })(SidebarWelcomePage);


const Sidebar = ({selectedRecord, listRow}) => {
    const sidebarContents = (record) => {
        if (record) {
            return <SidebarWithDetailsContainer record={record}/>;
        } else if (listRow !== null) {
            return <RecordsList
                listRow={listRow}
            />;
        } else {
            return <SidebarWelcomePageContainer/>;
        }
    };

    return sidebarContents(selectedRecord);
};

const SidebarContainer = connect(state => {
        return {
            selectedRecord: state.ui.selectedRecord,
            listRow: state.ui.listRow,
        };
    }
)(Sidebar);

export default SidebarContainer;
