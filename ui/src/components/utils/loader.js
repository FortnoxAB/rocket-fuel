import React from 'react';
import FillPage from '../../components/helpers/fillpage';

class Loader extends React.Component {
	getColor(){
		if (this.props.light) {
			return 'light';
		}
		return '';
	}

	checkFillPage() {
		if (this.props.fillPage) {
			return (
				<FillPage>
					{this.renderLoader()}
				</FillPage>
			);
		}

		return this.renderLoader();
	}

	renderLoader() {
		return (
			<div className={`loader ${this.props.size} ${this.getColor()}`}>
				<span />
				<span />
				<span />
			</div>
		);
	}

	render() {
		return this.checkFillPage();
	}
}

Loader.defaultProps = {
	size: '',
	light: false,
	fillPage: false
};

export default Loader;
