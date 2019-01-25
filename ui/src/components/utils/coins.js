import React from 'react'

class Coins extends React.Component {
	render() {
		return (
			<span className="coins">
				<span className="amount">{this.props.amount}</span> <i className="fa fa-circle" />
			</span>
		);
	}
}

Coins.defaultProps = {
	amount: 1
};

export default Coins;
