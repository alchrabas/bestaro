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
        return <DropdownMenu
            isOpen={this.state.open}
            close={this.toggleMenu}
            toggle={<img src="/assets/images/hamburger.png"
                         onClick={this.toggleMenu}/>}
            align="right">
            {this.props.items.map(item =>
                <li onClick={item.callback} key={item.messageTag}>
                    <button type="button"
                            className={item.isActive ? "nav-menu-item-active" : ""}>
                        {Messages(item.messageTag)}
                    </button>
                </li>)}
        </DropdownMenu>;
    }
}


const NarrowMenuContainer = connect(
    (state, ownProps) => {
        return Object.assign({}, {
            items: ownProps.items
        }, state);
    }
)(NarrowMenu);

export default NarrowMenuContainer;
