import React from 'react';
import { NavLink } from 'react-router-dom';

class Header extends React.Component {
	printLinks() {
		if (!this.props.links || this.props.links.length <= 0){
			return null;
		}
		return this.props.links.map((link, index) => {
			return (
				<li key={index}>
					<NavLink exact to={link.path} activeClassName="active">
						{link.title}
					</NavLink>
				</li>
			);
		});
	}

	getUser() {
		const user = this.state.user;
		if (!user) {
			return null;
		}

		return (
			<div className="user">
				{user.name}
			</div>
		);
	}

	render() {
		return (
			<div className="header">
				<div className="title-space">
					<div className="title">
						{this.props.title}
					</div>
				</div>
				<ul className="menu">
					{this.printLinks()}
				</ul>
			</div>
		);
	}
}

Header.defaultProps = {
	title: '',
	links: [],
	history: null
};

export default Header;
