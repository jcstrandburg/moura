import React, { Component } from 'react';
import PropTypes from 'prop-types';
import ReactMarkdown from 'react-markdown';

import PrettyDate from './PrettyDate.jsx';
import UserLink from './UserLink.jsx';

function hashCode(str) { // java String#hashCode
    var hash = 0;
    for (var i = 0; i < str.length; i++) {
       hash = str.charCodeAt(i) + ((hash << 5) - hash);
    }
    return hash;
} 

function intToRGB(i){
    var c = (i & 0x00FFFFFF)
        .toString(16)
        .toUpperCase();

    return "00000".substring(0, 6 - c.length) + c;
}

export class ChatMessage extends Component {
    doRender = ({ token, user, createdTime, content, style, ...props }) =>
    <div key={token} style={{ margin: '5px', padding: '10px', ...style }} {...props}>
        <div>
            <UserLink user={user} />
            <PrettyDate date={createdTime} style={{ float: 'right', backgroundColor: '#'+intToRGB(hashCode(token))+'30' }}/>
        </div>
        <ReactMarkdown source={content} />
    </div>

    render = () => this.doRender(this.props.message);
}

var scrollCounter = 1;

export default class Chat extends Component {
    state = {
        fetching: false,
    };

    constructor(props) {
        super(props);
        this.scrollVariables = {
            scrollHeight: 0,
            scrollTop: 0,
            clientHeight: 0,
            offsetHeight: 0,
        };
    }

    componentDidMount = () => {
        this.props.fetchMessages();
    }

    componentDidUpdate(prevProps) {
        this.updateScrolling(prevProps);
    }

    updateScrolling = (prevProps) => {
        let scrollVariables = {
            scrollHeight: this.scrollable.scrollHeight,
            scrollTop: this.scrollable.scrollTop,
            clientHeight: this.scrollable.clientHeight,
            offsetHeight: this.scrollable.offsetHeight,
        };

        if (scrollVariables.scrollHeight > this.scrollVariables.scrollHeight) {
            let oldestMessage = this.props.messages.reduce((prev, curr) => {
                curr = curr || prev;
                return prev.createdTime < curr.createdTime ? prev : curr;
            });

            let minCreatedTime = this.props.messages.map(x => x.createdTime)
                .sort((a, b) => a - b)
                .find(x => x) || new Date();

            var deets;
            if (this.scrollVariables.scrollTop + this.scrollVariables.clientHeight >= this.scrollVariables.scrollHeight) {
                this.scrollable.scrollTop = this.scrollable.scrollHeight - this.scrollable.clientHeight;
                deets = { action: 'locked to bottom' };
            } else if (minCreatedTime < this.minCreatedTime) {
                let delta = scrollVariables.scrollHeight - this.scrollVariables.scrollHeight;
                this.scrollable.scrollTop += delta;
                deets = { action: 'pushing down', delta };
            } else {
                deets = { action: 'no action' };
            }

            console.log({ old: this.scrollVariables, new: scrollVariables, scrollTop: this.scrollable.scrollTop, deets });

            this.minCreatedTime = minCreatedTime;
        }

        this.scrollVariables = scrollVariables;
    }

    onScroll = (event) => {
        // console.log(scrollCounter++);

        if (this.scrollable.scrollTop < this.props.height * 2 && !this.state.fetching) {
            this.setState({ fetching: true });
            console.log('fetching');
            this.props.fetchMessages().then(() => {
                this.setState({ fetching: false });
                console.log('done fetchiing');
            });
        }

        this.updateScrolling(this.props);
    }

    doRender = ({ messages, fetchMessages, style, ...props }) =>
    <div
        {...props}
        style={{ overflowY: 'auto', overflowY: 'scroll', height: this.props.height, ...style }}
        onScroll={this.onScroll}
        ref={e => this.scrollable = e}
    >
        <div style={{height: this.props.height, textAlign: 'center'}}>Loading...</div>
        {messages.map(m => <ChatMessage key={m.token} message={m}/>)}
    </div>

    render = () => this.doRender(this.props);
}

Chat.defaultProps = {
    height: 600,
}

Chat.propTypes = {
    messages: PropTypes.array.isRequired,
    fetchMessages: PropTypes.func.isRequired,
    height: PropTypes.number
};

