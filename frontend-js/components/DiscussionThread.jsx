import { Component } from 'react';
import PropTypes from 'prop-types';
import ReactMarkdown from 'react-markdown';

import PrettyDate from './PrettyDate.jsx';
import UserLink from './UserLink.jsx';

export class Message extends Component {
    doRender = ({ token, user, createdTime, content, style, ...props }) =>
    <div style={{ margin: '5px', padding: '10px', border: '1px solid gray', ...style }} {...props}>
        <div>
            <UserLink user={user} /> commented  <PrettyDate date={createdTime} />
        </div>
        <ReactMarkdown source={content} />
    </div>

    render = () => this.doRender(this.props.message);
}

export default class DiscussionThread extends Component {
    doRender = ({ messages, ...props }) =>
    <div {...props}>
        {messages.map(m => <Message key={m.token} message={m}/>)}
    </div>

    render = () => this.doRender(this.props);
}
