import React from "react";
import TopBarContainer from "./TopBar";
import {connect} from "react-redux";
import RecordsList from "./RecordsList";
import {selectRecord} from "../store";
import {RecordDetailsContainer} from "./Sidebar";
import MapCacheContainer from "./MapCache";


const VIEW_WELCOME = "WELCOME";
const VIEW_LIST = "LIST";
const VIEW_MAP = "MAP";

class NarrowLayout extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            viewName: VIEW_WELCOME,
        };

        this.goToList = this.goToList.bind(this);
        this.goToMap = this.goToMap.bind(this);
    }

    goToList() {
        this.setState({
            viewName: VIEW_LIST,
        });
    }

    goToMap() {
        this.setState({
            viewName: VIEW_MAP,
        });
    }

    render() {
        if (this.props.selectedRecord) {
            return <RecordDetailsContainer record={this.props.selectedRecord}/>;
        }

        switch (this.state.viewName) {
            case VIEW_WELCOME:
                return <div>
                    <div key="text"
                         dangerouslySetInnerHTML={{__html: Messages("welcome_text")}}
                    />
                    <button
                        className="pure-button-primary big-wide-button"
                        onClick={this.goToMap}>
                        POKAŻ MAPĘ
                    </button>
                </div>;
            case VIEW_MAP:
                return [
                    <div className="row top-bar header">
                        <TopBarContainer key="topBar"/>
                    </div>,
                    <div className="row content" key="center">
                        <div className="google-map-parent">
                            <MapCacheContainer key="googleMap"/>
                            <div id="center-marker"/>
                        </div>
                    </div>,
                    <button key="button"
                            className="pure-button-primary big-wide-button"
                            onClick={this.goToList}>PACZAJ</button>
                ];
            case VIEW_LIST:
                return <div
                    style={{
                        display: "flex",
                        flexDirection: "column",
                        height: "100%",
                    }}>
                    <RecordsList key="records-list" style={{flex: 1}}/>
                    <button onClick={this.goToMap}
                            key="button"
                            className="pure-button-primary big-wide-button">
                        WRÓĆ NA MAPĘ
                    </button>
                </div>;
        }
    }
}

const NarrowLayoutContainer = connect(state => {
        return {
            selectedRecord: state.ui.selectedRecord,
        };
    },
    dispatch => {
        return {
            onClick: recordId => dispatch(selectRecord(recordId)),
        };
    })(NarrowLayout);

export default NarrowLayoutContainer;
