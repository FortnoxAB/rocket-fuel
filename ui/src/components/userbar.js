import React from 'react'
import Coins from '../components/utils/coins';

class Userbar extends React.Component {
	constructor(props) {
		super(props);
		this.state = {
			user: null
		}
	}

	componentDidUpdate(prevProps) {
		if (prevProps.signedIn !== this.props.signedIn && this.props.signedIn === true) {
			this.setState({
				user: JSON.parse(window.sessionStorage.getItem('user'))
			});
		}
	}

	getUser() {
		if (!this.props.signedIn || !this.state.user) {
			return 'Ej inloggad';
		}

		return <span>{this.state.user.name} <Coins amount="1337" /> </span>
	}

	render() {
		return (
			<div>
				<div className="userbar">
					{this.getUser()}
				</div>
				<div className="userbar-placeholder" />
			</div>
		);
	}
}

export default Userbar;
