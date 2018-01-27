import React from "react";

const WideHeader = ({goToReadMore}) => {
    return [
        <img key="logo" src="/assets/images/kotologo.png"/>,
        <span key="site-name" style={{
            fontSize: "32px",
            verticalAlign: "top",
        }}>MapaZwierzat.pl</span>,
        <div key="nav-buttons" className="nav-buttons">
            <button className="pure-button" onClick={goToReadMore}>Jak to działa?</button>
        </div>
    ];
};

export default WideHeader;
