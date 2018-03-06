import 'babel-polyfill';
import React from 'react';
import ReactDOM from 'react-dom';
import { Provider } from 'react-redux';
import { createStore, applyMiddleware } from 'redux';
import thunk from 'redux-thunk';

import AppClient from 'util/AppClient.js';
import App from 'containers/App.jsx'
import MockViewer from 'components/MockViewer.jsx';
import rootReducer, { getCurrentUser, createMessage } from 'reducers.js';
import log from 'middleware/log.js';

const app = document.getElementById('app');
if (app) {
    const client = new AppClient('/api/v1');
    const store = createStore(rootReducer, applyMiddleware(thunk.withExtraArgument(client), log));
    store.dispatch(getCurrentUser());

    ReactDOM.render(<Provider store={store}><App /></Provider>, app);
}

const mockViewer = document.getElementById('mock-viewer');
if (mockViewer)
    ReactDOM.render(<MockViewer />, mockViewer);
