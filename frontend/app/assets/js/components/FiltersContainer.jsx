import React from "react";
import {dateToString, daysRelativeToNow} from "../utils";
import {changeDateFilter, changeEventTypeFilter} from "../ducks/filter";
import {connect} from "react-redux";
import {EVENT_ANY, EVENT_FOUND, EVENT_LOST, EVENT_NONE} from "../constants";
import {fetchDataFromServer} from "../ducks/records";

class Filters extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            rangeType: "lastWeek",
            eventType: props.filters.eventType,
        };

        this.handleChangeDateFrom = this.handleChangeDateFrom.bind(this);
        this.handleChangeDateTo = this.handleChangeDateTo.bind(this);
        this.handleUpdateDateRange = this.handleUpdateDateRange.bind(this);
        this.handleUpdateEventType = this.handleUpdateEventType.bind(this);
        this.forceUpdateRangeFromInputs = this.forceUpdateRangeFromInputs.bind(this);
        this.isLostChecked = this.isLostChecked.bind(this);
        this.isFoundChecked = this.isFoundChecked.bind(this);
    }

    static isDateStrValid(date) {
        return !!date;
    }

    handleChangeDateFrom(event) {
        this.setState({
            rangeType: "custom",
            dateFrom: event.target.value,
            dateTo: this.state.dateTo,
        }, this.forceUpdateRangeFromInputs);
    }

    handleChangeDateTo(event) {
        this.setState({
            rangeType: "custom",
            dateFrom: this.state.dateFrom,
            dateTo: event.target.value,
        }, this.forceUpdateRangeFromInputs);
    }

    forceUpdateRangeFromInputs() {
        if (Filters.isDateStrValid(this.state.dateFrom) && Filters.isDateStrValid(this.state.dateTo)) {
            this.props.updateDateFilter(this.state.dateFrom, this.state.dateTo);
        }
    }

    handleUpdateDateRange(event) {
        const value = event.target.value;

        const rangeTypeToBeginDate = {
            lastWeek: daysRelativeToNow(-7),
            last2Weeks: daysRelativeToNow(-14),
            lastMonth: daysRelativeToNow(-31),
            last3Months: daysRelativeToNow(-92),
            lastYear: daysRelativeToNow(-366),
        };

        if (value in rangeTypeToBeginDate) {
            this.setState({
                rangeType: value,
            });
            this.props.updateDateFilter(
                dateToString(rangeTypeToBeginDate[value]),
                dateToString(new Date()));
        } else {
            this.setState(Object.assign({}, this.state, {
                rangeType: value,
            }));
        }
    };

    handleUpdateEventType(event, type) {
        const value = event.target.checked;
        let eventType = null;
        if (type === "LOST") {
            eventType = this.isFoundChecked() ? (value ? EVENT_ANY : EVENT_FOUND) : (value ? EVENT_LOST : EVENT_NONE);
        } else if (type === "FOUND") {
            eventType = this.isLostChecked() ? (value ? EVENT_ANY : EVENT_LOST) : (value ? EVENT_FOUND : EVENT_NONE);
        }
        this.setState(Object.assign({}, this.state, {
            eventType: eventType
        }));
        this.props.updateEventTypeFilter(eventType);
    }

    isLostChecked() {
        return [EVENT_ANY, EVENT_LOST].includes(this.state.eventType);
    }

    isFoundChecked() {
        return [EVENT_ANY, EVENT_FOUND].includes(this.state.eventType);
    }

    render() {
        return <form
            className="pure-form" style={{textAlign: "center"}}>
            <EventDateRange selectedOption={this.state.rangeType}
                            onChange={this.handleUpdateDateRange}
                            handleChangeDateFrom={this.handleChangeDateFrom}
                            handleChangeDateTo={this.handleChangeDateTo}
                            dateFrom={this.state.dateFrom}
                            dateTo={this.state.dateTo}
                            key="eventDate"/>
            <EventType lostChecked={this.isLostChecked()}
                       foundChecked={this.isFoundChecked()}
                       onChange={this.handleUpdateEventType}
                       key="eventType"/>
        </form>;
    }
}

const EventType = ({lostChecked, foundChecked, onChange}) => {
    return <div className="event-type-checkboxes">
        <label className="checkbox-label" key="lostCheckbox">
            <input type="checkbox"
                   checked={lostChecked}
                   onChange={event => onChange(event, "LOST")}/>
            {Messages("event_type.LOST")}
        </label>
        <label className="checkbox-label" key="foundCheckbox">
            <input type="checkbox"
                   checked={foundChecked}
                   onChange={event => onChange(event, "FOUND")}/>
            {Messages("event_type.FOUND")}
        </label>
    </div>;
};

const EventDateRange = ({
                            selectedOption, onChange,
                            handleChangeDateFrom, handleChangeDateTo,
                            dateFrom, dateTo
                        }) =>
    [
        <select name="date-range"
                id="date-range"
                value={selectedOption}
                onChange={onChange}
                key="dateRangeSelect">
            <option value="lastWeek">{Messages("event_date.last_week")}</option>
            <option value="last2Weeks">{Messages("event_date.last_2_weeks")}</option>
            <option value="lastMonth">{Messages("event_date.last_month")}</option>
            <option value="last3Months">{Messages("event_date.last_3_months")}</option>
            <option value="lastYear">{Messages("event_date.last_year")}</option>
            <option value="custom">{Messages("event_date.custom")}</option>
        </select>,
        selectedOption === "custom" &&
        <div className="event-type-checkboxes">
            <label key="dateFrom"> {Messages("event_date.custom_range")}
                <input
                    name="date-from"
                    id="date-from"
                    type="date"
                    onChange={handleChangeDateFrom}
                    value={dateFrom || ""}/>
            </label>
            <label key="dateTo">
                <input
                    name="date-to"
                    id="date-to"
                    type="date"
                    onChange={handleChangeDateTo}
                    value={dateTo || ""}/>
            </label>
        </div>
    ];


const FiltersContainer = connect(state => {
        return {
            filters: state.filters
        };
    },
    dispatch => {
        return {
            updateDateFilter: (dateFrom, dateTo) => {
                dispatch(changeDateFilter(
                    dateFrom,
                    dateTo
                ));
                dispatch(fetchDataFromServer());
            },
            updateEventTypeFilter: eventType => {
                dispatch(changeEventTypeFilter(
                    eventType
                ));
                dispatch(fetchDataFromServer());
            }
        };
    })(Filters);

export default FiltersContainer;
