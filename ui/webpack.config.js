const path                 = require('path');
const CopyPlugin           = require('copy-webpack-plugin');
const HtmlWebpackPlugin    = require('html-webpack-plugin');
const CleanWebpackPlugin   = require('clean-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const webpack = require('webpack');

const reactConfig = {
    presets: [
        '@babel/preset-react',
        [
            '@babel/preset-env',
            {
                'targets': {
                    'browsers': [
                        'last 4 versions',
                        'ie 11'
                    ]
                }
            }
        ]
    ],
    plugins: [
        '@babel/plugin-proposal-class-properties'
    ]
};

const computedPublicPath = '/build/';

module.exports = {
	entry: {
		rocketfuel: ['@babel/polyfill', './src/index.js']
	},
	output: {
		publicPath: computedPublicPath,
        path: path.resolve(__dirname, 'build'),
        filename: '[name].js'
    },
	module: {
		rules: [
			{
				test: /\.js$/,
				exclude: /node_modules/,
				loader: 'babel-loader',
				options: reactConfig
			},
			{
				test: /\.po$/,
				use: [
					{ loader: 'json-loader' },
					{ loader: 'po-gettext-loader' }
				]
			},
			{
				test: /\.scss$/,
				use: [
                    'css-hot-loader',
					{
						loader: MiniCssExtractPlugin.loader,
					},
					'css-loader',
					'sass-loader'
				]
			},
			{
				test: /\.woff(2)?(\?v=[0-9]\.[0-9]\.[0-9])?$/,
				loader: 'url-loader',
				options: {
					limit: 1000,
                    publicPath: computedPublicPath,
					minetype: 'application/font-woff'
				}
			},
			{
				test: /\.(ttf|eot|svg|jpg|png|mp4)(\?v=[0-9]\.[0-9]\.[0-9])?$/,
				loader: 'file-loader'
			}
		]
	},
	devtool: 'source-map', // TODO: Check production flag -> false
	plugins: [
		new MiniCssExtractPlugin({
			filename: 'style.css'
		}),
		new CleanWebpackPlugin('build', {}),
        new CopyPlugin([
            { from: './config.js', to: 'config.js' }
        ]),
        new webpack.DefinePlugin({
            'BUILDTIME': JSON.stringify(new Date().toISOString())
        }),
		new HtmlWebpackPlugin({
			inject: false,
			hash: true,
			template: './src/index.html',
			filename: 'index.html'
		})
	],
	devServer: {
		port: 8083,
		publicPath: computedPublicPath,
        contentBase: path.resolve(__dirname, 'build'),
        historyApiFallback: {
            index: computedPublicPath + '/index.html'
        },
        proxy: {
            '/api/**': {
                target: 'http://localhost:8080',
                secure: false,
                changeOrigin: true
            }
        }
    }
};
