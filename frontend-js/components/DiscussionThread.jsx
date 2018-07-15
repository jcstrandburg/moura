import { Component } from 'react';
import PropTypes from 'prop-types';

import PrettyDate from './PrettyDate.jsx';
import UserLink from './UserLink.jsx';

export class Message extends Component {
    doRender = ({ user, createdTime, content }) =>
    <div style={{ margin: '10px' }}>
        <div>
            <UserLink user={user} /> commented  <PrettyDate date={createdTime} />            
        </div>
        {content}
    </div>

    render = () => this.doRender(this.props.message);
}

export default class DiscussionThread extends Component {
    doRender = ({ messages }) =>
    <div>
        {messages.map(m => <Message message={m}/>)}
    </div>

    render = () => this.doRender(this.props);
}
