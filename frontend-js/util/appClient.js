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

    signOut() {
        let fetchOptions = {
            credentials: "same-origin",
            headers: { accept: 'text/html', 'X-XSRF-TOKEN': getCsrfToken() }
        }

        return fetch('/signout', fetchOptions)
            .then(response => {
                if (response.status != 200) {
                    throw new Error("Unexpected status code on signout: " + response.status);
                }
                return response.url;
            });
    }
}
