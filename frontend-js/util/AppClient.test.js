import JsonClient from 'util/JsonClient.js';
import AppClient from 'util/AppClient.js';


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

describe('AppClient tests', () => {
    test('getCurrentUser', async () => {
        const mockUser = { id: 1, name: 'jimbob' };
        const jsonClient = new JsonClient({ fetch: async () => new MockedResponse(200, mockUser)});
        const appClient = new AppClient('dummy', { jsonClient });

        await expect(appClient.getCurrentUser()).resolves.toEqual(mockUser);
    })
});
