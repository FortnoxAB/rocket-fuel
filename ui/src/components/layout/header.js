import React from 'react'
import { t } from 'ttag';
import { withRouter } from 'react-router-dom';
import { UserContext } from '../../usercontext';
import Logo from '../utils/logo';
import Coins from '../utils/coins';
import MenuBar from './menubar';
import InputField from '../forms/inputfield';
import Dropdown from '../utils/dropdown';
import Button from '../forms/button';
import * as User from '../../models/user';

class Header extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            quickSearch: '',
            isDropdownOpen: false,
            isCreateDropdownOpen: false
        }
    }

    handleChange(node) {
        const value = node.target.value;
        this.setState({
            quickSearch: value
        });
    }

    getUser() {
        return <span>{this.context.state.user.name}</span>
    }


    navigate(url) {
        this.props.history.push(url);
    }

    toggleDropdown() {
        this.setState({
            isDropdownOpen: !this.state.isDropdownOpen
        });
    }

    closeUserDropdown() {
        if (!this.state.isDropdownOpen) {
            return;
        }
        this.setState({
            isDropdownOpen: false
        });
    }

    toggleCreateDropdown() {
        this.setState({
            isCreateDropdownOpen: !this.state.isCreateDropdownOpen
        });
    }

    closeCreateDropdown() {
        if (!this.state.isCreateDropdownOpen) {
            return;
        }
        this.setState({
            isCreateDropdownOpen: false
        });
    }

    logoutUser() {
        console.log('logout');
    }

    render() {
        return (
            <div>
                <div className="header">
                    <div className="flex">
                        <Logo onClick={this.navigate.bind(this, '/')} className="pointer"
                              size="small" color="light" />
                        <MenuBar />
                    </div>
                    <div className="flex-grow">
                        {/*<InputField
                        type="text"
                        rounded
                        icon="fa-search"
                        label={t`Quick search`}
                        value={this.state.quickSearch}
                        onChange={this.handleChange.bind(this)}
                    />*/}
                    </div>
                    <div>
                        <div className="item">
                        <Button border small onClick={this.navigate.bind(this, '/create/question')}>
                            <i className="fa fa-pen" /> {t`New question`}
                        </Button>
                            {/*<Dropdown
                                isOpen={this.state.isCreateDropdownOpen}
                                close={this.closeCreateDropdown.bind(this)}
                            >
                                <ul>
                                    <li onClick={this.navigate.bind(this, '/create/question')}>New question</li>
                                </ul>
                            </Dropdown>*/}
                        </div>
                        <div className="user item">
                            <Button color="primary" text onClick={this.toggleDropdown.bind(this)}>
                                {this.getUser()}
                                <img className="profile-picture" src={this.context.state.user.picture} alt="user" />
                            </Button>
                            <Dropdown
                                isOpen={this.state.isDropdownOpen}
                                close={this.closeUserDropdown.bind(this)}
                            >
                                <ul>
                                    <li onClick={this.logoutUser.bind(this)}>{t`Logout`}</li>
                                </ul>
                            </Dropdown>
                        </div>
                    </div>
                </div>
                <div className="header-placeholder" />
            </div>
        );
    }
}

export default withRouter(Header);

Header.contextType = UserContext;
