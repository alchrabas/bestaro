import React from 'react';
import { connect } from 'react-redux';
import WideHeader from './WideHeaderContainer';
import NarrowMenu from './NarrowMenuContainer';
import { VIEW_MAP, VIEW_PRIVACY_POLICY, VIEW_READ_MORE } from '../constants';
import { withRouter } from 'react-router';
import { compose } from 'redux';
import i18n from '../i18n';

class NavbarItem {
    constructor(messageTag, callback, isActive) {
        this.messageTag = messageTag;
        this.callback = callback;
        this.isActive = isActive;
    }
}


const Header = ({
    wide, currentView, goToMap, goToReadMore,
    switchToEnglish, switchToPolish, goToPrivacyPolicy
}) => {
    const items = [
        new NavbarItem('navbar.map', goToMap, currentView === VIEW_MAP),
        new NavbarItem('navbar.read_more', goToReadMore, currentView === VIEW_READ_MORE),
        (i18n.language !== 'en'
            ? new NavbarItem('navbar.english', switchToEnglish)
            : new NavbarItem('navbar.polish', switchToPolish)),
        new NavbarItem('navbar.privacy_policy', goToPrivacyPolicy, currentView === VIEW_PRIVACY_POLICY)
    ];

    return wide
        ? <WideHeader items={items} />
        : <NarrowMenu items={items} />;
};

const mapStateToProps = state => ({
    wide: state.responsive.isWide,
    currentView: state.ui.currentView,
});

const mapDispatchToProps = (dispatch, ownProps) => {
    return {
        goToReadMore: () => {
            ownProps.history.push('read-more');
        },
        goToMap: () => {
            ownProps.history.push('map');
        },
        switchToEnglish: () => {
            ownProps.history.push('/en/');
        },
        switchToPolish: () => {
            ownProps.history.push('/pl/');
        },
        goToPrivacyPolicy: () => {
            ownProps.history.push('privacy-policy');
        },
    }
};

const HeaderContainer = compose(
    withRouter,
    connect(mapStateToProps, mapDispatchToProps)
)(Header);

export default HeaderContainer;
