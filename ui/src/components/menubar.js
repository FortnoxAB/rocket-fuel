import React from 'react';
import { t } from 'ttag';
import { NavLink } from 'react-router-dom'

class MenuBar extends React.Component {
	logoutUser() {
		console.log('logout');
	}

	render() {
		return (
			<div>
				<div className="menu-bar">
					<ul>
						<li><NavLink activeClassName="active" exact to="/">{t`Overview`}</NavLink></li>
						<li><NavLink activeClassName="active" to="/questions">{t`Questions`}</NavLink></li>
						{/*<li><NavLink activeClassName="active" to="/bloglist">{t`Blogs`}</NavLink></li>
						<li><NavLink activeClassName="active" to="/users">{t`Users`}</NavLink></li>
						<li><NavLink activeClassName="active" to="/search">{t`Search`}</NavLink></li>
						<li><NavLink activeClassName="active" to="/faq">{t`FAQ`}</NavLink></li>*/}
						<li><a onClick={this.logoutUser.bind(this)}>{t`Logout`}</a></li>
					</ul>
				</div>
				<div className="menu-bar-placeholder" />
			</div>
		);
	}
}

export default MenuBar;
