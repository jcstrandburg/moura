import { Component } from 'react';
import PropTypes from 'prop-types';

import PrettyDate from './PrettyDate.jsx'

export class Message extends Component {
    doRender = ({ user, createdTime, content }) =>
    <div>
        {user.name}<br />
        <PrettyDate date={createdTime} /><br />
        {content}<br />
    </div>

    render = () => this.doRender(this.props.message);
}

export default class DiscussionThread extends Component {
    doRender = ({ messages }) =>
    <div>
    </div>

    render = () => this.doRender(this.props);
}
