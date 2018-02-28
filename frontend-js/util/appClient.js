import JsonClient from 'util/JsonClient.js';
import * as httpCodes from 'util/httpCodes.js';

export function getCsrfToken() {
    var b = document.cookie.match('(^|;)\\s*' + 'XSRF-TOKEN' + '\\s*=\\s*([^;]+)');
    return b ? b.pop() : '';
}

export default class AppClient {
    constructor(apiBase, options={}) {
        this.apiBase = apiBase;

        const customHeaders = { 'X-XSRF-TOKEN': getCsrfToken() };
        this.jsonClient = options.jsonClient || new JsonClient({ getCustomHeaders: () => customHeaders });
    }

    getCurrentUser = () => this.jsonClient.get(this.apiBase + '/users/me')
        .then(response => response.handle({
            [httpCodes.OK]: user => user,
        }));

    createMessage = (message) => this.jsonClient.post(this.apiBase + '/messages', message)
        .then(response => response.handle({
            [httpCodes.CREATED]: message => message,
        }));

    signOut() {
        let fetchOptions = {
            credentials: "same-origin",
            method: 'POST',
            headers: { accept: 'text/html', 'X-XSRF-TOKEN': getCsrfToken() }
        }

        return fetch('/logout', fetchOptions)
            .then(response => {
                if (response.status != 200) {
                    throw new Error("Unexpected status code on signout: " + response.status);
                }
                return response.url;
            });
    }
}
