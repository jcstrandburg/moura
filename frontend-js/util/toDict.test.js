import toDict from 'util/toDict.js';

test('toDict works', () => {
    const input = [
        { id: 3, name: 'jim' },
        { id: 2, name: 'bob' },
        { id: 1, name: 'joe' }
    ];

    const expectedOutput = {
        [1]: { id: 1, name: 'joe' },
        [2]: { id: 2, name: 'bob' },
        [3]: { id: 3, name: 'jim' },
    }

    expect(toDict(input, it => it.id)).toEqual(expectedOutput);
});
