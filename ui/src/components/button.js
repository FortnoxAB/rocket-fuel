import React from 'react'

class Button extends React.Component {
	getClass() {
		let className = `button ${this.props.color} ${this.props.className}`;
		if (this.props.circle) {
			className = `${className} circle`;
		}
		if (this.props.floating) {
			className = `${className} floating`;
		}
		return className;
	}

	render() {
		return (
			<div onClick={this.props.onClick.bind(this)} className={this.getClass()}>
				{this.props.children}
			</div>
		);
	}
}

Button.defaultProps = {
	onClick: () => {},
	color: '',
	className: '',
	circle: false,
	floating: false
};

export default Button;
