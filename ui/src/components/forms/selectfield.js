import React from 'react'
import PropTypes from 'prop-types';
import Loader from '../utils/loader';

class SelectField extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            isDropdownOpen: false
        };

        this.onClick = this.onClickEvent.bind(this);
    }

    componentDidMount() {
        window.addEventListener('mouseup', this.onClick, false);
    }

    componentWillUnmount() {
        window.removeEventListener('mouseup', this.onClick, false);
    }

    onClickEvent(e) {
        if (!this.dropdownNode) {
            return;
        }
        if (!this.dropdownNode.contains(e.target)) {
            this.setState({
                isDropdownOpen: false
            });
        }
    }

    getClasses() {
        return [
            'input',
            this.props.className,
        ].join(' ');
    }

    onChange() {
        if (this.props.loading) {
            return null;
        }

        return this.props.onChange();
    }

    renderLoader() {
        if (!this.props.loading) {
            return null;
        }
        return <Loader />
    }

    toggleDropdown() {
        this.setState({
            isDropdownOpen: !this.state.isDropdownOpen
        });
    }

    getSelectedItem() {
        const options = this.props.options;
        let title = options[0].title;
        const selectedOption = options.find(option => option.selected);
        if (selectedOption) {
            title = selectedOption.title;
        }

        return (
            <span>
                {title}
            </span>
        );
    }

    selectItem(item) {
        this.props.onChange(item);
        this.setState({
            isDropdownOpen: false
        });
    }

    renderDropdown() {
        if (!this.state.isDropdownOpen) {
            return null;
        }

        return (
            <ul className="select-dropdown" ref={(node) => {this.dropdownNode = node;}}>
                {
                    this.props.options.map((item) => {
                        return (
                            <li key={item.value} onClick={this.selectItem.bind(this, item)}>
                                {item.title}
                            </li>
                        );
                    })
                }
            </ul>
        );
    }

    render() {
        return (
            <div className={this.getClasses()}>
                <div className="input-wrap">
                    <div className="select-field" tabIndex={0} onClick={this.toggleDropdown.bind(this)}>
                        {this.getSelectedItem()}
                        <i className="fa fa-caret-down" />
                    </div>
                    {this.renderDropdown()}
                </div>
            </div>
        );
    }
}

SelectField.defaultProps = {
    onChange: () => {},
    options: [],
    className: '',
    close: () => {}
};

SelectField.propTypes = {
    onChange: PropTypes.func,
    options: PropTypes.array,
    className: PropTypes.string,
    close: PropTypes.func,
};

export default SelectField;
