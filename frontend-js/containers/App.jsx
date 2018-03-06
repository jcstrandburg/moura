import { connect } from 'react-redux';

import App from 'components/App.jsx';
import { signOut } from 'reducers.js';


const mapStateToProps = (state) => {
    return { user: state.currentUser.user };
};

const mapDispatchToProps = (dispatch) => ({
    signOut: () => dispatch(signOut()),
});

export default connect(mapStateToProps, mapDispatchToProps)(App);
