import { Component } from 'react';
import PropTypes from 'prop-types';
import { Dropdown, DropdownToggle, DropdownMenu, DropdownItem } from 'reactstrap';

import ActionLink from 'components/ActionLink.jsx';


const styles = {
    mainHeader: {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        background: 'rgb(34, 49, 63)',
    },
    mainHeaderText: {
        fontSize: '30px',
        color: 'white',
    },
    mainHeaderDropdown: {
        marginRight: '50px',
    },
    dropdownMenu: {
        left: 'unset',
        right: 0,
    },
};

export default class Header extends Component {
    state = {
        dropdownOpen: false
    };

    toggle = () => {
        this.setState({ dropdownOpen: !this.state.dropdownOpen });
    }

    render = () =>
        <div style={styles.mainHeader}>
            <span style={styles.mainHeaderText}>Moura Header</span>
            <Dropdown style={styles.mainHeaderDropdown} isOpen={this.state.dropdownOpen} toggle={this.toggle}>
                <DropdownToggle caret>{this.props.user.name}</DropdownToggle>
                <DropdownMenu style={styles.dropdownMenu}>
                    <DropdownItem>Settings</DropdownItem>
                    <DropdownItem>
                        <ActionLink text='Sign Out' action={this.props.signOut} />
                    </DropdownItem>
                </DropdownMenu>
            </Dropdown>
        </div>
}

Header.propTypes = {
    signOut: PropTypes.func.isRequired,
    user: PropTypes.shape({
        name: PropTypes.string.isRequired
    }).isRequired,
};
