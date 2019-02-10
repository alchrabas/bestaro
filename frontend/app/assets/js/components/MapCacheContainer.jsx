import React from "react";
import ReactDOM from "react-dom";
import GoogleMapContainer from "./GoogleMapContainer";
import {connect} from "react-redux";
import PropTypes from "prop-types";
import {refreshMap} from "../ducks/map";

let cachedParentElement = null;
let cachedReactComponent = null;

class MapCache extends React.Component {

    componentDidMount() {
        if (cachedParentElement === null) {
            cachedParentElement = document.createElement("div");
            cachedParentElement.style.height = "100%";
            cachedParentElement.style.width = "100%";
            ReactDOM.render(
                <GoogleMapContainer
                    ref={ref => cachedReactComponent = ref}
                    store={this.context.store}/>,
                cachedParentElement
            );
        }

        document.getElementById("cachedMapContainer").appendChild(cachedParentElement);

        // this is to force redraw of the cached map, as google map is not meant to be used like that
        if (cachedReactComponent) {
            this.props.refreshMap();
        }
    }

    componentWillUnmount() {
        document.getElementById("cachedMapContainer").removeChild(cachedParentElement);
    }

    render() {
        return <div style={{flex: 2}} id="cachedMapContainer"/>;
    }
}

MapCache.contextTypes = {store: PropTypes.object};

const MapCacheContainer = connect(
    state => state,
    dispatch => {
        return {
            refreshMap: () => dispatch(refreshMap())
        };
    }
)(MapCache);

export default MapCacheContainer;
