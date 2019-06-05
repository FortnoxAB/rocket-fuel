import React from 'react';
import PropTypes from 'prop-types';

const Certificate = (props) => (
    <span className={`certificate ${props.active ? 'active' : ''}`}>
        <i className="fa fa-certificate" />
        <i className="fa fa-check" />
    </span>
);

Certificate.defaultProps = {
    active: false
};

Certificate.propTypes = {
    active: PropTypes.bool
};

export default Certificate;
