import React from "react";
import {GoogleMap, Marker, withGoogleMap, withScriptjs} from "react-google-maps";
import {googleApiKey} from "../context";
import {selectRecord, updateMapBounds} from "../store";
import {connect} from "react-redux";
import {EVENT_LOST, EVENT_FOUND} from "../constants";

const iconByEventType = {
    [EVENT_LOST]: "red-pin.png",
    [EVENT_FOUND]: "green-pin.png",
};


const iconPathForEventType = (eventType) => {
    return "/assets/images/" + iconByEventType[eventType];
};

class MyMapComponent extends React.Component {

    constructor(props) {
        super(props);

        this.onClickMarker = this.onClickMarker.bind(this);
        this.handleBoundsChanged = this.handleBoundsChanged.bind(this);
        this.createMarker = this.createMarker.bind(this);
    }

    render() {
        const records = this.props.records;

        return <GoogleMap
            ref={ref => this.mapRef = ref}
            defaultZoom={12}
            defaultCenter={{lat: 50.063408, lng: 19.943933}}
            onBoundsChanged={this.handleBoundsChanged}
            options={{gestureHandling: 'greedy'}}
        >
            {records.map(this.createMarker)}
        </GoogleMap>;
    }

    handleBoundsChanged() {
        this.props.onBoundsChanged(this.mapRef.getBounds());
    }

    createMarker(record) {
        return <Marker
            position={{
                lat: parseFloat(record.lat),
                lng: parseFloat(record.lon)
            }}
            flat={true}
            record={record}
            icon={iconPathForEventType(record.eventType)}
            onClick={() => this.onClickMarker(record)}
            key={"record_" + record.id}
        />;
    }

    onClickMarker(record) {
        this.props.selectRecord(record.id);
    }

}

const GoogleMapComponent = withScriptjs(withGoogleMap(MyMapComponent));


const GoogleMapContainer = connect(state => {
    return {
        records: state.records,
        googleMapURL: "https://maps.googleapis.com/maps/api/js?key=" + googleApiKey,
        loadingElement: <div style={{height: `100%`}}/>,
        containerElement: <div style={{width: "100%"}}/>,
        mapElement: <div style={{height: `100%`, width: "100%"}}/>,
    };
}, dispatch => {
    return {
        selectRecord: recordId => {
            dispatch(selectRecord(recordId));
        },
        onBoundsChanged: bounds => {
            dispatch(updateMapBounds(
                bounds.getNorthEast(),
                bounds.getSouthWest()
            ));
        },
    }
})(GoogleMapComponent);

export default GoogleMapContainer;
