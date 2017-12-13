import React from "react";
import {tag} from "../utils";
import {changeFilter} from "../store";
import {fetchDataFromServer} from "../react-script";
import {connect} from "react-redux";
import {EVENT_ANY, EVENT_FOUND, EVENT_LOST} from "../constants";

class TopBar extends React.Component {

    constructor(props) {
        super(props);

        this.state = TopBar.propsToState(props);

        this.handleChangeDateFrom = this.handleChangeDateFrom.bind(this);
        this.handleChangeDateTo = this.handleChangeDateTo.bind(this);
        this.handleChangeEventType = this.handleChangeEventType.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    componentWillReceiveProps(nextProps) {
        this.setState(TopBar.propsToState(nextProps));
    }

    static propsToState(props) {
        return {
            dateFrom: props.filters.dateFrom || '',
            dateTo: props.filters.dateTo || '',
            eventType: props.filters.eventType || '',
        }
    }

    handleSubmit(event) {
        event.preventDefault();
        if (TopBar.isDateStrValid(this.state.dateFrom) && TopBar.isDateStrValid(this.state.dateTo)) {
            this.props.updateFilters(this.state);
        }
    }

    static isDateStrValid(date) {
        return date !== "";
    }

    handleChangeDateFrom(event) {
        this.setState({
            dateFrom: event.target.value,
            dateTo: this.state.dateTo,
            eventType: this.state.eventType,
        });
    }

    handleChangeDateTo(event) {
        this.setState({
            dateFrom: this.state.dateFrom,
            dateTo: event.target.value,
            eventType: this.state.eventType,
        });
    }

    handleChangeEventType(event) {
        this.state = {
            dateFrom: this.state.dateFrom,
            dateTo: this.state.dateTo,
            eventType: event.target.value,
        }
    }

    render() {
        return tag("div", {className: "row top-bar header"}, [
            tag("form", {
                className: "pure-form",
                onSubmit: this.handleSubmit,
            }, [
                tag("label", {}, [Messages("date_from.label"),
                    tag("input", {
                        name: "date-from",
                        id: "date-from",
                        type: "date",
                        onChange: this.handleChangeDateFrom,
                        value: this.state.dateFrom,
                    })
                ]),
                tag("label", {}, [Messages("date_to.label"),
                    tag("input", {
                        name: "date-to",
                        id: "date-to",
                        type: "date",
                        onChange: this.handleChangeDateTo,
                        value: this.state.dateTo,
                    })
                ]),
                tag("label", {}, Messages("event_type.label")),
                tag("select", {name: "event-type", id: "event-type", onChange: this.handleChangeEventType}, [
                    tag("option", {value: EVENT_ANY}, Messages("event_type.ANY")),
                    tag("option", {value: EVENT_LOST}, Messages("event_type.LOST")),
                    tag("option", {value: EVENT_FOUND}, Messages("event_type.FOUND")),
                ]),
                tag("button", {
                    id: "filter-button",
                    className: "pure-button pure-button-primary"
                }, Messages("filter_button"))
            ])
        ]);
    }
}

let TopBarContainer = ({dispatch, filters}) => {
    return tag(TopBar, {
        filters: filters,
        updateFilters: filters => {
            dispatch(changeFilter(
                filters.dateFrom,
                filters.dateTo,
                filters.eventType
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
