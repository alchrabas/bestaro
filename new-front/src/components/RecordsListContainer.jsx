import { connect } from 'react-redux';
import React from 'react';
import { scrollList, selectRecord } from '../ducks/ui';
import scrollbarSize from 'scrollbar-size';
import { EVENT_LOST } from '../constants';
import { AutoSizer, List } from 'react-virtualized';
import { withTranslation } from 'react-i18next';
import { compose } from 'redux';

const groups = [
    {
        upperBound: 100,
        message: 'range_groups.to100m',
    }, {
        upperBound: 500,
        message: 'range_groups.to500m',
    }, {
        upperBound: 1000,
        message: 'range_groups.to1km',
    }, {
        upperBound: 5000,
        message: 'range_groups.to5km',
    }, {
        upperBound: 10000,
        message: 'range_groups.to10km',
    }, {
        upperBound: 50000,
        message: 'range_groups.to50km',
    }, {
        upperBound: 100000,
        message: 'range_groups.to100km',
    }, {
        upperBound: Number.MAX_SAFE_INTEGER,
        message: 'range_groups.above100km',
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

        this.state = { groupHeaderMessage: '' };

        this.renderList = this.renderList.bind(this);
        this.updateGroupHeader = this.updateGroupHeader.bind(this);
    }

    static renderGroupHeader(message, key) {
        return <div key={key}
                    className="records-list-header"
                    style={{ fontSize: HEADER_FONT_SIZE_PX + 'px' }}
        >
            {message}
        </div>;
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

    static rowHeightByIndex(firstRowIndexByGroup, groupByRowIndex, recordDimension) {
        return ({ index }) => {
            const beginOfThisGroup = firstRowIndexByGroup[groupByRowIndex(index)];
            return index === beginOfThisGroup ? HEADER_FONT_SIZE_PX : recordDimension;
        }
    }

    renderRow(beginOfThisGroup, index, key, recordDimension,
        buckets, columnsCount, groupByRowIndex, onClick) {
        if (beginOfThisGroup === index) {
            return RecordsList.renderGroupHeader(this.props.t(groups[groupByRowIndex(index)].message, index));
        } else {
            const lineOfRecordsInGroup = index - beginOfThisGroup - 1;
            const recordsToShow = RecordsList.recordsToShowInRow(index, lineOfRecordsInGroup, buckets, groupByRowIndex, columnsCount);

            return recordsToShow.map(record =>
                <div
                    key={key + '_' + record.id}
                    className="record-list-item"
                    style={{
                        width: recordDimension + 'px',
                        height: recordDimension + 'px',
                        display: 'inline-block',
                        position: 'relative',
                    }}>
                    <div style={{
                        width: `${recordDimension - 8}px`,
                        height: `${recordDimension - 8}px`,
                        position: 'relative',
                        top: '4px',
                        left: '4px',
                    }}>
                        <img
                            style={{
                                width: '100%',
                                height: '100%',
                                objectFit: 'contain',
                            }}
                            alt={this.props.t('details.animal_picture')}
                            className="animal-image"
                            src={record.minPicture}
                            onClick={() => onClick(record.id)}
                        />
                        <div className={record.eventType === EVENT_LOST
                            ? 'animal-image-status-lost'
                            : 'animal-image-status-found'}
                             title={record.eventType === EVENT_LOST
                                 ? this.props.t('record_details.probably_lost')
                                 : this.props.t('record_details.probably_found')}
                        />
                    </div>
                </div>
            );
        }
    };

    renderList() {
        const { records, onClick, onListScroll } = this.props;
        const onScroll = ({ scrollTop }) => {
            onListScroll(scrollTop);
        };
        const sortedRecords = records.concat()
            .sort((a, b) => a.distance - b.distance);

        return <AutoSizer>
            {({ width, height }) => {
                const workingWidth = width - scrollbarSize();
                const columnsCount = smartColumnCount(workingWidth);
                const recordDimension = smartColumnWidth(workingWidth);

                const buckets = groupRecordsByIndex(sortedRecords);
                const firstRowIndexByGroup = RecordsList.getFirstRowIndexByGroup(buckets, columnsCount);

                const groupByRowIndex = index => {
                    let previousGroupId = 0;
                    const existingGroupIndices = Object.keys(firstRowIndexByGroup);
                    for (let groupId of existingGroupIndices) {
                        if (firstRowIndexByGroup[groupId] > index) {
                            return previousGroupId;
                        }
                        previousGroupId = groupId;
                    }
                    return existingGroupIndices[existingGroupIndices.length - 1];
                };

                // index for each record to know how many rows to omit
                const cellRenderer = ({ index, key, style }) => {
                    const beginOfThisGroup = firstRowIndexByGroup[groupByRowIndex(index)];

                    const contentsOfRow = this.renderRow(beginOfThisGroup, index, key,
                        recordDimension, buckets, columnsCount, groupByRowIndex, onClick, this.t);
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
                    width={width} />;
            }}
        </AutoSizer>;
    }

    render() {
        return (
            <div className="records-list-wrapper"
                 style={this.props.style}>
                {RecordsList.renderGroupHeader(this.state.groupHeaderMessage, 'headerMessage')}
                <div className="records-list">
                    {this.renderList()}
                </div>
            </div>);
    };

    updateGroupHeader(groupByRowIndex) {
        return ({ startIndex }) => {
            if (startIndex !== undefined) {
                const groupInfo = groups[groupByRowIndex(startIndex)];
                this.setState({ groupHeaderMessage: this.props.t(groupInfo.message) });
            }
        }
    }
}


const smartColumnWidth = (workingWidth) => {
    return workingWidth / smartColumnCount(workingWidth);
};

const smartColumnCount = (workingWidth) => {
    return Math.max(1, Math.floor(workingWidth / 150));
};


const RecordsListContainer = compose(
    withTranslation(),
    connect((state, ownProps) => {
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
        })
)(RecordsList);

export default RecordsListContainer;