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
    }

    getHeaders() {
        return new Headers(this.getCustomHeaders
            ? Object.assign({}, defaultHeaders, this.getCustomHeaders())
            : defaultHeaders);
    }

    processRequest(uri, method, body = null) {
        let fetchOptions = {
            credentials: "same-origin",
            method: method,
            body: body,
            headers: this.getHeaders()
        }

        let f = this.customFetch || fetch;
        return f(uri, fetchOptions).then(response => new WrappedResponse(response));
    }

    get(uri) {
        return this.processRequest(uri, 'GET');
    }

    post(route, body) {
        return this.processRequest(route, 'POST', JSON.stringify(body));
    }
}
