import React from "react";
import ReactDOM from "react-dom";
import GoogleMapContainer from "./MapWrapper";
import {connect} from "react-redux";
import PropTypes from "prop-types";

let cachedParentElement = null;

class MapCache extends React.Component {

    componentDidMount() {
        if (cachedParentElement === null) {
            cachedParentElement = document.createElement("div");
            cachedParentElement.style.height = "100%";
            cachedParentElement.style.width = "100%";
            ReactDOM.render(
                <GoogleMapContainer store={this.context.store}/>,
                cachedParentElement
            );
        }

        document.getElementById("cachedMapContainer").appendChild(cachedParentElement);
    }

    componentWillUnmount() {
        document.getElementById("cachedMapContainer").removeChild(cachedParentElement);
    }

    render() {
        return <div style={{flex: 2}} id="cachedMapContainer"/>;
    }
}

MapCache.contextTypes = {store: PropTypes.object};

const MapCacheContainer = connect()(MapCache);

export default MapCacheContainer;
