import { LazyLoadRequestCache, LazyLoadDataService } from 'util/lazyLoad.js';

describe('LazyLoadRequestCache', () => {
    test('Key requests are handled correctly', () => {
        let loadCount = { a: 0, b: 0, c: 0 };
        let cache = new LazyLoadRequestCache();
        let lazyLoad = (k) => loadCount[k]++;

        expect(loadCount).toEqual({ a: 0, b: 0, c: 0 });

        cache.startLazyLoad('a', lazyLoad);
        expect(loadCount).toEqual({ a: 1, b: 0, c: 0 });

        cache.startLazyLoad('b', lazyLoad);
        expect(loadCount).toEqual({ a: 1, b: 1, c: 0 });

        cache.startLazyLoad('a', lazyLoad);
        cache.startLazyLoad('a', lazyLoad);
        expect(loadCount).toEqual({ a: 1, b: 1, c: 0 });
    })
});

describe('LazyLoadDataService', () => {
    test('returns correct values', () => {
        let data = {
            'a': 'value_a'
        };
        let service = new LazyLoadDataService(
            new LazyLoadRequestCache(),
            key => null,
            data,
            key => 'loading_'+key);

        expect(service.get('a')).toEqual('value_a');
        expect(service.get('b')).toEqual('loading_b');
    });
    test('lazyLoad is called appropriately', () => {
        let loadCount = { a: 0, b: 0, c: 0 };

        let data = {
            'a': 'value_a'
        };
        let service = new LazyLoadDataService(
            new LazyLoadRequestCache(),
            key => {
                loadCount[key]++;
            },
            data,
            key => 'loading_'+key);

        ['a', 'b', 'b', 'b'].forEach(k => service.get(k));
        expect(loadCount).toEqual({ a: 0, b: 1, c: 0 });
    });
});
