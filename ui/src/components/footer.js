import React from 'react';
import { t } from 'ttag';

class Footer extends React.Component {
	render() {
		return (
			<div className="footer">
				<div>
					<div className="headline">Rocket fuel</div>
						{t`This project is an open source project under the MIT license.`}<br />
						<a href="https://github.com/FortnoxAB/rocket-fuel" target="_blank">
						<i className="fa fa-github" /> Github
						</a>

				</div>
			</div>
		);
	}
}

export default Footer;
