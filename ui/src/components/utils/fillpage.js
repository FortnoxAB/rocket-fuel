import React from 'react';

class FillPage extends React.Component {
	getClasses() {

	}

	render() {
		return (
			<div className="center-center fill-page">
				{this.props.children}
			</div>
		);
	}
}

FillPage.defaultProps = {
	fullView: false
};

export default FillPage;
