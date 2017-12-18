import React from "react";
import {dateToString, daysRelativeToNow, tag} from "../utils";
import {changeFilter, fetchDataFromServer} from "../store";
import {connect} from "react-redux";
import {EVENT_ANY, EVENT_FOUND, EVENT_LOST} from "../constants";

class TopBar extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            rangeType: "lastWeek",
        };

        this.handleChangeDateFrom = this.handleChangeDateFrom.bind(this);
        this.handleChangeDateTo = this.handleChangeDateTo.bind(this);
        this.handleUpdateDateRange = this.handleUpdateDateRange.bind(this);
        this.forceUpdateRangeFromInputs = this.forceUpdateRangeFromInputs.bind(this);
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
        if (TopBar.isDateStrValid(this.state.dateFrom) && TopBar.isDateStrValid(this.state.dateTo)) {
            this.props.updateFilters(this.state.dateFrom, this.state.dateTo, EVENT_ANY);
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
            this.props.updateFilters(
                dateToString(rangeTypeToBeginDate[value]),
                dateToString(new Date()),
                EVENT_ANY);
        } else {
            this.setState(Object.assign({}, this.state, {
                rangeType: value,
            }));
        }
    };

    render() {
        return <div className="row top-bar header">
            <form
                className="pure-form">
                <label>
                    <select name="date-range"
                            id="date-range"
                            value={this.state.rangeType}
                            onChange={this.handleUpdateDateRange}>
                        <option value="lastWeek">Ostatni tydzień</option>
                        <option value="last2Weeks">Ostatnie 2 tygodnie</option>
                        <option value="lastMonth">Ostatni miesiąc</option>
                        <option value="last3Months">Ostatnie 3 miesiące</option>
                        <option value="lastYear">Ostatni rok</option>
                        <option value="custom">Niestandardowe...</option>
                    </select>
                </label>
                {this.state.rangeType === "custom" && [
                    <label key="dateFrom">Przedział dat:
                        <input
                            name="date-from"
                            id="date-from"
                            type="date"
                            onChange={this.handleChangeDateFrom}
                            value={this.state.dateFrom || ""}/>
                    </label>,
                    <label key="dateTo">
                        <input
                            name="date-to"
                            id="date-to"
                            type="date"
                            onChange={this.handleChangeDateTo}
                            value={this.state.dateTo || ""}/>
                    </label>]}
            </form>
        </div>;
    }
}

let TopBarContainer = ({dispatch, filters}) => {
    return tag(TopBar, {
        filters: filters,
        updateFilters: (dateFrom, dateTo, eventType) => {
            dispatch(changeFilter(
                dateFrom,
                dateTo,
                eventType
            ));
            dispatch(fetchDataFromServer());
        }
    });
};

TopBarContainer = connect(state => {
    return {
        filters: state.filters
    };
})(TopBarContainer);

export default TopBarContainer;
