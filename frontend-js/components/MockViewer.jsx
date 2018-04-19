import { Component } from 'react';
import PropTypes from 'prop-types';

import Header from 'components/Header.jsx';
import ActionLink from 'components/ActionLink.jsx';
import DiscussionThread, { Message } from 'components/DiscussionThread.jsx';
import PrettyDate from 'components/PrettyDate.jsx';
import { LazyLoadRequestCache, LazyLoadDataService } from 'util/lazyLoad.js';
import toDict from 'util/toDict.js';

let mockUsers = toDict([
    {
        id: 0,
        name: 'Mock User',
    },
    {
        id: 1,
        name: 'Demo User 1',
    },
    {
        id: 2,
        name: 'Bobbert Bobberson',
    },
    {
        id: 3,
        name: 'Sam Smith',
    },
    {
        id: 4,
        name: 'Jimbo Jimmerson',
    },
], it => it.id);

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
let yesterday = new Date();
yesterday.setDate(yesterday.getDate() - 1);
let lastYear = new Date();
lastYear.setFullYear(lastYear.getFullYear() - 1);

export default class MockViewer extends Component {
    render = () =>
    <div>
        <Header user={mockUsers[0]} signOut={() => {}} />
        <MockSelector defaultMock="DiscussionThreadMessage">
            <Mock mockName="action-link">
                <ActionLink text="Click Me" action={() => alert('click')}/>
            </Mock>
            <Mock mockName="PrettyDate">
                <div><PrettyDate date={now}/></div>
                <div><PrettyDate date={yesterday}/></div>
                <div><PrettyDate date={lastYear}/></div>
            </Mock>
            <Mock mockName="DiscussionThreadMessage">
                <Message message={{ user: mockUsers[0], createdTime: new Date('2018-03-24T10:20:48Z'), content: "Ideations" }} />
            </Mock>
            <Mock mockName="DiscussionThread">
                <DiscussionThread messages={[]}/>
            </Mock>
        </MockSelector>
    </div>;
}
