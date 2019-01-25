import React from 'react'
import { BrowserRouter as Router } from 'react-router-dom'

import { Provider } from './appcontext';
import Sidebar from './components/sidebar';

import Routing from './views/routing';

import './style/style.less';

class App extends React.Component {
	constructor(props) {
		super(props);
	}
	render() {
		return (
			<Provider>
				<Router>
					<div className="app">
						<Sidebar />
						<Routing />
					</div>
				</Router>
			</Provider>
		);
	}
}

export default App;
