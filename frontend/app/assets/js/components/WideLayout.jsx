import * as React from "react";
import FiltersContainer from "./Filters";
import {connect} from "react-redux";
import RecordsList from "./RecordsList";
import {RecordDetailsContainer} from "./Sidebar";
import MapCacheContainer from "./MapCache";
import WelcomePageContainer from "./WelcomePage";


const SidebarForWideLayout = ({selectedRecord, listRow}) => {
    if (selectedRecord) {
        return <RecordDetailsContainer style={{flex: "0 0 40%"}}
                                       record={selectedRecord}/>;
    } else {
        return <div
            key="sidebar"
            style={{
                display: "flex",
                flexDirection: "column"
            }}
            className="sidebar">
            <FiltersContainer key="filters"/>
            <RecordsList
                style={{display: "flex", flex: 1}}
                listRow={listRow}
            />
        </div>;
    }
};

const SidebarForWideLayoutContainer = connect(state => {
        return {
            selectedRecord: state.ui.selectedRecord,
            listRow: state.ui.listRow,
        };
    }
)(SidebarForWideLayout);


const VIEW_WELCOME = "WELCOME";
const VIEW_MAP_AND_LIST = "MAP_AND_LIST";

const WideHeader = () => {
    return [
        <img key="logo" src="/assets/images/kotologo.png"/>,
        <span key="site-name" style={{
            fontSize: "32px",
            verticalAlign: "top",
        }}>mapazwierzat.pl</span>,
        <div key="nav-buttons" style={{float: "right"}}>
            <button className="pure-button">Kontakt</button>
        </div>
    ];
};

class WideLayout extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            viewName: VIEW_WELCOME,
        };

        this.goToMapAndList = this.goToMapAndList.bind(this);
    }

    goToMapAndList() {
        this.setState({
            viewName: VIEW_MAP_AND_LIST,
        });
    }

    render() {
        switch (this.state.viewName) {
            case VIEW_WELCOME:
                return <WelcomePageContainer goToMap={this.goToMapAndList}/>;
            case VIEW_MAP_AND_LIST:
                return [
                    <div className="row top-bar header" key="header">
                        <WideHeader/>
                    </div>,
                    <div className="row content" key="center">
                        <SidebarForWideLayoutContainer key="sidebar"/>
                        <div className="google-map-parent">
                            <MapCacheContainer key="googleMap"/>
                            <div id="center-marker"/>
                        </div>
                    </div>
                ];
        }
    };
}

export default WideLayout;
