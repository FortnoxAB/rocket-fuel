import React from 'react';
import UserBar from './userbar';
import MenuBar from './menubar';
import QuickBar from './quickbar';
import Footer from './footer';

class SignedInView extends React.Component {
	renderSideBar() {
		console.log(2);
	}

	render() {
		return (
			<div className="view">
				<UserBar />
				<div className="flex-grow">
					<div className="main">
						<div className="flex-no-shrink">
							<MenuBar />
							<div className="content">
								{this.props.children}
							</div>
							<QuickBar />
						</div>
						<Footer />
					</div>
				</div>
			</div>
		);
	}
}

export default SignedInView;
