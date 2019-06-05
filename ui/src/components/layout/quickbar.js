import React from 'react';
import { t } from 'ttag';
import { NavLink } from 'react-router-dom';

class QuickBar extends React.Component {
    render() {
        return (
            <div className="quick-bar">
                <div className="headline">{t`Latest questions`}</div>
                {/*
				<ul>
					<li>Some question</li>
				</ul>
				*/}
                <div className="headline">{t`Popular tags`}</div>
                {/*
				<ul>
					<div className="tag">JavaScript</div>
					<div className="tag">ReactJs</div>
					<div className="tag">Frontend</div>
				</ul>
				*/}
            </div>
        );
    }
}

export default QuickBar;
