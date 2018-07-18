import { Component } from 'react';
import PropTypes from 'prop-types';
import uuid from 'uuid';
import loremIpsum from 'lorem-ipsum';

import Header from 'components/Header.jsx';
import ActionLink from 'components/ActionLink.jsx';
import DiscussionThread, { Message } from 'components/DiscussionThread.jsx';
import Chat from 'components/Chat.jsx';
import PrettyDate from 'components/PrettyDate.jsx';
import { LazyLoadRequestCache, LazyLoadDataService } from 'util/lazyLoad.js';
import toDict from 'util/toDict.js';
import { pickOne } from 'util/array.js';

let filler = () => loremIpsum({ count: Math.random() * 2 + 1 });

let mockUsers = [
    {
        name: 'Mock User',
        token: uuid(),
    },
    {
        name: 'Demo User 1',
        token: uuid(),
    },
    {
        name: 'Bobbert Bobberson',
        token: uuid(),
    },
    {
        name: 'Sam Smith',
        token: uuid(),
    },
    {
        name: 'Jimbo Jimmerson',
        token: uuid(),
    },
];

const Mock = ({ children }) => <div>{children}</div>;
Mock.propTypes = {
    mockName: PropTypes.string.isRequired
};

class MockSelector extends Component {
    state = {
        selectedMock: this.props.defaultMock || '',
    }

    onSelectedMockChange = (event) => {
        this.setState({ selectedMock: event.target.value });
    }

    filterChildren = (selectedMock) => {
        return this.props.children.filter(x => x.props.mockName == this.state.selectedMock);
    }

    getChildOptions = () => {
        return this.props.children
            .filter(x => x.type == Mock)
            .map(x => x.props.mockName)
            .map(name => <option value={name} key={name}>{name}</option>)
    }

    render = () => 
        <div>
            <select id='mock-selector' value={this.state.selectedMock} onChange={this.onSelectedMockChange}>
                <option value=''></option>
                {this.getChildOptions()}
            </select>
            <div style={{padding: '2em', margin: '2em', border: 'solid 1px black'}}>
                {this.filterChildren(this.state.selectedMock)}
            </div>
        </div>
}

MockSelector.propTypes = {
    defaultMock: PropTypes.string
}

let now = new Date();
let fiveMinutesAgo = new Date();
fiveMinutesAgo.setMinutes(fiveMinutesAgo.getMinutes() - 5);
let fiftyNineMinutesAgo = new Date();
fiftyNineMinutesAgo.setMinutes(fiftyNineMinutesAgo.getMinutes() - 59);
let twoHoursAgo = new Date();
twoHoursAgo.setHours(twoHoursAgo.getHours() - 2);
let yesterday = new Date();
yesterday.setDate(yesterday.getDate() - 1);
let lastYear = new Date();
lastYear.setFullYear(lastYear.getFullYear() - 1);

function concatMessages(messages, count) {
    let time = messages.map(x => x.createdTime).sort((a, b) => a - b).find(x => x) || new Date();

    var newMessages = [];
    for (var i = 0; i < count; i++) {
        time = new Date(time.setMinutes(time.getMinutes() - Math.random() * 60));
        newMessages.push({ token: uuid(), user: pickOne(mockUsers), createdTime: time, content: filler() })
    }

    return messages.concat(newMessages).sort((a, b) => {
        return a.createdTime < b.createdTime ? -1 :
            a.createdTime > b.createdTime ? 1 :
            0;
    });
}

function appendMessage(messages) {
    return messages.concat([{ token: uuid(), user: pickOne(mockUsers), createdTime: new Date(), content: filler() }]);
}

export default class MockViewer extends Component {
    state = {
        chatMessages: concatMessages([], 10),
    };

    loadMoreMessages = () => {
        setTimeout(() => this.fetchMessages)
    }

    appendMessage = () => {
        this.setState({ chatMessages: appendMessage(this.state.chatMessages) });
        setTimeout(this.appendMessage, Math.random() * 10000 + 500);
    }

    componentDidMount = () => this.appendMessage();

    fetchMessages = () => new Promise((resolve, reject) => {
        setTimeout(() => {
            this.setState({ chatMessages: concatMessages(this.state.chatMessages, 3)});
            resolve();
        }, Math.random()*1000 + 250);
      });

    render = () =>
    <div>
        <Header user={mockUsers[0]} signOut={() => {}} />
        <MockSelector defaultMock="Chat">
            <Mock mockName="ActionLink">
                <div>
                    <ActionLink text="Click Me" action={() => alert('click')} />
                </div>
                <div>
                    <ActionLink text="With Style" style={{ color: 'purple' }} action={() => alert('click')} />
                </div>
            </Mock>
            <Mock mockName="PrettyDate">
                <div><PrettyDate date={now} /></div>
                <div><PrettyDate date={fiveMinutesAgo} /></div>
                <div><PrettyDate date={fiftyNineMinutesAgo} /></div>
                <div><PrettyDate date={twoHoursAgo} /></div>
                <div><PrettyDate date={yesterday} /></div>
                <div><PrettyDate date={lastYear} /></div>
            </Mock>
            <Mock mockName="DiscussionThreadMessage">
                <Message message={{ user: pickOne(mockUsers), createdTime: new Date('2018-03-24T10:20:48Z'), content: filler() }} />
            </Mock>
            <Mock mockName="DiscussionThread">
                <DiscussionThread messages={[
                    { token: uuid(), user: pickOne(mockUsers), createdTime: new Date('2018-03-24T10:20:48Z'), content: filler() },
                    { token: uuid(), user: pickOne(mockUsers), createdTime: new Date('2018-03-24T10:45:48Z'), content: filler() }
                ]}/>
            </Mock>
            <Mock mockName="Chat">
                <Chat messages={this.state.chatMessages} fetchMessages={this.fetchMessages}/>
            </Mock>
        </MockSelector>
    </div>;
}
 