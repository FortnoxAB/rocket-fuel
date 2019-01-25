import React from 'react'

class Button extends React.Component {
	getClass() {
		return `button ${this.props.color} ${this.props.className}`;
	}

	render() {
		return (
			<div onClick={this.props.onClick()} className={this.getClass()}>
				{this.props.children}
			</div>
		);
	}
}

Button.defaultProps = {
	onClick: () => {},
	color: 'primary',
	className: ''
};

export default Button;
