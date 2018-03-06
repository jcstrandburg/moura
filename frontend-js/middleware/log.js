const log = store => next => action => {
    const returnMe = next(action);
    console.log({ action, endState: store.getState() })
    return returnMe;
};
export default log;
