import { Component } from 'react';
import PropTypes from 'prop-types';

import Header from 'components/Header.jsx';

export default class App extends Component {

    doRender = ({ user, signOut }) => 
    <div>
        <Header user={user} signOut={signOut} />
        <div>
            Content goes here
        </div>
    </div>;

    render = () => doRender(this.props);
}

App.propTypes = {
    signOut: PropTypes.func.isRequired,
    user: PropTypes.shape({
        name: PropTypes.string.isRequired
    }).isRequired,
};
