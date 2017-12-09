// "imports"
const connect = window.ReactRedux.connect;


const CHANGE_FILTER = "CHANGE_FILTER";
const UPDATE_RECORDS = "UPDATE_RECORDS";
const SELECT_RECORD = "SELECT_RECORD";

const EVENT_ANY = "ANY";
const EVENT_LOST = "LOST";
const EVENT_FOUND = "FOUND";

const filterReducer = (state = {
    "dateFrom": null,
    "dateTo": null,
    "eventType": null
}, action) => {
    switch (action.type) {
        case CHANGE_FILTER:
            return Object.assign({}, state, {
                dateFrom: action.dateFrom,
                dateTo: action.dateTo,
                eventType: action.eventType,
            });
        default:
            return state;
    }
};


const recordsReducer = (state = [], action) => {
    switch (action.type) {
        case UPDATE_RECORDS:
            return action.records;
        default:
            return state;
    }
};

const uiReducer = (state = {selectedRecord: null}, action) => {
    switch (action.type) {
        case SELECT_RECORD:
            return {
                selectedRecord: action.selectedRecord,
            };
        default:
            return state;
    }
};

const mainReducer = window.Redux.combineReducers({
    filters: filterReducer,
    records: recordsReducer,
    ui: uiReducer,
});

const ReduxThunk = window.ReduxThunk.default;

const composeEnhancers = window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ || window.Redux.compose;
let store = window.Redux.createStore(mainReducer,
    composeEnhancers(window.Redux.applyMiddleware(ReduxThunk))
);


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
            dispatch({type: CHANGE_FILTER, ...filters});
            dispatch(fetchDataFromServer());
        }
    });
};

TopBarContainer = connect(state => {
    return {
        filters: state.filters
    };
})(TopBarContainer);


let SideBarWithDetails = ({record}) => {
    return tag("div", {className: "pure-g"}, [
        tag("div", {className: "pure-u-1-2"}, Messages("details.event_date")),
        tag("div", {className: "pure-u-1-2"}, formatDate(record.eventDate)),
        tag("div", {className: "pure-u-1-2"}, Messages("details.post_date")),
        tag("div", {className: "pure-u-1-2"}, formatDate(record.publishDate)),
        tag("div", {className: "pure-u-1-2"}, Messages("details.event_type")),
        tag("div", {className: "pure-u-1-2"}, Messages("event_type." + record.eventType)),
        tag("div", {className: "pure-u-1"}, Messages("details.picture")),
        tag("div", {className: "pure-u-1"}, [
                tag("img", {className: "fullPicturePreview", src: "pictures/" + record.picture}),
                tag("div", {className: "pure-u-1"},
                    tag("a", {href: record.link}, Messages("details.link"))
                )
            ]
        )
    ]);
};

let SidebarWelcomePage = () => {
    return [
        tag("div", {
            key: "text",
            dangerouslySetInnerHTML: {__html: Messages("welcome_text")},
        }),
        tag("div", {
            key: "credits",
            className: "credits",
            dangerouslySetInnerHTML: {__html: Messages("credits")},
        }),
    ];
};

const Grid = window.ReactVirtualized.Grid;

const SidebarWithRecords = ({records}) => {
    const cellRenderer = ({columnIndex, key, rowIndex, style}) => {
        if (rowIndex * 7 + columnIndex < records.length) {
            return tag("div", {
                key: key,
                style: style,
            }, [
                tag("img", {src: "pictures_min/" + records[rowIndex * 7 + columnIndex].picture})
            ]);
        } else {
            return null;
        }
    };

    return tag(Grid, {
        cellRenderer: cellRenderer,
        columnCount: 7,
        columnWidth: 100,
        height: 900,
        rowCount: Math.ceil(records.length / 7),
        rowHeight: 100,
        width: 750,
    });
};

const SidebarWithRecordsContainer = connect(state => {
    return {records: state.records};
})(SidebarWithRecords);

let Sidebar = ({selectedRecord}) => {
    const sidebarContents = (record) => {
        if (record) {
            return tag(SideBarWithDetails, {record: record});
        } else {
            return tag(SidebarWithRecordsContainer, {});
        }
    };

    return tag("div", {className: "sidebar-content"}, sidebarContents(selectedRecord));
};

const iconByEventType = {
    [EVENT_LOST]: "red-pin.png",
    [EVENT_FOUND]: "green-pin.png",
};

const iconPathForEventType = (eventType) => {
    return "/assets/images/" + iconByEventType[eventType];
};

class GoogleMapContainer extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            visibleMarkers: []
        };

        this.onClickMarker = this.onClickMarker.bind(this);
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.records !== this.props.records) {
            this.state.visibleMarkers.forEach(marker => marker.setMap(null));
            this.state.visibleMarkers = [];
            nextProps.records.map(record =>
                this.createMarker(record)
            ).forEach(marker => {
                this.state.visibleMarkers.push(marker);
                marker.setMap(googleMap);
            });
        }
    }

    createMarker(record) {
        const animalMarker = new google.maps.Marker({
            position: new google.maps.LatLng(parseFloat(record.lat), parseFloat(record.lon)),
            flat: true,
            map: googleMap,
            record: record,
            icon: iconPathForEventType(record.eventType),
        });
        google.maps.event.addListener(animalMarker, 'click', () => this.onClickMarker(animalMarker));
        return animalMarker;
    }

    onClickMarker(animalMarker) {
        this.props.selectRecord(animalMarker.record.id);
    }

    render() {
        return tag("div", {id: "mapContainer"});
    }
}

GoogleMapContainer = connect(state => {
    return {
        records: state.records,
    };
}, dispatch => {
    return {
        selectRecord: (recordId) => {
            dispatch(selectRecord(recordId));
        }
    }
})(GoogleMapContainer);

let AppContainer = ({dispatch, selectedRecord}) => {
    return [
        tag(TopBarContainer, {key: "topBar"}),
        tag("div", {className: "row content", key: "center"},
            [
                tag("div", {
                        key: "sidebar",
                        className: "sidebar"
                    },
                    tag(Sidebar, {selectedRecord: selectedRecord})),
                tag(GoogleMapContainer, {
                    key: "googleMap"
                })
            ],
        ),
    ];
};


AppContainer = connect((state) => {
    return {
        selectedRecord: state.ui.selectedRecord,
    };
})(AppContainer);

window.ReactDOM.render(
    tag(window.ReactRedux.Provider, {store: store},
        tag(AppContainer)
    ),
    document.getElementById('root')
);

const selectRecord = (recordId) => {
    return (dispatch, getState) => {
        const selectedRecord = getState().records.filter(r => r.id === recordId)[0];
        dispatch({type: SELECT_RECORD, selectedRecord: selectedRecord});
    }

};

const fetchDataFromServer = () => {
    return (dispatch, getState) => {
        console.log("FETCHING DATA FROM SERVER");
        const boundsNE = googleMap.getBounds().getNorthEast();
        const boundsSW = googleMap.getBounds().getSouthWest();
        const filters = getState().filters;
        fetch(`/rest/${boundsSW.lat()}/${boundsSW.lng()}/${boundsNE.lat()}/${boundsNE.lng()}/`
            + `${filters.dateFrom}/${filters.dateTo}/${filters.eventType}`)
            .then(response => response.json())
            .then(data =>
                dispatch({
                    type: UPDATE_RECORDS,
                    records: data,
                })
            ).catch(e => {
            console.log("Error when trying to fetch data", e);
        });
    };
};


const weekAgoDate = new Date();
weekAgoDate.setDate(weekAgoDate.getDate() - 7);

store.dispatch({
    type: CHANGE_FILTER,
    dateFrom: dateToString(weekAgoDate),
    dateTo: dateToString(new Date()),
    eventType: EVENT_ANY,
});

const centerOfKrakow = new google.maps.LatLng(50.063408, 19.943933);

const googleMap = new google.maps.Map(document.getElementById('mapContainer'), {
    zoom: 12,
    center: centerOfKrakow,
    gestureHandling: 'greedy',
});


let lastMoveTimestamp = Date.now();
const updateLastMoveTimestamp = () => lastMoveTimestamp = Date.now();
googleMap.addListener("bounds_changed", updateLastMoveTimestamp);

// ask server for data when it may be outdated
setInterval(() => {
    const currentTimestamp = Date.now();
    if (currentTimestamp >= lastMoveTimestamp + 3000) {
        store.dispatch(fetchDataFromServer());
        lastMoveTimestamp = Infinity;
    }
}, 1000);

setTimeout(() => store.dispatch(fetchDataFromServer()), 1000);
