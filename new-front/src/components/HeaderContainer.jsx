import React from 'react';
import { connect } from 'react-redux';
import { switchLanguage } from '../ducks/ui';
import WideHeader from './WideHeaderContainer';
import NarrowMenu from './NarrowMenuContainer';
import { VIEW_MAP, VIEW_PRIVACY_POLICY, VIEW_READ_MORE } from '../constants';
import { withRouter } from 'react-router';

const LANGUAGE_ENGLISH = 'en';
const LANGUAGE_POLISH = 'pl';

class NavbarItem {
    constructor(messageTag, callback, isActive) {
        this.messageTag = messageTag;
        this.callback = callback;
        this.isActive = isActive;
    }
}


const Header = ({
    wide, language, currentView, goToMap, goToReadMore,
    switchToEnglish, switchToPolish, goToPrivacyPolicy
}) => {
    const items = [
        new NavbarItem('navbar.map', goToMap, currentView === VIEW_MAP),
        new NavbarItem('navbar.read_more', goToReadMore, currentView === VIEW_READ_MORE),
        (language !== 'en'
            ? new NavbarItem('navbar.english', switchToEnglish)
            : new NavbarItem('navbar.polish', switchToPolish)),
        new NavbarItem('navbar.privacy_policy', goToPrivacyPolicy, currentView === VIEW_PRIVACY_POLICY)
    ];

    return wide
        ? <WideHeader items={items} />
        : <NarrowMenu items={items} />;
};

const HeaderContainer = withRouter(connect(
    state => {
        return {
            wide: state.responsive.isWide,
            language: state.ui.language,
            currentView: state.ui.currentView,
        };
    },
    (dispatch, ownProps) => {
        return {
            goToReadMore: () => {
                ownProps.history.push('read-more');
            },
            goToMap: () => {
                ownProps.history.push('map');
            },
            switchToEnglish: () => {
                dispatch(switchLanguage(LANGUAGE_ENGLISH));
            },
            switchToPolish: () => {
                dispatch(switchLanguage(LANGUAGE_POLISH));
            },
            goToPrivacyPolicy: () => {
                ownProps.history.push('privacy-policy');
            },
        }
    },
)(Header));

export default HeaderContainer;
