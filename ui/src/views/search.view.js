import React from 'react';
import { Route, withRouter, NavLink } from 'react-router-dom'


const SearchView = () => {
	return (
		<div>
			<div className="content">
				Sök användare eller trådar...
			</div>
		</div>
	);
};

export default withRouter(SearchView);
