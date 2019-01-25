import React from 'react';

class Loader extends React.Component {
	getColor(){
		if (this.props.light) {
			return 'light';
		}
		return '';
	}

	render() {
		return (
			<div className={`loader ${this.props.size}`}>
				<svg className="circular" viewBox="25 25 50 50">
					<circle
						className={`path ${this.getColor()}`}
						cx="50"
						cy="50"
						r="20"
						fill="none"
						strokeWidth="2"
						strokeMiterlimit="10"
					/>
				</svg>
			</div>
		);
	}
}

Loader.defaultProps = {
	size: '',
	light: false
};

export default Loader;
