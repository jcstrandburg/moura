import { Component } from 'react';
import PropTypes from 'prop-types';


const UserLink = ({ user }) => <span><a href={'/users/'+user.id}>{user.name}</a></span>;
export default UserLink;

UserLink.propTypes = {
    user: PropTypes.shape({
        id: PropTypes.number.isRequired,
        name: PropTypes.string.isRequired,
    }).isRequired,
};
