import React from 'react';
import DropdownMenu from 'react-dd-menu';
import 'react-dd-menu/src/scss/react-dd-menu.scss';
import { connect } from 'react-redux';
import { withTranslation } from 'react-i18next';
import { compose } from 'redux';


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
        const { t } = this.props;
        return <DropdownMenu
            isOpen={this.state.open}
            close={this.toggleMenu}
            toggle={<img src="/assets/images/hamburger.png"
                         alt={t('navbar.menu')}
                         onClick={this.toggleMenu} />}
            align="right">
            {this.props.items.map(item =>
                <li onClick={item.callback} key={item.messageTag}>
                    <button type="button"
                            className={item.isActive ? 'nav-menu-item-active' : ''}>
                        {t(item.messageTag)}
                    </button>
                </li>)}
        </DropdownMenu>;
    }
}


const NarrowMenuContainer = compose(
    connect(
        (state, ownProps) => {
            return Object.assign({}, {
                items: ownProps.items
            }, state);
        }),
    withTranslation(),
)(NarrowMenu);

export default NarrowMenuContainer;
