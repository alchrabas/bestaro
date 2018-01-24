import React from "react";


const WideHeader = ({goToContact}) => {
    return [
        <img key="logo" src="/assets/images/kotologo.png"/>,
        <span key="site-name" style={{
            fontSize: "32px",
            verticalAlign: "top",
        }}>MapaZwierzat.pl</span>,
        <div key="nav-buttons" style={{float: "right"}}>
            <button className="pure-button" onClick={goToContact}>Kontakt</button>
        </div>
    ];
};

export default WideHeader;
