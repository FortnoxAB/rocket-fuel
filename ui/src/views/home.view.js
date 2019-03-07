import React from 'react';
import { AppContext } from '../appcontext';

class HomeView extends React.Component {
	constructor(props) {
		super(props);
	}

	getUserDisplayName() {
		const user = this.context.state.user;
		return user.name;
	}

	render() {
		return (
			<div className="content">
				<div className="user-space">
					<h2>{this.getUserDisplayName()}</h2>

				</div>
				<div className="flex-row grow">

				</div>
			</div>
		)

	}
}

HomeView.contextType = AppContext;

export default HomeView;
