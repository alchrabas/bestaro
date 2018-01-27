import React from "react";
import FiltersContainer from "./Filters";
import {connect} from "react-redux";
import RecordsList from "./RecordsList";
import {selectRecord} from "../store";
import {RecordDetailsContainer} from "./Sidebar";
import MapCacheContainer from "./MapCache";
import NarrowMenu from "./NarrowMenu";


const SUBVIEW_LIST = "SUBVIEW_LIST";
const SUBVIEW_MAP = "SUBVIEW_MAP";

class NarrowMapPage extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            subview: SUBVIEW_MAP,
        };

        this.goToList = this.goToList.bind(this);
        this.goToMap = this.goToMap.bind(this);
    }

    goToList() {
        this.setState({
            subview: SUBVIEW_LIST,
        });
    }

    goToMap() {
        this.setState({
            subview: SUBVIEW_MAP,
        });
    }

    render() {
        if (this.props.selectedRecord) {
            return <RecordDetailsContainer
                buttonsFixed={true}
                record={this.props.selectedRecord}/>;
        }

        if (this.state.subview === SUBVIEW_MAP) {
            return [
                <div className="row top-bar header" key="header">
                    <NarrowMenu toggleMenu={this.toggleMenu}/>
                    <FiltersContainer key="filters"/>
                </div>,
                <div className="row content" key="center">
                    <div className="google-map-parent">
                        <MapCacheContainer key="googleMap"/>
                        <div id="center-marker"/>
                    </div>
                </div>,
                <button className="pure-button-primary big-wide-button button-on-bottom"
                        onClick={this.goToList}
                        key="button">Pokaż ogłoszenia w pobliżu</button>
            ];
        } else {
            return <div
                style={{
                    display: "flex",
                    flexDirection: "column",
                    height: "100%",
                }}>
                <FiltersContainer key="filters"/>
                <RecordsList key="records-list" style={{flex: 1}}/>
                <button onClick={this.goToMap}
                        key="button"
                        className="pure-button-primary big-wide-button button-on-bottom">
                    Powrót na mapę
                </button>
            </div>;
        }
    }
}

const NarrowMapPageContainer = connect(state => {
        return {
            selectedRecord: state.ui.selectedRecord,
        };
    },
    dispatch => {
        return {
            onClick: recordId => dispatch(selectRecord(recordId)),
        };
    })(NarrowMapPage);

export default NarrowMapPageContainer;
