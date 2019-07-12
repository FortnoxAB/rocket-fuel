import React from 'react';
import { t } from 'ttag';
import { NavLink } from 'react-router-dom'

const activeClass = 'active';

class MenuBar extends React.Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <div className="menu">
                <ul>
                    <li>
                        <NavLink activeClassName={activeClass} exact to="/">{t`Overview`}</NavLink>
                    </li>
                    <li>
                        <NavLink activeClassName={activeClass} to="/search">{t`Search`}</NavLink>
                    </li>
                    {/*<li><NavLink activeClassName="active" to="/bloglist">{t`Blogs`}</NavLink></li>
                    <li><NavLink activeClassName="active" to="/users">{t`Users`}</NavLink></li>
                    <li><NavLink activeClassName="active" to="/search">{t`Search`}</NavLink></li>
                    <li><NavLink activeClassName="active" to="/faq">{t`FAQ`}</NavLink></li>*/}
                </ul>
            </div>
        );
    }
}

export default MenuBar;
