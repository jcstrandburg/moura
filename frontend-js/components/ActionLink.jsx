import { Component } from 'react';
import PropTypes from 'prop-types';


const style = {
    cursor: 'pointer',
    textDecoration: 'underline',
};

const ActionLink = ({ action, text, ...props }) => <span onClick={action} className='action-link' style={style} {...props}>{text}</span>;
export default ActionLink;

ActionLink.propTypes = {
    text: PropTypes.string.isRequired,
    action: PropTypes.func.isRequired,
};
