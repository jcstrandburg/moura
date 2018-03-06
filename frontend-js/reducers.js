import { combineReducers } from 'redux';


export const signOut = () => (dispatch, getState, appClient) =>
    appClient.signOut().then(url => window.location = url);

const SET_CURRENT_USER = 'SET_CURRENT_USER';
export const setCurrentUser = currentUser => ({ type: SET_CURRENT_USER, currentUser });

export const getCurrentUser = () => (dispatch, getState, appClient) =>
    appClient.getCurrentUser().then(currentUserResponse => dispatch(setCurrentUser(currentUserResponse)));

const MESSAGE_UPDATED = 'MESSAGE_UPDATED';
export const messageUpdated = message => ({ type: MESSAGE_UPDATED, message })

export const createMessage = message => (dispatch, getState, appClient) =>
    appClient.createMessage(message)
        .then(message => dispatch(messageUpdated(message)));

const initialState = {
    currentUser: {
        organizations: [],
        user: {
            id: -1,
            name: "Loading..."
        },
    },
    debug: {
        actions: [],
    },
};

const currentUser = (state = initialState.currentUser, action) => {
    switch(action.type) {
    case SET_CURRENT_USER:
        return action.currentUser
    }

    return state;
}

const debug = (state = initialState.debug, action) => {
    return {
        actions: state.actions.concat([action]),
    };
}

const rootReducer = combineReducers({
    currentUser,
    debug,
});
export default rootReducer;
