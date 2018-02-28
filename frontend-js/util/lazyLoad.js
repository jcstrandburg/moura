/**
 * Callback for lazy loading a data element. This callback must handle the population of the target data set, callers of this callback WILL NOT HANDLE THIS.
 * @callback lazyLoadCallback
 * @param {*} key - The unique identifier for the object to be loaded.
  */

/**
 * Manages lazy load requests, preventing duplicate requests.
  */
export class LazyLoadRequestCache {
    keysLoaded = {};

    /**
     * Start a lazy load operation for the given key, if one has not already been started within the lifetime of this object
     * @param {*} key - Unique identifier for the requested element.
     * @param {lazyLoadCallback} lazyLoad - Starts the lazy load process for a given key. This is injected at each invokation in order to facilitate injection from redux connect operations.
     */
    startLazyLoad(key, lazyLoad) {
        if (this.keysLoaded[key])
            return;

        this.keysLoaded[key] = true;
        lazyLoad(key);
    }
}

/**
 * A repository that manages an external data dictionary.
 * This class is designed to be lightweight, and to not own the data it is handed.
 * This is in the interest of frequent re-instantiation in line with the sort of frequent immutable state changes that occur in a framework like redux.
 * The lazyLoad function provided to this manager MUST HANDLE POPULATION OF THE DATA. The manager will not handle this.
*/
export class LazyLoadDataService {

    /**
     * @param {{startLazyLoad: func}} lazyLoadCache - The cache managing lazy load operations
     * @param {lazyLoadCallback} lazyLoad - Lazy loading function, must handle population of the data set.
     * @param {Object.<*, Object>} data - The dictionary containing the current data to be managed.
     * @param {Object} getMissingValue - A function returning the value to be returned for missing or loading keys.
     */
    constructor(lazyLoadRequestCache, lazyLoad, data = {}, getMissingValue = null) {
        this.data = data
        this.lazyLoad = lazyLoad
        this.lazyLoadRequestCache = lazyLoadRequestCache;
        this.getMissingValue = getMissingValue;
    }

    /**
     * Returns the current value for key in the managed data, or starts a lazy load and returns the default value if the key is not present. 
     * @param {*} key - Unique identifier for the desired data element.
     */
    get(key) {
        if (this.data[key])
            return this.data[key];

        this.lazyLoadRequestCache.startLazyLoad(key, this.lazyLoad);
        return this.getMissingValue ? this.getMissingValue(key) : null;
    }
}