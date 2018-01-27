import React from "react";

class NarrowMenu extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            showMenu: false,
        };

        this.toggleMenu = this.toggleMenu.bind(this);
    }

    toggleMenu() {
        this.setState({
            showMenu: !this.state.showMenu,
        });
    }

    render() {
        return <div style={{position: "fixed", top: 0, right: 0, zIndex: 100}}>
            <img onClick={this.toggleMenu}
                 src="/assets/images/hamburger.png"
                 style={{float: "right"}}/>
            {this.state.showMenu &&
            <MenuList/>}
        </div>;
    }
}

const MenuList = () => {
    return <ul style={{
        listStyleType: "none",
        paddingLeft: 0,
        backgroundColor: "#ddd",
        margin: 0,
        clear: "both",
        fontSize: "24px"
    }}>
        <li>Mapa</li>
        <li>Jak to działa?</li>
    </ul>;
};

export default NarrowMenu;
