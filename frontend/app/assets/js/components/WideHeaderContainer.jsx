import React from "react";
import {connect} from "react-redux";

const WideHeader = (props) => {
    return [
        <img key="logo" src="/assets/images/kotologo.png"/>,
        <span key="site-name" style={{
            fontSize: "32px",
            verticalAlign: "top",
        }}>MapaZwierzat.pl</span>,
        <div key="nav-buttons" className="nav-buttons">
            {props.items.map(item =>
                <button
                    className="pure-button button-link"
                    key={item.message_tag}
                    onClick={item.callback}>
                    {Messages(item.message_tag)}
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
