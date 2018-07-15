import { Component } from 'react';
import PropTypes from 'prop-types';


const UserLink = ({ user }) => <span><a href={'/users/'+user.token}>{user.name}</a></span>;
export default UserLink;

UserLink.propTypes = {
    user: PropTypes.shape({
        token: PropTypes.string.isRequired,
        name: PropTypes.string.isRequired,
    }).isRequired,
};
