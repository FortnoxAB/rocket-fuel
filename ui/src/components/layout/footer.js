import React from 'react';
import { t } from 'ttag';

class Footer extends React.Component {
    render() {
        return (
            <div className="footer">
                <div>
                    <div className="headline">Rocket Fuel</div>
                    {t`This project is an open source project under the MIT license.`}<br />
                    <a href="https://github.com/FortnoxAB/rocket-fuel" target="_blank">
                        <i className="fa fa-github" /> Github
                    </a><br />< br />
                    {t`Developed and maintained by Fortnox.`}
                </div>
            </div>
        );
    }
}

export default Footer;
