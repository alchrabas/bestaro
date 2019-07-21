import React from 'react';
import ReactDOM from 'react-dom';
import GoogleMapContainer from './GoogleMapContainer';
import { connect } from 'react-redux';
import { refreshMap } from '../ducks/map';
import { fetchDataFromServer } from '../ducks/records';
import { store } from '../store';

let cachedParentElement = null;
let cachedReactComponent = null;

class MapCache extends React.Component {

    componentDidMount() {
        if (cachedParentElement === null) {
            cachedReactComponent = React.createRef();
            cachedParentElement = document.createElement('div');
            cachedParentElement.style.height = '100%';
            cachedParentElement.style.width = '100%';
            ReactDOM.render(
                <GoogleMapContainer
                    ref={cachedReactComponent}
                    store={store} />,
                cachedParentElement
            );
        }

        document.getElementById('cachedMapContainer').appendChild(cachedParentElement);

        // this is to force redraw of the cached map, as google map is not meant to be used like that
        if (cachedReactComponent.current) {
            this.props.refreshMap();
        }
        fetchDataFromServer();
    }

    componentWillUnmount() {
        document.getElementById('cachedMapContainer').removeChild(cachedParentElement);
    }

    render() {
        return <div style={{ flex: 2 }} id="cachedMapContainer" />;
    }
}

const MapCacheContainer = connect(
    state => state,
    dispatch => {
        return {
            refreshMap: () => dispatch(refreshMap())
        };
    },
    null,
    {forwardRef : true}
)(MapCache);

export default MapCacheContainer;
