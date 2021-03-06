import React from 'react';
import FiltersContainer from './FiltersContainer';
import { connect } from 'react-redux';
import RecordsList from './RecordsListContainer';
import RecordDetailsContainer from './RecordDetailsContainer';
import { selectRecord } from '../ducks/ui';
import MapCacheContainer from './MapCacheContainer';
import HeaderContainer from './HeaderContainer';
import { withTranslation } from 'react-i18next';
import { compose } from 'redux';


const SUBVIEW_LIST = 'SUBVIEW_LIST';
const SUBVIEW_MAP = 'SUBVIEW_MAP';

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
        const { t } = this.props;

        if (this.props.selectedRecord) {
            return <RecordDetailsContainer
                buttonsFixed={true}
                record={this.props.selectedRecord} />;
        }

        if (this.state.subview === SUBVIEW_MAP) {
            return [
                <div className="row top-bar header" key="header">
                    <HeaderContainer key="header" />
                    <FiltersContainer key="filters" />
                </div>,
                <div className="row content" key="center">
                    <div className="google-map-parent">
                        <MapCacheContainer key="googleMap" />
                        <div id="center-marker" />
                    </div>
                </div>,
                <button className="pure-button-primary big-wide-button button-on-bottom"
                        onClick={this.goToList}
                        key="button">{t('show_announcements_in_proximity')}</button>
            ];
        } else {
            return <div
                style={{
                    display: 'flex',
                    flexDirection: 'column',
                    height: '100%',
                }}>
                <FiltersContainer key="filters" />
                <RecordsList key="records-list" style={{ flex: 1 }} />
                <button onClick={this.goToMap}
                        key="button"
                        className="pure-button-primary big-wide-button button-on-bottom">
                    {t('map.back_to_map')}
                </button>
            </div>;
        }
    }
}

const mapStateToProps = state => {
    return {
        selectedRecord: state.ui.selectedRecord,
    };
};

const mapDispatchToProps = dispatch => {
    return {
        onClick: recordId => dispatch(selectRecord(recordId)),
    };
};

const NarrowMapPageContainer = compose(
    connect(mapStateToProps, mapDispatchToProps),
    withTranslation(),
)(NarrowMapPage);

export default NarrowMapPageContainer;
