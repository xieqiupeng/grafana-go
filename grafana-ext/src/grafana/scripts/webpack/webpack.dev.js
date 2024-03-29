'use strict';

const merge = require('webpack-merge');
const common = require('./webpack.common.js');
const path = require('path');
const webpack = require('webpack');
const HtmlWebpackPlugin = require("html-webpack-plugin");
const ExtractTextPlugin = require("extract-text-webpack-plugin");
const WebpackCleanupPlugin = require('webpack-cleanup-plugin');
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;

module.exports = merge(common, {
  devtool: "eval-source-map",

  entry: {
    dark: './public/sass/grafana.dark.scss',
    light: './public/sass/grafana.light.scss',
    vendor: require('./dependencies'),
  },

  module: {
    rules: [
      require('./sass.rule.js')({
        sourceMap: true, minimize: false
      })
    ]
  },

  plugins: [
    new ExtractTextPlugin({ // define where to save the file
      filename: 'grafana.[name].css',
    }),
    new HtmlWebpackPlugin({
      filename: path.resolve(__dirname, '../../public/views/index.html'),
      template: path.resolve(__dirname, '../../public/views/index.template.html'),
      inject: 'body',
      chunks: ['manifest', 'vendor', 'app'],
    }),
    new webpack.DefinePlugin({
      'process.env': {
        'NODE_ENV': JSON.stringify('development')
      }
    }),
    new webpack.optimize.CommonsChunkPlugin({
      names: ['vendor', 'manifest'],
    }),
    // new BundleAnalyzerPlugin({
    //   analyzerPort: 8889
    // })
  ]
  //,
  //devServer: {
  //  contentBase: "./public/views",//本地服务器所加载的页面所在的目录
  //  historyApiFallback: true,//不跳转
  //  inline: true//实时刷新
  //}
});
