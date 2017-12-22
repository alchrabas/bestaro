import React from "react";
import {GoogleMap, Marker, withGoogleMap, withScriptjs} from "react-google-maps";
import {googleApiKey} from "../context";
import {selectRecord, updateMapCenter} from "../store";
import {connect} from "react-redux";
import {EVENT_LOST, EVENT_FOUND} from "../constants";

const iconByEventType = {
    [EVENT_LOST]: "red-pin.png",
    [EVENT_FOUND]: "green-pin.png",
};


const iconPathForEventType = (eventType) => {
    return "/assets/images/" + iconByEventType[eventType];
};

class MapWrapper extends React.Component {

    constructor(props) {
        super(props);

        this.onClickMarker = this.onClickMarker.bind(this);
        this.handleBoundsChanged = this.handleBoundsChanged.bind(this);
        this.createMarker = this.createMarker.bind(this);
        this.getBounds = this.getBounds.bind(this);
    }

    getBounds() {
        if (this.mapRef) {
            return {
                northEast: this.mapRef.getBounds().getNorthEast(),
                southWest: this.mapRef.getBounds().getSouthWest(),
            }
        } else {
            return {
                northEast: {lat: 0, lng: 0},
                southWest: {lat: 0, lng: 0},
            };
        }
    }

    render() {
        const records = this.props.records;

        const {northEast, southWest} = this.getBounds();
        const markersToPresent = records
            .filter(record => MapWrapper.pointBetween(
                record.lat, record.lng,
                northEast, southWest))
            .map(this.createMarker);

        return <GoogleMap
            ref={ref => this.mapRef = ref}
            defaultZoom={12}
            defaultCenter={{lat: 50.063408, lng: 19.943933}}
            onBoundsChanged={this.handleBoundsChanged}
            options={{gestureHandling: 'greedy'}}
        >
            {markersToPresent}
        </GoogleMap>;
    }

    static pointBetween(lat, lng, northEast, southWest) {
        const latInBounds = lat <= northEast.lat() && lat >= southWest.lat();
        const lngInBounds = lng <= northEast.lng() && lng >= southWest.lng();

        return latInBounds && lngInBounds;
    }

    handleBoundsChanged() {
        this.props.onCenterChanged(this.mapRef.getCenter().lat(), this.mapRef.getCenter().lng());
    }

    createMarker(record) {
        return <Marker
            position={{
                lat: parseFloat(record.lat),
                lng: parseFloat(record.lng)
            }}
            flat={true}
            record={record}
            icon={
                (this.props.selectedRecord && this.props.selectedRecord.id === record.id)
                    ? "/assets/images/blue-pin.png"
                    : iconPathForEventType(record.eventType)
            }
            onClick={() => this.onClickMarker(record)}
            key={"record_" + record.id}
        />;
    }

    onClickMarker(record) {
        this.props.selectRecord(record.id);
    }

}

const GoogleMapComponent = withScriptjs(withGoogleMap(MapWrapper));


const mapStateToProps = state => {
    return {
        records: state.records,
        googleMapURL: "https://maps.googleapis.com/maps/api/js?key=" + googleApiKey,
        loadingElement: <div style={{height: `100%`}}/>,
        containerElement: <div style={{width: "100%"}}/>,
        mapElement: <div style={{height: `100%`, width: "100%"}}/>,
        selectedRecord: state.ui.selectedRecord,
    };
};

const mapDispatchToProps = dispatch => {
    return {
        selectRecord: recordId => {
            dispatch(selectRecord(recordId));
        },
        onCenterChanged: (lat, lng) => {
            dispatch(updateMapCenter({lat, lng}));
        },
    }
};

const GoogleMapContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(GoogleMapComponent);

export default GoogleMapContainer;
