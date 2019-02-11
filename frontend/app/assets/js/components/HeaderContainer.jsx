import React from "react";
import {connect} from "react-redux";
import {goToMap, goToPrivacyPolicy, goToReadMore, switchLanguage} from "../ducks/ui";
import WideHeader from "./WideHeaderContainer";
import NarrowMenu from "./NarrowMenuContainer";
import {VIEW_MAP, VIEW_PRIVACY_POLICY, VIEW_READ_MORE} from "../constants";

const LANGUAGE_ENGLISH = "en";
const LANGUAGE_POLISH = "pl";

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
        new NavbarItem("navbar.map", goToMap, currentView === VIEW_MAP),
        new NavbarItem("navbar.read_more", goToReadMore, currentView === VIEW_READ_MORE),
        (language !== "en"
            ? new NavbarItem("navbar.english", switchToEnglish)
            : new NavbarItem("navbar.polish", switchToPolish)),
        new NavbarItem("navbar.privacy_policy", goToPrivacyPolicy, currentView === VIEW_PRIVACY_POLICY)
    ];

    return wide
        ? <WideHeader items={items}/>
        : <NarrowMenu items={items}/>;
};

const HeaderContainer = connect(
    state => {
        return {
            wide: state.responsive.isWide,
            language: state.ui.language,
            currentView: state.ui.currentView,
        };
    },
    dispatch => {
        return {
            goToReadMore: () => {
                dispatch(goToReadMore())
            },
            goToMap: () => {
                dispatch(goToMap());
            },
            switchToEnglish: () => {
                dispatch(switchLanguage(LANGUAGE_ENGLISH));
            },
            switchToPolish: () => {
                dispatch(switchLanguage(LANGUAGE_POLISH));
            },
            goToPrivacyPolicy: () => {
                dispatch(goToPrivacyPolicy());
            },
        }
    },
)(Header);

export default HeaderContainer;
