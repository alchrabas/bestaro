import {connect} from "react-redux";
import React from "react";
import {scrollList, selectRecord} from "../store";
import scrollbarSize from "scrollbar-size/dist/scrollbar-size";
import {EVENT_LOST} from "../constants";
import {AutoSizer, List} from "react-virtualized";

const groups = [
    {
        upperBound: 100,
        message: Messages("range_groups.to100m"),
    }, {
        upperBound: 500,
        message: Messages("range_groups.to500m"),
    }, {
        upperBound: 1000,
        message: Messages("range_groups.to1km"),
    }, {
        upperBound: 5000,
        message: Messages("range_groups.to5km"),
    }, {
        upperBound: 10000,
        message: Messages("range_groups.to10km"),
    }, {
        upperBound: 50000,
        message: Messages("range_groups.to50km"),
    }, {
        upperBound: 100000,
        message: Messages("range_groups.to100km"),
    }, {
        upperBound: Number.MAX_SAFE_INTEGER,
        message: Messages("range_groups.above100km"),
    },
];

const getIndexForRecord = distance => {
    for (let index = 0; index < groups.length; index++) {
        if (distance < groups[index].upperBound) {
            return index;
        }
    }
};

/**
 * Puts records into buckets, each bucket is represented as an index of the `groups`.
 * The records in the same bucket have the same order
 */
const groupRecordsByIndex = records => {
    const buckets = {};
    records.forEach(record => {
        const index = getIndexForRecord(record.distance);
        if (buckets[index] === undefined) {
            buckets[index] = [];
        }
        buckets[index].push(record);
    });

    return buckets;
};

const HEADER_FONT_SIZE_PX = 24;

class RecordsList extends React.Component {

    constructor(props) {
        super(props);

        this.state = {groupHeaderMessage: ""};

        this.renderList = this.renderList.bind(this);
        this.updateGroupHeader = this.updateGroupHeader.bind(this);
    }

    static renderGroupHeader(message, key) {
        return <div key={key}
                    className="records-list-header"
                    style={{fontSize: HEADER_FONT_SIZE_PX + "px"}}
        >
            {message}
        </div>;
    };

    static renderRow(beginOfThisGroup, index, key, recordDimension,
                     buckets, columnsCount, groupByRowIndex, onClick) {
        if (beginOfThisGroup === index) {
            return RecordsList.renderGroupHeader(groups[groupByRowIndex(index)].message, index);
        } else {
            const lineOfRecordsInGroup = index - beginOfThisGroup - 1;
            const recordsToShow = RecordsList.recordsToShowInRow(index, lineOfRecordsInGroup, buckets, groupByRowIndex, columnsCount);

            return recordsToShow.map(record =>
                <div
                    key={key + "_" + record.id}
                    className="record-list-item"
                    style={{
                        width: recordDimension + "px",
                        height: recordDimension + "px",
                        display: "inline-block",
                        position: "relative",
                    }}>
                    <img
                        style={{
                            width: `${recordDimension - 8}px`,
                            height: `${recordDimension - 8}px`,
                            position: "relative",
                            top: "4px",
                            left: "4px",
                            objectFit: "contain",
                        }}
                        className="animal-image"
                        src={"pictures_min/" + record.picture}
                        onClick={() => onClick(record.id)}
                    />
                    <div className={record.eventType === EVENT_LOST
                        ? "animal-image-status-lost"
                        : "animal-image-status-found"}
                         title={record.eventType === EVENT_LOST
                             ? Messages("record_details.probably_lost")
                             : Messages("record_details.probably_found")}
                    />
                </div>
            );
        }
    };

    static recordsToShowInRow(index, lineOfRecordsInGroup, buckets, groupByRowIndex, columnsCount) {
        const startRowOfRecords = lineOfRecordsInGroup * columnsCount;
        const endRowOfRecords = (lineOfRecordsInGroup + 1) * columnsCount;

        return buckets[groupByRowIndex(index)].slice(startRowOfRecords, endRowOfRecords);
    }

    static getFirstRowIndexByGroup(buckets, columnsCount) {
        const firstRowIndexByGroup = {};
        let rowCounter = -1; // hide the first header
        for (let i = 0; i < groups.length; i++) {
            if (buckets[i] !== undefined) {
                firstRowIndexByGroup[i] = rowCounter;
                rowCounter += Math.ceil(buckets[i].length / columnsCount) + 1;
            }
        }
        return firstRowIndexByGroup;
    };

    renderList() {
        const {records, onClick, onListScroll} = this.props;
        const onScroll = ({scrollTop}) => {
            onListScroll(scrollTop);
        };
        const sortedRecords = records.concat()
            .sort((a, b) => a.distance - b.distance);

        return <AutoSizer>
            {({width, height}) => {
                const workingWidth = width - scrollbarSize();
                const columnsCount = smartColumnCount(workingWidth);
                const recordDimension = smartColumnWidth(workingWidth);

                const buckets = groupRecordsByIndex(sortedRecords);
                const firstRowIndexByGroup = RecordsList.getFirstRowIndexByGroup(buckets, columnsCount);

                const groupByRowIndex = index => {
                    let previousGroupId = 0;
                    for (let groupId of Object.keys(firstRowIndexByGroup)) {
                        if (firstRowIndexByGroup[groupId] > index) {
                            return previousGroupId;
                        }
                        previousGroupId = groupId;
                    }
                    const idOfLastGroup = groups.length - 1;
                    return idOfLastGroup;
                };

                // index for each record to know how many rows to omit
                const cellRenderer = ({index, key, style}) => {
                    const beginOfThisGroup = firstRowIndexByGroup[groupByRowIndex(index)];

                    const contentsOfRow = RecordsList.renderRow(beginOfThisGroup, index, key,
                        recordDimension, buckets, columnsCount, groupByRowIndex, onClick);
                    return <div key={key}
                                style={style}>
                        {contentsOfRow}
                    </div>;
                };

                const numberOfRows = (buckets, columnsCount) => {
                    return Object.values(buckets)
                        .map(bucket => Math.ceil(bucket.length / columnsCount) + 1)
                        .reduce((a, b) => a + b, 0)
                };

                return <List
                    rowRenderer={cellRenderer}
                    onScroll={onScroll}
                    scrollTop={this.props.listOffset}
                    height={height}
                    rowCount={numberOfRows(buckets, columnsCount)}
                    estimatedRowSize={recordDimension}
                    rowHeight={RecordsList.rowHeightByIndex(firstRowIndexByGroup, groupByRowIndex, recordDimension)}
                    onRowsRendered={this.updateGroupHeader(groupByRowIndex)}
                    width={width}/>;
            }}
        </AutoSizer>;
    }

    render() {
        return (
            <div className="records-list-wrapper">
                {RecordsList.renderGroupHeader(this.state.groupHeaderMessage, "headerMessage")}
                <div className="records-list">
                    {this.renderList()}
                </div>
            </div>);
    };

    updateGroupHeader(groupByRowIndex) {
        return ({startIndex}) => {
            if (startIndex !== undefined) {
                const groupInfo = groups[groupByRowIndex(startIndex)];
                this.setState({groupHeaderMessage: groupInfo.message});
            }
        }
    }

    static rowHeightByIndex(firstRowIndexByGroup, groupByRowIndex, recordDimension) {
        return ({index}) => {
            const beginOfThisGroup = firstRowIndexByGroup[groupByRowIndex(index)];
            return index === beginOfThisGroup ? HEADER_FONT_SIZE_PX : recordDimension;
        }
    }
}


const smartColumnWidth = (workingWidth) => {
    return workingWidth / smartColumnCount(workingWidth);
};

const smartColumnCount = (workingWidth) => {
    return Math.max(1, Math.floor(workingWidth / 150));
};


export default connect((state, ownProps) => {
        return {
            records: state.records,
            listOffset: state.ui.listRow,
            style: ownProps.style || {},
        };
    },
    dispatch => {
        return {
            onClick: recordId => dispatch(selectRecord(recordId)),
            onListScroll: topScrolling => dispatch(scrollList(topScrolling)),
        };
    })(RecordsList);
