import React from 'react';
import Header from './layout/header';
import MenuBar from './layout/menubar';
import QuickBar from './layout/quickbar';
import Footer from './layout/footer';

class SignedInView extends React.Component {
	render() {
		return (
			<div className="view">
				<Header />
				<div className="main">
					<div className="content">
						{this.props.children}
					</div>
                    {/*<QuickBar />*/}
				</div>
				{/*
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
				*/}
			</div>
		);
	}
}

export default SignedInView;
