import React from 'react'
import { t } from 'ttag';
import { withRouter } from 'react-router-dom';
import { UserContext } from '../../usercontext';
import Logo from '../utils/logo';
import Coins from '../utils/coins';
import MenuBar from './menubar';
import Dropdown from '../utils/dropdown';
import Tooltip from '../utils/tooltip';
import Button from '../forms/button';
import * as User from '../../models/user';
import HeaderSearch from './headersearch';
import Dialog from '../utils/dialog';
import SelectField from '../forms/selectfield';

class Header extends React.Component {
    constructor(props) {
        super(props);

        this.themes = [
            {
                title: t`Light`,
                value: 'light',
                selected: false
            },
            {
                title: t`Dark`,
                value: 'dark',
                selected: false
            }
        ];

        this.state = {
            quickSearch: '',
            isDropdownOpen: false,
            isSettingsOpen: false,
            themes: this.themes
        }
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

    logoutUser() {
        this.GoogleAuth = gapi.auth2.getAuthInstance();
        this.GoogleAuth.signOut().catch(() => {
            User.signOut();
        });
    }

    openSettings() {
        this.setState({
            isDropdownOpen: false,
            isSettingsOpen: true
        });
    }

    closeSettings() {
        this.setState({
            isSettingsOpen: false
        });
    }

    onChangeTheme(newItem) {
        const newThemesState = this.state.themes.map((item) => {
            item.selected = false;
            if (newItem.value === item.value) {
                item.selected = true
            }

            return item;
        });

        localStorage.setItem('theme', newItem.value);

        this.context.setState({
            theme: newItem.value
        });

        this.setState({
            themes: newThemesState
        });
    }

    renderDialog() {
        return (
            <Dialog isOpen={this.state.isSettingsOpen} title={t`Settings`}>
                <h3>{t`Theme`}</h3>
                <SelectField options={this.state.themes} onChange={this.onChangeTheme.bind(this)}/>
                <div className="flex flex-end padded-top-large">
                    <Button color="primary" text onClick={this.closeSettings.bind(this)}>{t`Close`}</Button>
                </div>
            </Dialog>
        );
    }

    render() {
        return (
            <div>
                {this.renderDialog()}
                <div className="header">
                    <div className="flex">
                        <Logo onClick={this.navigate.bind(this, '/')} className="pointer"
                              size="small" color="light"
                        />
                        <MenuBar />
                    </div>
                    <HeaderSearch />
                    <div>
                        <div className="item">
                        <Tooltip content={t`New question`}>
                            <Button text onClick={this.navigate.bind(this, '/create/question')}>
                                <i className="fa fa-plus" />
                            </Button>
                        </Tooltip>
                        </div>
                        <div className="user item">
                            <Tooltip content={this.context.state.user.name}>
                                <Button text onClick={this.toggleDropdown.bind(this)}>
                                    <img className="profile-picture" src={this.context.state.user.picture} alt="user" />
                                </Button>
                            </Tooltip>
                            <Dropdown
                                isOpen={this.state.isDropdownOpen}
                                close={this.closeUserDropdown.bind(this)}
                            >
                                <ul>
                                    <li onClick={this.openSettings.bind(this)}>{t`Settings`}</li>
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
