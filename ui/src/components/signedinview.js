import React from 'react';
import { t } from 'ttag';
import Header from './layout/header';
import MenuBar from './layout/menubar';
import QuickBar from './layout/quickbar';
import Footer from './layout/footer';
import Logo from './utils/logo';

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
                    <div className="footer">
                        <div className="content">
                            <div className="text">
                                {t`Rocket fuel, developed and maintained by Fortnox`}
                            </div>
                            <div className="icons text-size-large padded-top">
                                <a href="https://github.com/FortnoxAB/rocket-fuel" target="_blank">
                                    <i className="fab fa-github" />
                                </a>
                            </div>
                            <div className="text padded-top">
                                <a href="https://github.com/FortnoxAB/rocket-fuel/blob/master/LICENSE" target="_blank">
                                    Licenced under MIT
                                </a>
                            </div>
                        </div>
                    </div>
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
