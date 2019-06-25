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

class Header extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            quickSearch: '',
            isDropdownOpen: false
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

    navigateToMain() {
        this.props.history.push('/');
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
        console.log('logout');
    }

    render() {
        return (
            <div>
                <div className="header">
                    <div className="flex">
                        <Logo onClick={this.navigateToMain.bind(this)} className="pointer"
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
                        <div className="user" onClick={this.toggleDropdown.bind(this)}>
                            <Button color="primary" text>
                                <i className="fa fa-user" /> {this.getUser()}
                            </Button>
                            {/*<Coins amount={172} />*/}
                        </div>
                        <Dropdown
                            isOpen={this.state.isDropdownOpen}
                            close={this.closeUserDropdown.bind(this)}
                        >
                            <ul>
                                <li><a onClick={this.logoutUser.bind(this)}>{t`Logout`}</a></li>
                            </ul>
                        </Dropdown>
                    </div>
                </div>
                <div className="header-placeholder" />
            </div>
        );
    }
}

export default withRouter(Header);

Header.contextType = UserContext;
