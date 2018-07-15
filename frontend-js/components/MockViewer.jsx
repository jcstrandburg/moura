import { Component } from 'react';
import PropTypes from 'prop-types';

import Header from 'components/Header.jsx';
import ActionLink from 'components/ActionLink.jsx';
import DiscussionThread, { Message } from 'components/DiscussionThread.jsx';
import PrettyDate from 'components/PrettyDate.jsx';
import { LazyLoadRequestCache, LazyLoadDataService } from 'util/lazyLoad.js';
import toDict from 'util/toDict.js';

let mockUsers = [
    {
        name: 'Mock User',
        token: '4a56159e-f2a2-40b1-ab45-bf3fe9597f5f',
    },
    {
        name: 'Demo User 1',
        token: '337029c9-2183-40c6-b3bd-4dc42ff63992',
    },
    {
        name: 'Bobbert Bobberson',
        token: '2a0534ca-0684-49d5-97da-5a556d9eafa9',
    },
    {
        name: 'Sam Smith',
        token: '586f618f-e46f-40f5-8f74-0da1e6a5f336',
    },
    {
        name: 'Jimbo Jimmerson',
        token: 'a70fec41-2837-4ccd-a9f2-b548ea4cf0ef',
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
let yesterday = new Date();
yesterday.setDate(yesterday.getDate() - 1);
let lastYear = new Date();
lastYear.setFullYear(lastYear.getFullYear() - 1);

export default class MockViewer extends Component {
    render = () =>
    <div>
        <Header user={mockUsers[0]} signOut={() => {}} />
        <MockSelector defaultMock="DiscussionThread">
            <Mock mockName="ActionLink">
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
                <DiscussionThread messages={[
                    { user: mockUsers[0], createdTime: new Date('2018-03-24T10:20:48Z'), content: "Neque porro quisquam est qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit." },
                    { user: mockUsers[1], createdTime: new Date('2018-03-24T10:45:48Z'), content: "Sed in purus sagittis erat pharetra dapibus. In commodo at leo porttitor dictum." }
                ]}/>
            </Mock>
        </MockSelector>
    </div>;
}
