import React from 'react';
import { connect } from 'react-redux';
import kotologo from '../images/kotologo.png';
import { useTranslation } from 'react-i18next';

const WideHeader = (props) => {
    const { t } = useTranslation();
    return [
        <img alt="Logo" key="logo" src={kotologo} />,
        <span key="site-name" style={{
            fontSize: "32px",
            verticalAlign: "top",
        }}>MapaZwierzat.pl</span>,
        <div key="nav-buttons" className="nav-buttons">
            {props.items.map(item =>
                <button
                    className={"pure-button button-link " + (item.isActive ? "button-link-active" : "")}
                    key={item.messageTag}
                    onClick={item.callback}>
                    {t(item.messageTag)}
                </button>
            )}
        </div>,
    ];
};

const WideHeaderContainer = connect(
    (state, ownProps) => {
        return Object.assign({}, {
            items: ownProps.items
        }, state);
    }
)(WideHeader);

export default WideHeaderContainer;
