import React from 'react'
import Loader from '../utils/loader';

class Button extends React.Component {
    getClass() {
        return [
            'button',
            `button-${this.props.color}`,
            this.props.rounded ? 'rounded' : '',
            this.props.floating ? 'floating' : '',
            this.props.circle ? 'circle' : '',
            this.props.text ? 'text' : '',
            this.props.border ? 'border' : '',
            this.props.small ? 'small' : '',
            this.props.icon ? 'icon' : '',
            this.props.className,
        ].join(' ');
    }

    onClick() {
        if (this.props.loading) {
            return null;
        }

        return this.props.onClick();
    }

    getButtonContentClassName() {
        if (!this.props.loading) {
            return '';
        }

        return 'hidden-blocking';
    }

    renderLoader() {
        if (!this.props.loading) {
            return null;
        }
        return <Loader />
    }

    render() {
        return (
            <div onClick={this.onClick.bind(this)} className={this.getClass()}>
                {this.renderLoader()}
                <span className={`button-content ${this.getButtonContentClassName()}`}>{this.props.children}</span>
            </div>
        );
    }
}

Button.defaultProps = {
    onClick: () => {
    },
    color: 'default',
    className: '',
    circle: false,
    rounded: false,
    floating: false,
    text: false,
    border: false,
    small: false,
    loading: false,
    icon: false
};

export default Button;
