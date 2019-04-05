import React from 'react';
import { t } from 'ttag';

const PageNotFoundView = () => {
	return (
		<div className="page-not-found">
			<div className="text">
				<h1>{t`404 - Page not found`}</h1>
				<p>{t`Houston we have a problem...`}</p>
			</div>
			<div className="airtube" />
			<div className="airtube-junk" />
			<div className="screw" />
			<div className="astronaut" />
			<div className="scrap" />
			<div className="scrap-far" />
			<div className="star-map" />
		</div>
	);
};

export default PageNotFoundView;
