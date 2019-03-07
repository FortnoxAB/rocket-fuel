import React from 'react'
import { AppContext } from '../appcontext';
import Logo from './utils/logo';
import Coins from './utils/coins';

class UserBar extends React.Component {
	getUser() {
		return <span>{this.context.state.user.name}</span>
	}

	render() {
		return (
			<div>
				<div className="user-bar">
					<Logo size="small" color="light" />
					<div className="user">
						<div className="name">{this.getUser()}</div> <Coins amount={172} />
					</div>
				</div>
				<div className="user-bar-placeholder" />
			</div>
		);
	}
}

UserBar.contextType = AppContext;

export default UserBar;
