const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CleanWebpackPlugin = require('clean-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');

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
module.exports = {
	entry: {
		main: ['@babel/polyfill', './src/index.js']
	},
	output: {
		publicPath: '/',
		path: path.resolve(__dirname, 'dist'),
		filename: `rocketfuel.js`
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
				test: /\.less$/,
				use: [
					{
						loader: MiniCssExtractPlugin.loader,
						options: {
							publicPath: '../'
						}
					},
					'css-loader',
					'less-loader'
				]
			},
			{
				test: /\.woff(2)?(\?v=[0-9]\.[0-9]\.[0-9])?$/,
				loader: 'url-loader',
				options: {
					limit: 1000,
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
		new CleanWebpackPlugin('dist', {}),
		new HtmlWebpackPlugin({
			inject: false,
			hash: true,
			template: './src/index.html',
			filename: 'index.html'
		})
	],
	devServer: {
		port: 8083,
		publicPath: '/',
		historyApiFallback: true,
		proxy: {
			'/api/**': {
				target: 'http://localhost:8080',
				secure: false,
				changeOrigin: true
			}
		}
	}
};
