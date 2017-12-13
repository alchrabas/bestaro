'use strict';

var webpack = require('webpack');
var HardSourceWebpackPlugin = require('hard-source-webpack-plugin');
var ExtractTextPlugin = require("extract-text-webpack-plugin");

module.exports = {
    entry: './frontend/app/assets/js/index.jsx',
    output: {
        filename: './frontend/target/web/webpack/main/js/bundle.js'
    },
    module: {
        rules: [
            {
                test: /\.jsx?$/,
                exclude: /node_modules/,
                use: {
                    loader: 'babel-loader',
                    options: {
                        presets: ['es2015', 'react']
                    }
                }
            },
            {
                test: /\.s?css$/,
                use: [{
                    loader: "style-loader"
                }, {
                    loader: "css-loader"
                }, {
                    loader: "sass-loader"
                }]
            }
        ]
    },
    resolve: {
        extensions: ['.js', '.jsx']
    },
    plugins: [
        new HardSourceWebpackPlugin(),
        new webpack.ContextReplacementPlugin(/moment[\/\\]locale$/, /en/),
        new ExtractTextPlugin({
            filename: "styles.css",
            disable: process.env.NODE_ENV === "development"
        })
    ]
};
