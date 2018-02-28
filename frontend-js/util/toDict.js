const stuff = (map, key, it) => {
    map[key] = it;
    return map;
}

export default function toDict(data, keySelector) {
    if (data == null)
        throw new Error("Argument null: data");

    if (!data.reduce)
        throw new Error("Data must have a reduce method");

    return data.reduce((dict, it) => stuff(dict, keySelector(it), it), {});
}
