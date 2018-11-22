import React from "react";
import DropdownMenu from "react-dd-menu";
import "react-dd-menu/src/scss/react-dd-menu.scss";
import {connect} from "react-redux";



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

            console.log("ZYJESZ " + this.props.items[0].message_tag);
        return <DropdownMenu
            isOpen={this.state.open}
            close={this.toggleMenu}
            toggle={<img src="/assets/images/hamburger.png"
                         onClick={this.toggleMenu}/>}
            align="right">
            {this.props.items.map(item =>
                <li onClick={item.callback} key={item.message_tag}>
                    <button type="button">{Messages(item.message_tag)}</button>
                </li>)}
        </DropdownMenu>;
    }
}


const NarrowMenuContainer = connect(
    (state, ownProps) => {
        return Object.assign({}, {
            items: ownProps.items
        }, state);
    },
    dispatch => {
        return {
        };
    }
)(NarrowMenu);

export default NarrowMenuContainer;
