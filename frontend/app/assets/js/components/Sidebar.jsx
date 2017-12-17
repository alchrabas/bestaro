import {formatDate} from "../utils";
import {connect} from "react-redux";
import {AutoSizer, Grid} from "react-virtualized";
import React from "react";
import {EVENT_LOST} from "../constants";
import scrollbarSize from "scrollbar-size";
import {scrollList, selectRecord} from "../store";

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
            moveBack: () => dispatch(selectRecord(null)),
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


const SidebarWithRecords = ({records, onClick}) => {
    return <AutoSizer>
        {({width, height}) => {
            const workingWidth = width - scrollbarSize();
            const columnsCount = smartColumnCount(workingWidth);
            const columnWidth = smartColumnWidth(workingWidth);
            const cellRenderer = ({columnIndex, key, rowIndex, style}) => {
                if (rowIndex * columnsCount + columnIndex < records.length) {
                    const record = records[rowIndex * columnsCount + columnIndex];
                    return <div
                        key={key}
                        style={Object.assign({}, {
                            position: "relative",
                        }, style)}
                    >
                        <img
                            style={{
                                width: `${columnWidth - 8}px`,
                                height: `${columnWidth - 8}px`,
                                position: "relative",
                                top: "4px",
                                left: "4px",
                            }}
                            className="animal-image"
                            src={"pictures_min/" + record.picture}
                            onClick={() => onClick(record.id)}
                        />
                        <div className={record.eventType === EVENT_LOST
                            ? "animal-image-status-lost"
                            : "animal-image-status-found"}
                        />
                    </div>;
                } else {
                    return null;
                }
            };

            return <Grid
                cellRenderer={cellRenderer}
                columnCount={columnsCount}
                columnWidth={columnWidth}
                height={height}
                rowCount={Math.ceil(records.length / columnsCount)}
                rowHeight={columnWidth}
                width={width}/>;
        }}
    </AutoSizer>;
};

const smartColumnWidth = (workingWidth) => {
    return workingWidth / smartColumnCount(workingWidth);
};

const smartColumnCount = (workingWidth) => {
    return Math.max(1, Math.floor(workingWidth / 150));
};


const SidebarWithRecordsContainer = connect(state => {
        return {
            records: state.records,
        };
    },
    dispatch => {
        return {
            onClick: recordId => dispatch(selectRecord(recordId)),
        };
    })(SidebarWithRecords);

const Sidebar = ({selectedRecord, listRow}) => {
    const sidebarContents = (record) => {
        if (record) {
            return <SidebarWithDetailsContainer record={record}/>;
        } else if (listRow !== null) {
            return <SidebarWithRecordsContainer
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
