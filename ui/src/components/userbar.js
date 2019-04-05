import React from 'react'
import { withRouter } from 'react-router-dom';
import { AppContext } from '../appcontext';
import Logo from './utils/logo';
import Coins from './utils/coins';

class UserBar extends React.Component {
	getUser() {
		return <span>{this.context.state.user.name}</span>
	}

	navigateToMain() {
		this.props.history.push('/');
	}

	render() {
		return (
			<div>
				<div className="user-bar">
					<Logo onClick={this.navigateToMain.bind(this)} className="pointer" size="small" color="light" />
					<div className="user">
						<div className="name"><i className="fa fa-user" /> {this.getUser()}</div> <Coins amount={172} />
					</div>
				</div>
				<div className="user-bar-placeholder" />
			</div>
		);
	}
}

export default withRouter(UserBar);

UserBar.contextType = AppContext;
