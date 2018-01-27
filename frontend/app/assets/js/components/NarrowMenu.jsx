import React from "react";
import DropdownMenu from "react-dd-menu";
import "react-dd-menu/src/scss/react-dd-menu.scss";
import {connect} from "react-redux";
import {goToMap, goToReadMore} from "../store";


class NarrowMenu extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            open: false,
        };

        this.toggleMenu = this.toggleMenu.bind(this);
    }

    toggleMenu() {
        this.setState({
            open: !this.state.open,
        });
    }

    render() {
        return <DropdownMenu
            isOpen={this.state.open}
            close={this.toggleMenu}
            toggle={<img src="/assets/images/hamburger.png"
                         onClick={this.toggleMenu}/>}
            align="right">
            <li onClick={this.props.goToMap}>
                <button type="button">Mapa</button>
            </li>
            <li onClick={this.props.goToReadMore}>
                <button type="button">Jak to działa?</button>
            </li>
        </DropdownMenu>;
    }
}


const NarrowMenuContainer = connect(
    state => state,
    dispatch => {
        return {
            goToMap: () => dispatch(goToMap()),
            goToReadMore: () => dispatch(goToReadMore()),
        };
    }
)(NarrowMenu);

export default NarrowMenuContainer;
