import JsonClient, { WrappedResponse } from 'util/JsonClient.js';


// Mock the Headers class constructor so SIT doesn't throw exceptions. Headers aren't actually examined by these tests
global.Headers = x => x;

// TODO: de-duplicate this mock from other tests
class MockedResponse {
    constructor(status, body) {
        this.status = status;
        this.jsonPromise = Promise.resolve(body);
    }

    json = () => this.jsonPromise;
}

describe('WrappedResponse tests', () => {
    const handler = { [200]: body => body };

    test('Handler handles success response correctly', async () => {
        let response = new WrappedResponse(new MockedResponse(200, 'response'));
        await expect(response.handle(handler)).resolves.toEqual('response');
    });

    test('Unhandled status code rejects', async () => {
        const response = new WrappedResponse(new MockedResponse(400, 'response'));
        await expect(response.handle(handler)).rejects.toBeDefined();
    });
});

describe('JsonClient tests', () => {
    const mockedUser = { id: 1, name: 'jimbob' };
    const mockedResponse = new MockedResponse(200, mockedUser);
    const jsonClient = new JsonClient({ fetch: async () => mockedResponse });

    test('get returns expected response', async () => {
        const expectedResponse = new WrappedResponse(mockedResponse);
        await expect(jsonClient.get('dummy')).resolves.toEqual(expectedResponse);
    });

    test('response handler returns body', async () => {
        const userPromise = jsonClient.get('dummy').then(r => r.handle({ [200]: user => user }));
        await expect(userPromise).resolves.toEqual(mockedUser);
    });
});

describe('URI builder tests', () => {
    const uriTemplate = 'https://test.com/users/{userId}';
    const expectedUri = 'https://test.com/users/with%20spaces?param1=123&param2=a%26e%2Cb%3F';
    const params = { userId: 'with spaces', param1: '123', param2: ['a&e', 'b?'] };

    let makeTestClient = () => {
        let capturedRequests = [];
        const jsonClient = new JsonClient({ fetch: async (uri, options) => {
            capturedRequests.push({ uri, options });
            return new MockedResponse(200, {});
        }});
        jsonClient.capturedRequests = capturedRequests;
        return jsonClient;
    }

    test('buildUri without params', () => {
        let client = makeTestClient();
        expect(client.buildUri('https://test.com/users/me')).toEqual('https://test.com/users/me');
    });

    test('buildUri without query string', () => {
        let client = makeTestClient();
        expect(client.buildUri(uriTemplate, { userId: 'abcdef' })).toEqual('https://test.com/users/abcdef');
    });

    test('buildUri with query string', () => {
        let client = makeTestClient();
        expect(client.buildUri(uriTemplate, params)).toEqual(expectedUri);
    });

    test('get with params', async () => {
        let client = makeTestClient();
        await client.get(uriTemplate, params);

        expect(client.capturedRequests[0].uri).toEqual(expectedUri);
    });

    test('post with params', async () => {
        const body = {name: 'whatever'}

        let client = makeTestClient();
        await client.post(uriTemplate, params, body);

        expect(client.capturedRequests[0].uri).toEqual(expectedUri);
        expect(client.capturedRequests[0].options.body).toEqual(JSON.stringify(body));
    });
});
