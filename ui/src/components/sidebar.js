import React from 'react'
import { NavLink } from 'react-router-dom'

class Sidebar extends React.Component {
	logoutUser() {
		console.log('logout');
	}

	render() {
		return (
			<div className="sidebar">
				<div className="Logo">
					Nox Overflow <i className="fa fa-diamond" />
				</div>
				<ul>
					<li><NavLink activeClassName="active" exact to="/">Översikt</NavLink></li>
					<li><NavLink activeClassName="active" to="/threads">Trådar</NavLink></li>
					<li><NavLink activeClassName="active" to="/bloglist">Bloggar</NavLink></li>
					<li><NavLink activeClassName="active" to="/users">Användare</NavLink></li>
					<li><NavLink activeClassName="active" to="/search">Sök</NavLink></li>
					<li><NavLink activeClassName="active" to="/faq">FAQ</NavLink></li>
					<li><a onClick={this.logoutUser.bind(this)}>Logga ut</a></li>
				</ul>
			</div>
		);
	}
}

export default Sidebar;
