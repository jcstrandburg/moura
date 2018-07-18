import React, { Component } from 'react';
import PropTypes from 'prop-types';
import ReactMarkdown from 'react-markdown';

import PrettyDate from './PrettyDate.jsx';
import UserLink from './UserLink.jsx';

export class ChatMessage extends Component {
    doRender = ({ token, user, createdTime, content, style, ...props }) =>
    <div key={token} style={{ margin: '5px', padding: '10px', ...style }} {...props}>
        <div>
            <UserLink user={user} />
            <PrettyDate date={createdTime} style={{ float: 'right' }}/>
        </div>
        <ReactMarkdown source={content} />
    </div>

    render = () => this.doRender(this.props.message);
}

export default class Chat extends Component {
    state = {
        fetching: false,
    };

    constructor(props) {
        super(props);
        this.scrollVariables = {
            scrollHeight: 0,
            scrollTop: 0,
            clientHeight: 0
        }
    }

    onComponentDidMount = () => {
        this.props.fetchMessages();
    }

    componentDidUpdate() {
        console.log({
            scrollHeight: this.scrollable.scrollHeight,
            scrollTop: this.scrollable.scrollTop,
            clientHeight: this.scrollable.clientHeight
        });

        this.updateScrolling();
    }

    updateScrolling = () => {
        let scrollVariables = {
            scrollHeight: this.scrollable.scrollHeight,
            scrollTop: this.scrollable.scrollTop,
            clientHeight: this.scrollable.clientHeight
        }

        if (scrollVariables.scrollHeight > this.scrollVariables.scrollHeight) {
            if (this.scrollVariables.scrollTop + this.scrollVariables.clientHeight >= this.scrollVariables.scrollHeight) {
                this.scrollable.scrollTop = this.scrollable.scrollHeight - this.scrollable.clientHeight;
            }
            else {
                this.scrollable.scrollTop += scrollVariables.scrollHeight - this.scrollVariables.scrollHeight;
            }
        }

        this.scrollVariables = scrollVariables;
    }

    onScroll = (event) => {
        if (this.scrollable.scrollTop < 200 && !this.state.fetching) {
            this.setState({ fetching: true });
            console.log('fetching');
            this.props.fetchMessages().then(() => {
                this.setState({ fetching: false });
                console.log('done fetchiing');
            });
        }

        this.updateScrolling();
    }

    doRender = ({ messages, fetchMessages, style, ...props }) =>
    <div
        {...props}
        style={{ overflowY: 'auto', overflowY: 'scroll', height: '600px', ...style }}
        onScroll={this.onScroll}
        ref={e => this.scrollable = e}
    >
        {messages.map(m => <ChatMessage key={m.token} message={m}/>)}
    </div>

    render = () => this.doRender(this.props);
}

Chat.propTypes = {
    messages: PropTypes.array.isRequired,
    fetchMessages: PropTypes.func.isRequired,
};

