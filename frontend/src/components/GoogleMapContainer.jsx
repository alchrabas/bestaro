import React from 'react';
import { GoogleMap, Marker, withGoogleMap } from 'react-google-maps';
import { selectRecord } from '../ducks/ui';
import { connect } from 'react-redux';
import { EVENT_FOUND, EVENT_LOST } from '../constants';
import { updateMapBounds } from '../ducks/map';
import redPin from '../images/red-pin.png';
import greenPin from '../images/green-pin.png';
import yellowPin from '../images/yellow-pin.png';

const iconByEventType = {
    [EVENT_LOST]: redPin,
    [EVENT_FOUND]: greenPin,
};

const iconPathForEventType = (eventType) => {
    return iconByEventType[eventType];
};

class MapWrapper extends React.Component {

    constructor(props) {
        super(props);

        this.mapRef = React.createRef();

        this.onClickMarker = this.onClickMarker.bind(this);
        this.handleBoundsChanged = this.handleBoundsChanged.bind(this);
        this.createMarker = this.createMarker.bind(this);
        this.getBounds = this.getBounds.bind(this);
    }

    static pointBetween(lat, lng, northEast, southWest) {
        const latInBounds = lat <= northEast.lat() && lat >= southWest.lat();
        const lngInBounds = lng <= northEast.lng() && lng >= southWest.lng();

        return latInBounds && lngInBounds;
    }

    getBounds() {
        if (this.mapRef.current && this.mapRef.current.getBounds()) {
            return {
                northEast: this.mapRef.current.getBounds().getNorthEast(),
                southWest: this.mapRef.current.getBounds().getSouthWest(),
            }
        } else {
            return {
                northEast: { lat: () => 0, lng: () => 0 },
                southWest: { lat: () => 0, lng: () => 0 },
            };
        }
    }

    componentDidUpdate(prevProps, prevState) {
        if (prevProps.refreshSerialId !== this.props.refreshSerialId && this.mapRef.current) {
            // force redraw of the map
            window.google.maps.event.trigger(
                this.mapRef.current.context['__SECRET_MAP_DO_NOT_USE_OR_YOU_WILL_BE_FIRED'], 'resize');
        }
    }

    render() {
        const records = this.props.records;

        const { northEast, southWest } = this.getBounds();
        const markersToPresent = records
            .filter(record => MapWrapper.pointBetween(
                record.lat, record.lon,
                northEast, southWest))
            .map(this.createMarker);
        return <GoogleMap
            ref={this.mapRef}
            defaultZoom={12}
            defaultCenter={{ lat: 50.063408, lng: 19.943933 }}
            onBoundsChanged={this.handleBoundsChanged}
            options={{
                gestureHandling: 'greedy',
                streetViewControl: false,
                fullscreenControl: false,
                mapTypeControlOptions: {
                    mapTypeIds: []
                },
            }}
        >
            {markersToPresent}
        </GoogleMap>;
    }

    handleBoundsChanged() {
        const { northEast, southWest } = this.getBounds();
        this.props.onCenterChanged({
            minLat: southWest.lat(),
            minLon: southWest.lng(),
            maxLat: northEast.lat(),
            maxLon: northEast.lng(),
        });
    }

    createMarker(record) {
        return <Marker
            position={{
                lat: parseFloat(record.lat),
                lng: parseFloat(record.lon)
            }}
            flat={true}
            record={record}
            icon={
                (this.props.selectedRecord && this.props.selectedRecord.id === record.id)
                    ? yellowPin
                    : iconPathForEventType(record.eventType)
            }
            onClick={() => this.onClickMarker(record)}
            key={'record_' + record.id}
        />;
    }

    onClickMarker(record) {
        this.props.selectRecord(record.id);
    }

}

const GoogleMapComponent = withGoogleMap(MapWrapper);


const mapStateToProps = (state, ownProps) => {
    return {
        records: state.records,
        loadingElement: <div style={{ height: `100%` }} />,
        containerElement: <div className="googleMapContainer" />,
        mapElement: <div style={{ height: `100%`, width: '100%' }} />,
        selectedRecord: state.ui.selectedRecord,
        className: ownProps.className || '',
        style: ownProps.style || {},
        refreshSerialId: state.map.refreshSerialId,
    };
};

const mapDispatchToProps = dispatch => {
    return {
        selectRecord: recordId => {
            dispatch(selectRecord(recordId));
        },
        onCenterChanged: (bounds) => {
            dispatch(updateMapBounds(bounds));
        },
    }
};

const GoogleMapContainer = connect(
    mapStateToProps,
    mapDispatchToProps,
    null,
    { forwardRef: true }
)(GoogleMapComponent);

export default GoogleMapContainer;
