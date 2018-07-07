import { Set } from "core-js";

export const defaultHeaders = {
    'Accept': 'application/json',
    'Content-Type': 'application/json',
}

export class WrappedResponse {
    constructor(response) {
        this.response = response;
    }

    async handle(handlers) {
        const response = this.response;
        if (handlers.hasOwnProperty(response.status)) {
            const jsonBody = await response.json();
            return handlers[response.status](jsonBody);
        }

        throw new Error("Unhandled status code "+response.status);
    }
}

export default class JsonClient {
    constructor(options={}) {
        this.getCustomHeaders = options.getCustomHeaders || null;
        this.customFetch = options.fetch || null;
        this.fetchOptions = options.fetchOptions || {};
    }

    getHeaders() {
        return new Headers(this.getCustomHeaders
            ? Object.assign({}, defaultHeaders, this.getCustomHeaders())
            : defaultHeaders);
    }

    buildUri(baseUri, params = {}) {
        const re = /\{([a-zA-Z0-9]*)\}/g;

        let pathParamsSet = new Set((baseUri.match(re) || []).map(it => it.substr(1, it.length - 2)));

        let pathString = baseUri.replace(re, (match, offset, string) => {
            return encodeURIComponent(params[match.substr(1, match.length - 2)]);
        });

        let queryString = Object.keys(params)
            .filter(key => !pathParamsSet.has(key))
            .filter(key => params[key] != null && params[key] != undefined)
            .map(key => key + '=' + encodeURIComponent(params[key]))
            .join('&');

        return queryString.length > 0 ? pathString + '?' + queryString : pathString;
    }

    processRequest(uri, params, method, body = null) {
        let fetchOptions = {
            credentials: this.fetchOptions.credentials || "same-origin",
            method: method,
            body: body,
            headers: this.getHeaders()
        }

        let f = this.customFetch || fetch;
        return f(this.buildUri(uri, params), fetchOptions).then(response => new WrappedResponse(response));
    }

    get(uri, params) {
        return this.processRequest(uri, params, 'GET');
    }

    post(uri, params, body) {
        return this.processRequest(uri, params, 'POST', JSON.stringify(body));
    }
}
