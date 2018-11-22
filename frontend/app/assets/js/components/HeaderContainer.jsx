import React from "react";
import {connect} from "react-redux";
import {goToMap, goToReadMore, goToPrivacyPolicy, switchLanguage} from "../store";
import WideHeader from "./WideHeader";
import NarrowMenu from "./NarrowMenu";

const LANGUAGE_ENGLISH = "en";
const LANGUAGE_POLISH = "pl";

class NavbarItem {
    constructor(message_tag, callback) {
        this.message_tag = message_tag;
        this.callback = callback;
    }
}


const Header = ({wide, language, goToMap, goToReadMore, switchToEnglish, switchToPolish, goToPrivacyPolicy}) => {
    const items = [
        new NavbarItem("navbar.map", goToMap),
        new NavbarItem("navbar.read_more", goToReadMore),
        (language != "en"
            ? new NavbarItem("navbar.english", switchToEnglish)
            : new NavbarItem("navbar.polish", switchToPolish)),
        new NavbarItem("navbar.privacy_policy", goToPrivacyPolicy)
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
