var webpack = require('webpack');
var path = require('path');

var config = {
    entry: './main.js',

    output: {
        path: __dirname+'/',
        filename: '../src/main/resources/static/index.js',
    },

    devServer: {
        inline: true,
        port: 8080
    },

    devtool: 'cheap-module-eval-source-map',

    plugins: [
        new webpack.ProvidePlugin({
            "React": "react",
        }),
    ],

    module: {
        loaders: [
            {
                test: /\.jsx?$/,
                exclude: /node_modules/,
                loader: 'babel-loader',
            }
        ]
    },

    resolve: {
        modules: [
            path.resolve('./'),
            'node_modules'
        ]
    },
}

module.exports = config;
