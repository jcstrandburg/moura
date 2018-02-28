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
