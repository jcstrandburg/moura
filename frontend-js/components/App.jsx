import { Component } from 'react';
import PropTypes from 'prop-types';

import Header from 'components/Header.jsx';


const App = ({ user, signOut }) => 
    <div>
        <Header user={user} signOut={signOut} />
        <div>
            Content goes here
        </div>
    </div>;
export default App;

App.propTypes = {
    signOut: PropTypes.func.isRequired,
    user: PropTypes.shape({
        name: PropTypes.string.isRequired
    }).isRequired,
};
